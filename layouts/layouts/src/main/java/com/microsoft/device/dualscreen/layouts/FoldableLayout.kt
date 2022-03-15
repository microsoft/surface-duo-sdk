/*
 *
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License.
 *
 */

package com.microsoft.device.dualscreen.layouts

import android.content.Context
import android.content.res.Configuration
import android.content.res.TypedArray
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.LinearLayout.LayoutParams.MATCH_PARENT
import androidx.activity.ComponentActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.window.layout.WindowInfoTracker
import com.microsoft.device.dualscreen.utils.wm.ScreenMode
import com.microsoft.device.dualscreen.utils.wm.extractFoldingFeatureRect
import com.microsoft.device.dualscreen.utils.wm.getFoldingFeature
import com.microsoft.device.dualscreen.utils.wm.isFoldingFeatureVertical
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectIndexed
import kotlinx.coroutines.launch

/**
 * Class that is the root view of the layout containers for different screen modes.
 * The class receives the layout ids for the views that will be added inside of the
 * containers and then creates a [FoldableLayoutController] to handle the logic for each screen
 * state.
 * On foldable devices it supports the following configurations:
 * - Two children side by side when the device is double portrait mode.
 * - Two children on above the other when the device is in double landscape mode.
 * When the device is in double portrait or landscape mode, the [FoldableLayout] can be forced to display a single child.
 * The [FoldableLayout] behaviour is control using the [FoldableLayout.Config] class.
 */
