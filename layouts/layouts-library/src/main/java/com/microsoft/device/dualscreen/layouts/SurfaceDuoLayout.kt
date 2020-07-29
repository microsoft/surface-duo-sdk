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
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import com.microsoft.device.dualscreen.core.ScreenMode

/**
 * Class that is the root view of the layout containers for different screen modes.
 * The class receives the layout ids for the views that will be added inside of the
 * containers and then creates a SurfaceDuoLayoutStatusHandler to handle the logic for each screen
 * state.
 */
open class SurfaceDuoLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    val config: Config = Config()
) : LinearLayout(context, attrs, defStyleAttr) {

    constructor(context: Context, config: Config) : this(context, null, 0, config)

    private lateinit var surfaceDuoLayoutStatusHandler: SurfaceDuoLayoutStatusHandler

    init {
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        gravity = Gravity.BOTTOM

        if (attrs != null) {
            val styledAttributes = readAttributes(context, attrs)

            if (this.isInEditMode) {
                createAndroidStudioPreview(styledAttributes, config)
            } else {
                createView(config)
            }
        } else {
            createView(config)
        }
    }

    private fun readAttributes(context: Context, attrs: AttributeSet?): TypedArray {
        val styledAttributes =
            context.theme.obtainStyledAttributes(attrs, R.styleable.SurfaceDuoLayout, 0, 0)
        try {
            createConfiguration(styledAttributes)
        } finally {
            styledAttributes.recycle()
        }
        return styledAttributes
    }

    private fun createConfiguration(styledAttributes: TypedArray) {
        config.singleScreenLayoutId = styledAttributes.getResourceId(
            R.styleable.SurfaceDuoLayout_single_screen_layout_id,
            View.NO_ID
        )
        config.dualScreenStartLayoutId = styledAttributes.getResourceId(
            R.styleable.SurfaceDuoLayout_dual_screen_start_layout_id,
            View.NO_ID
        )
        config.dualScreenEndLayoutId = styledAttributes.getResourceId(
            R.styleable.SurfaceDuoLayout_dual_screen_end_layout_id,
            View.NO_ID
        )
        // Single container in Dual Screen mode
        config.dualPortraitSingleLayoutId = styledAttributes.getResourceId(
            R.styleable.SurfaceDuoLayout_dual_portrait_single_layout_id,
            View.NO_ID
        )
        config.isDualPortraitSingleContainer = styledAttributes.getBoolean(
            R.styleable.SurfaceDuoLayout_is_dual_portrait_single_container,
            false
        )
        config.dualLandscapeSingleLayoutId = styledAttributes.getResourceId(
            R.styleable.SurfaceDuoLayout_dual_landscape_single_layout_id,
            View.NO_ID
        )
        config.isDualLandscapeSingleContainer = styledAttributes.getBoolean(
            R.styleable.SurfaceDuoLayout_is_dual_landscape_single_container,
            false
        )
    }

    /**
     * Instantiates SurfaceDuoLayoutStatusHandler that will add the containers and views
     * depending on the given configuration.
     *
     * @param config The configuration of the SurfaceDuoLayout
     */
    private fun createView(config: Config) {
        surfaceDuoLayoutStatusHandler = SurfaceDuoLayoutStatusHandler(
            this.context,
            this,
            config
        )
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        surfaceDuoLayoutStatusHandler.onConfigurationChanged(this, newConfig)
    }

    /**
     * Creates the preview that is visible in Android Studio
     */
    private fun createAndroidStudioPreview(
        styledAttributes: TypedArray,
        config: Config
    ) {
        styledAttributes.getResourceId(
            R.styleable.SurfaceDuoLayout_show_in_single_screen,
            View.NO_ID
        ).takeIf { it != View.NO_ID }
            ?.let { config.singleScreenLayoutId = it }

        styledAttributes.getResourceId(
            R.styleable.SurfaceDuoLayout_show_in_dual_screen_start,
            View.NO_ID
        ).takeIf { it != View.NO_ID }
            ?.let { config.dualScreenStartLayoutId = it }

        styledAttributes.getResourceId(
            R.styleable.SurfaceDuoLayout_show_in_dual_screen_end,
            View.NO_ID
        ).takeIf { it != View.NO_ID }
            ?.let { config.dualScreenEndLayoutId = it }

        styledAttributes.getInteger(
            R.styleable.SurfaceDuoLayout_show_in_dual_portrait_single_container,
            View.NO_ID
        ).takeIf { it != View.NO_ID }
            ?.let { config.dualPortraitSingleLayoutId = it }

        styledAttributes.getInteger(
            R.styleable.SurfaceDuoLayout_show_in_dual_landscape_single_container,
            View.NO_ID
        ).takeIf { it != View.NO_ID }
            ?.let { config.dualLandscapeSingleLayoutId = it }

        val screenMode: ScreenMode = ScreenMode.fromId(
            styledAttributes.getResourceId(
                R.styleable.SurfaceDuoLayout_tools_screen_mode,
                ScreenMode.SINGLE_SCREEN.ordinal
            )
        )
        val hingeColor: HingeColor = HingeColor.fromId(
            styledAttributes.getResourceId(
                R.styleable.SurfaceDuoLayout_tools_hinge_color,
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
        private val HINGE_DIMENSION = 84

        init {
            when (screenMode) {
                ScreenMode.SINGLE_SCREEN -> {
                    val singleScreenView = LayoutInflater
                        .from(context)
                        .inflate(config.singleScreenLayoutId, null)
                    singleScreenView.layoutParams =
                        LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
                    this@SurfaceDuoLayout.orientation = VERTICAL
                    this@SurfaceDuoLayout.addView(singleScreenView)
                }
                ScreenMode.DUAL_SCREEN -> {
                    addDualScreenPreview()
                }
            }
        }

        private fun createHingePreview(hingeColor: HingeColor): FrameLayout {
            val hinge = FrameLayout(context)
            hinge.id = View.generateViewId()

            when (resources.configuration.orientation) {
                Configuration.ORIENTATION_LANDSCAPE -> {
                    hinge.layoutParams = LayoutParams(
                        HINGE_DIMENSION,
                        LayoutParams.MATCH_PARENT
                    )
                }
                Configuration.ORIENTATION_PORTRAIT -> {
                    hinge.layoutParams = LayoutParams(
                        LayoutParams.MATCH_PARENT,
                        HINGE_DIMENSION
                    )
                }
            }

            when (hingeColor) {
                HingeColor.BLACK -> {
                    hinge.background = ColorDrawable(
                        ContextCompat.getColor(context, R.color.black)
                    )
                }
                HingeColor.WHITE -> {
                    hinge.background = ColorDrawable(
                        ContextCompat.getColor(context, R.color.white)
                    )
                }
            }
            return hinge
        }

        private fun addDualScreenPreview() {
            this@SurfaceDuoLayout.weightSum = 2F
            this@SurfaceDuoLayout.layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT
            )

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
                    LayoutInflater.from(context).inflate(
                        config.dualPortraitSingleLayoutId,
                        null
                    ).apply {
                        id = View.generateViewId()
                        layoutParams = LayoutParams(0, 0)
                    }
                }
                Configuration.ORIENTATION_PORTRAIT -> {
                    LayoutInflater.from(context).inflate(
                        config.dualLandscapeSingleLayoutId,
                        null
                    ).apply {
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
            rootContainer.layoutParams = ConstraintLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )

            val hinge = createHingePreview(hingeColor)
            applyParentConstraintsToView(hinge, rootContainer)

            val layout = getSingleContainerLayout()
            applyParentConstraintsToView(layout, rootContainer)

            this@SurfaceDuoLayout.addView(rootContainer)
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
                    this@SurfaceDuoLayout.orientation = HORIZONTAL
                    val params = LayoutParams(
                        0,
                        LayoutParams.MATCH_PARENT,
                        1F
                    )
                    dualScreenStartView.layoutParams = params
                    dualScreenEndView.layoutParams = params
                }
                Configuration.ORIENTATION_PORTRAIT -> {
                    this@SurfaceDuoLayout.orientation = VERTICAL
                    val params = LayoutParams(
                        LayoutParams.MATCH_PARENT,
                        0,
                        1F
                    )
                    dualScreenStartView.layoutParams = params
                    dualScreenEndView.layoutParams = params
                }
            }

            this@SurfaceDuoLayout.addView(dualScreenStartView)
            this@SurfaceDuoLayout.addView(hinge)
            this@SurfaceDuoLayout.addView(dualScreenEndView)
        }
    }

    /**
     * Class that contains the attributes to describe SurfaceDuoLayout behaviour.
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
     * Base class to keep the configuration of the SurfaceDuoLayout
     *
     * @param <T> An object that extends BaseConfig
     */
    @Suppress("UNCHECKED_CAST")
    sealed class BaseConfig<T : BaseConfig<T>>(protected val config: Config) {

        /**
         * Class to add a new config in SurfaceDuoLayout
         * and recreate the view
         */
        class NewConfigCreator(
            private val surfaceDuoLayout: SurfaceDuoLayout
        ) : BaseConfig<NewConfigCreator>(Config()) {
            fun reInflate() {
                surfaceDuoLayout.createView(config)
            }
        }

        /**
         * Class to update the config in SurfaceDuoLayout
         * and recreate the view
         */
        class UpdateConfigCreator(
            private val surfaceDuoLayout: SurfaceDuoLayout
        ) : BaseConfig<UpdateConfigCreator>(surfaceDuoLayout.config.copy()) {
            fun reInflate() {
                surfaceDuoLayout.createView(config)
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
}