open class FoldableLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    private val config: Config = Config()
) : LinearLayout(context, attrs, defStyleAttr) {

    constructor(context: Context, config: Config) : this(context, null, 0, config)

    private lateinit var layoutController: FoldableLayoutController
    private lateinit var viewModel: FoldableLayoutViewModel
    private var job: Job? = null
    val currentConfiguration: FoldableLayout.Config
        get() = viewModel.layoutConfig ?: config

    init {
        if (context !is ViewModelStoreOwner) {
            throw RuntimeException(
                "Context must implement androidx.lifecycle.ViewModelStoreOwner " +
                    "or should extend androidx.fragment.app.FragmentActivity or androidx.appcompat.app.AppCompatActivity"
            )
        }

        layoutParams = LayoutParams(MATCH_PARENT, MATCH_PARENT)
        gravity = Gravity.BOTTOM

        setupViewModel()
        val currentConfiguration = viewModel.layoutConfig ?: config
        viewModel.layoutConfig = currentConfiguration

        if (attrs != null) {
            val styledAttributes = readAttributes(context, attrs)

            if (this.isInEditMode) {
                createAndroidStudioPreview(styledAttributes, currentConfiguration)
            } else {
                createView(currentConfiguration)
            }
        } else {
            createView(currentConfiguration)
        }

        registerWindowInfoFlow()
    }

    private fun registerWindowInfoFlow() {
        val activity = (context as? ComponentActivity)
            ?: throw RuntimeException("Context must implement androidx.activity.ComponentActivity!")
        job = activity.lifecycleScope.launch(Dispatchers.Main) {
            activity.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                WindowInfoTracker.getOrCreate(activity)
                    .windowLayoutInfo(activity)
                    .collectIndexed { index, info ->
                        if (index == 0) {
                            viewModel.windowLayoutInfo = info
                            layoutController.foldingFeature = info.getFoldingFeature()
                        }
                    }
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        job?.cancel()
    }

    /**
     * Update the layout containers based on the given configuration and view model screen state
     *
     * @param config The new configuration
     */
    private fun updateContentWithConfiguration(config: Config) {
        viewModel.layoutConfig = config
        layoutController.changeConfiguration(config)
    }

    /**
     * Creates the view model and initialise his internal state
     */
    private fun setupViewModel() {
        viewModel =
            ViewModelProvider(context as ViewModelStoreOwner).get(FoldableLayoutViewModel::class.java)
    }

    private fun readAttributes(context: Context, attrs: AttributeSet?): TypedArray {
        val styledAttributes =
            context.theme.obtainStyledAttributes(attrs, R.styleable.FoldableLayout, 0, 0)
        try {
            createConfiguration(styledAttributes)
        } finally {
            styledAttributes.recycle()
        }
        return styledAttributes
    }

    private fun createConfiguration(styledAttributes: TypedArray) {
        config.singleScreenLayoutId = styledAttributes.getResourceId(
            R.styleable.FoldableLayout_single_screen_layout_id,
            View.NO_ID
        )
        config.dualScreenStartLayoutId = styledAttributes.getResourceId(
            R.styleable.FoldableLayout_dual_screen_start_layout_id,
            View.NO_ID
        )
        config.dualScreenEndLayoutId = styledAttributes.getResourceId(
            R.styleable.FoldableLayout_dual_screen_end_layout_id,
            View.NO_ID
        )
        // Single container in Dual Screen mode
        config.dualPortraitSingleLayoutId = styledAttributes.getResourceId(
            R.styleable.FoldableLayout_dual_portrait_single_layout_id,
            View.NO_ID
        )
        config.isDualPortraitSingleContainer = styledAttributes.getBoolean(
            R.styleable.FoldableLayout_is_dual_portrait_single_container,
            false
        )
        config.dualLandscapeSingleLayoutId = styledAttributes.getResourceId(
            R.styleable.FoldableLayout_dual_landscape_single_layout_id,
            View.NO_ID
        )
        config.isDualLandscapeSingleContainer = styledAttributes.getBoolean(
            R.styleable.FoldableLayout_is_dual_landscape_single_container,
            false
        )
    }

    /**
     * Instantiates [FoldableLayoutController] that will add the containers and views
     * depending on the given configuration.
     *
     * @param config The configuration of the [FoldableLayout]
     */
    private fun createView(config: Config) {
        layoutController = FoldableLayoutController(this, config)
    }

    fun addContentChangedListener(listener: ContentChangedListener) {
        layoutController.addContentChangedListener(listener)
    }

    fun removeContentChangedListener(callback: ContentChangedListener) {
        layoutController.removeContentChangedListener(callback)
    }

    fun doAfterContentChanged(runnable: Runnable) {
        if (layoutController.isChangingContent) {
            addContentChangedListener(
                object : ContentChangedListener {
                    override fun contentChanged() {
                        runnable.run()
                        removeContentChangedListener(this)
                    }
                }
            )
        } else {
            runnable.run()
        }
    }

    /**
     * Creates the preview that is visible in Android Studio
     */
    private fun createAndroidStudioPreview(
        styledAttributes: TypedArray,
        config: Config
    ) {
        styledAttributes.getResourceId(
            R.styleable.FoldableLayout_show_in_single_screen,
            View.NO_ID
        ).takeIf { it != View.NO_ID }
            ?.let { config.singleScreenLayoutId = it }

        styledAttributes.getResourceId(
            R.styleable.FoldableLayout_show_in_dual_screen_start,
            View.NO_ID
        ).takeIf { it != View.NO_ID }
            ?.let { config.dualScreenStartLayoutId = it }

        styledAttributes.getResourceId(
            R.styleable.FoldableLayout_show_in_dual_screen_end,
            View.NO_ID
        ).takeIf { it != View.NO_ID }
            ?.let { config.dualScreenEndLayoutId = it }

        styledAttributes.getInteger(
            R.styleable.FoldableLayout_show_in_dual_portrait_single_container,
            View.NO_ID
        ).takeIf { it != View.NO_ID }
            ?.let { config.dualPortraitSingleLayoutId = it }

        styledAttributes.getInteger(
            R.styleable.FoldableLayout_show_in_dual_landscape_single_container,
            View.NO_ID
        ).takeIf { it != View.NO_ID }
            ?.let { config.dualLandscapeSingleLayoutId = it }

        val screenMode: ScreenMode = ScreenMode.fromId(
            styledAttributes.getResourceId(
                R.styleable.FoldableLayout_tools_screen_mode,
                ScreenMode.SINGLE_SCREEN.ordinal
            )
        )
        val hingeColor: HingeColor = HingeColor.fromId(
            styledAttributes.getResourceId(
                R.styleable.FoldableLayout_tools_hinge_color,
                HingeColor.BLACK.ordinal
            )
        )

        PreviewRenderer(config, screenMode, hingeColor)
    }

    internal inner class PreviewRenderer(
        private val config: Config,
        screenMode: ScreenMode,
        private val hingeColor: HingeColor
    ) {
        init {
            when (screenMode) {
                ScreenMode.SINGLE_SCREEN -> {
                    val singleScreenView =
                        LayoutInflater.from(context).inflate(config.singleScreenLayoutId, null)
                    singleScreenView.layoutParams = LayoutParams(MATCH_PARENT, MATCH_PARENT)
                    this@FoldableLayout.orientation = VERTICAL
                    this@FoldableLayout.addView(singleScreenView)
                }
                ScreenMode.DUAL_SCREEN -> {
                    addDualScreenPreview()
                }
            }
        }

        private fun createHingePreview(hingeColor: HingeColor): FrameLayout {
            val hinge = FrameLayout(context)
            hinge.id = View.generateViewId()
            val foldingFeatureThickness =
                viewModel.windowLayoutInfo.extractFoldingFeatureRect().let { rect ->
                    if (viewModel.windowLayoutInfo.isFoldingFeatureVertical()) {
                        rect.width()
                    } else {
                        rect.height()
                    }
                }

            when (resources.configuration.orientation) {
                Configuration.ORIENTATION_LANDSCAPE -> {
                    hinge.layoutParams = LayoutParams(foldingFeatureThickness, MATCH_PARENT)
                }
                Configuration.ORIENTATION_PORTRAIT -> {
                    hinge.layoutParams = LayoutParams(MATCH_PARENT, foldingFeatureThickness)
                }
                else -> {
                }
            }

            when (hingeColor) {
                HingeColor.BLACK -> {
                    hinge.background = ColorDrawable(ContextCompat.getColor(context, R.color.black))
                }
                HingeColor.WHITE -> {
                    hinge.background = ColorDrawable(ContextCompat.getColor(context, R.color.white))
                }
            }
            return hinge
        }

        private fun addDualScreenPreview() {
            this@FoldableLayout.weightSum = 2F
            this@FoldableLayout.layoutParams = LayoutParams(MATCH_PARENT, MATCH_PARENT)

            if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                if (config.dualPortraitSingleLayoutId != View.NO_ID) {
                    addDualScreenSingleContainerPreview()
                } else {
                    addDualScreenDualContainerPreview()
                }
            } else if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                if (config.dualLandscapeSingleLayoutId != View.NO_ID) {
                    addDualScreenSingleContainerPreview()
                } else {
                    addDualScreenDualContainerPreview()
                }
            }
        }

        private fun applyParentConstraintsToView(view: View, root: ConstraintLayout) {
            root.addView(view)

            val hingeConstraintSet = ConstraintSet()
            hingeConstraintSet.clone(root)
            hingeConstraintSet.connect(
                view.id,
                ConstraintSet.TOP,
                ConstraintSet.PARENT_ID,
                ConstraintSet.TOP,
                0
            )
            hingeConstraintSet.connect(
                view.id,
                ConstraintSet.LEFT,
                ConstraintSet.PARENT_ID,
                ConstraintSet.LEFT,
                0
            )
            hingeConstraintSet.connect(
                view.id,
                ConstraintSet.RIGHT,
                ConstraintSet.PARENT_ID,
                ConstraintSet.RIGHT,
                0
            )
            hingeConstraintSet.connect(
                view.id,
                ConstraintSet.BOTTOM,
                ConstraintSet.PARENT_ID,
                ConstraintSet.BOTTOM,
                0
            )
            hingeConstraintSet.applyTo(root)
        }

        private fun getSingleContainerLayout(): View {
            return when (resources.configuration.orientation) {
                Configuration.ORIENTATION_LANDSCAPE -> {
                    LayoutInflater.from(context).inflate(config.dualPortraitSingleLayoutId, null)
                        .apply {
                            id = View.generateViewId()
                            layoutParams = LayoutParams(0, 0)
                        }
                }
                Configuration.ORIENTATION_PORTRAIT -> {
                    LayoutInflater.from(context).inflate(config.dualLandscapeSingleLayoutId, null)
                        .apply {
                            id = View.generateViewId()
                            layoutParams = LayoutParams(0, 0)
                        }
                }
                else -> {
                    FrameLayout(context)
                }
            }
        }

        private fun addDualScreenSingleContainerPreview() {
            val rootContainer = ConstraintLayout(context)
            rootContainer.id = View.generateViewId()
            rootContainer.layoutParams = ConstraintLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)

            val hinge = createHingePreview(hingeColor)
            applyParentConstraintsToView(hinge, rootContainer)

            val layout = getSingleContainerLayout()
            applyParentConstraintsToView(layout, rootContainer)

            this@FoldableLayout.addView(rootContainer)
        }

        private fun addDualScreenDualContainerPreview() {
            val hinge = createHingePreview(hingeColor)
            val dualScreenStartView = LayoutInflater
                .from(context)
                .inflate(config.dualScreenStartLayoutId, null)
            val dualScreenEndView = LayoutInflater
                .from(context)
                .inflate(config.dualScreenEndLayoutId, null)

            when (resources.configuration.orientation) {
                Configuration.ORIENTATION_LANDSCAPE -> {
                    this@FoldableLayout.orientation = HORIZONTAL
                    val params = LayoutParams(0, MATCH_PARENT, 1F)
                    dualScreenStartView.layoutParams = params
                    dualScreenEndView.layoutParams = params
                }
                Configuration.ORIENTATION_PORTRAIT -> {
                    this@FoldableLayout.orientation = VERTICAL
                    val params = LayoutParams(MATCH_PARENT, 0, 1F)
                    dualScreenStartView.layoutParams = params
                    dualScreenEndView.layoutParams = params
                }
                else -> {
                }
            }

            this@FoldableLayout.addView(dualScreenStartView)
            this@FoldableLayout.addView(hinge)
            this@FoldableLayout.addView(dualScreenEndView)
        }
    }

    /**
     * Class that contains the attributes to describe FoldableLayout behaviour.
     *
     * @param singleScreenLayoutId The layout id that will be inflated in the single screen container
     * @param dualScreenStartLayoutId The layout id that will be inflated in the dual screen start container.
     * @param dualScreenEndLayoutId The layout id that will be inflated in the dual screen end container.
     * @param dualPortraitSingleLayoutId The layout id that will be inflated in
     * dual portrait single container (dual screen landscape container).
     * @param isDualPortraitSingleContainer Boolean to add just the container in
     * dual portrait single container (dual screen landscape container).
     * The container can be found using R.id.dual_portrait_single_container_id
     * @param dualLandscapeSingleLayoutId The layout id that will be inflated in
     * dual landscape single container (dual screen portrait container).
     * @param isDualLandscapeSingleContainer Boolean to add just the container in
     * dual landscape single container (dual screen portrait container).
     * The container can be found using R.id.dual_landscape_single_container_id
     */
    data class Config(
        var singleScreenLayoutId: Int = View.NO_ID,
        var dualScreenStartLayoutId: Int = View.NO_ID,
        var dualScreenEndLayoutId: Int = View.NO_ID,
        var dualPortraitSingleLayoutId: Int = View.NO_ID,
        var isDualPortraitSingleContainer: Boolean = false,
        var dualLandscapeSingleLayoutId: Int = View.NO_ID,

        var isDualLandscapeSingleContainer: Boolean = false
    )

    fun newConfigCreator() = BaseConfig.NewConfigCreator(this)
    fun updateConfigCreator() = BaseConfig.UpdateConfigCreator(this)

    /**
     * Base class to keep the configuration of the [FoldableLayout]
     *
     * @param <T> An object that extends BaseConfig
     */
    @Suppress("UNCHECKED_CAST")
    sealed class BaseConfig<T : BaseConfig<T>>(protected val config: Config) {

        /**
         * Class to add a new config in [FoldableLayout]
         * and recreate the view
         */
        class NewConfigCreator(
            private val foldableLayout: FoldableLayout
        ) : BaseConfig<NewConfigCreator>(Config()) {
            fun reInflate() {
                foldableLayout.updateContentWithConfiguration(config)
            }
        }

        /**
         * Class to update the config in [FoldableLayout]
         * and recreate the view
         */
        class UpdateConfigCreator(
            private val foldableLayout: FoldableLayout
        ) : BaseConfig<UpdateConfigCreator>(foldableLayout.config.copy()) {
            fun reInflate() {
                foldableLayout.updateContentWithConfiguration(config)
            }
        }

        fun singleScreenLayoutId(singleScreenLayoutId: Int): T =
            apply { config.singleScreenLayoutId = singleScreenLayoutId } as T

        fun dualScreenStartLayoutId(dualScreenStartLayoutId: Int): T =
            apply { config.dualScreenStartLayoutId = dualScreenStartLayoutId } as T

        fun dualScreenEndLayoutId(dualScreenEndLayoutId: Int): T =
            apply { config.dualScreenEndLayoutId = dualScreenEndLayoutId } as T

        fun dualPortraitSingleLayoutId(dualPortraitSingleLayoutId: Int): T =
            apply { config.dualPortraitSingleLayoutId = dualPortraitSingleLayoutId } as T

        fun isDualPortraitSingleContainer(isDualPortraitSingleContainer: Boolean): T =
            apply { config.isDualPortraitSingleContainer = isDualPortraitSingleContainer } as T

        fun dualLandscapeSingleLayoutId(dualLandscapeSingleLayoutId: Int): T =
            apply { config.dualLandscapeSingleLayoutId = dualLandscapeSingleLayoutId } as T

        fun isDualLandscapeSingleContainer(isDualLandscapeSingleContainer: Boolean): T =
            apply { config.isDualLandscapeSingleContainer = isDualLandscapeSingleContainer } as T
    }

    @FunctionalInterface
    interface ContentChangedListener {
        fun contentChanged()
    }
}
