package com.microsoft.device.dualscreen.fragmentshandler

import android.os.Bundle
import android.os.Parcelable
import androidx.test.filters.MediumTest
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.rule.ActivityTestRule
import com.google.common.truth.Truth.assertThat
import com.microsoft.device.dualscreen.ScreenManagerProvider
import com.microsoft.device.dualscreen.fragmentshandler.utils.SampleActivity
import com.microsoft.device.dualscreen.fragmentshandler.utils.ScreenInfoListenerImpl
import com.microsoft.device.dualscreen.fragmentshandler.utils.setOrientationLeft
import com.microsoft.device.dualscreen.fragmentshandler.utils.setOrientationRight
import com.microsoft.device.dualscreen.fragmentshandler.utils.switchFromDualToSingleScreen
import com.microsoft.device.dualscreen.fragmentshandler.utils.switchFromSingleToDualScreen
import com.microsoft.device.dualscreen.fragmentshandler.utils.unfreezeRotation
import org.junit.After
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters

@MediumTest
@RunWith(AndroidJUnit4ClassRunner::class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class FragmentManagerStateHandlerTest {
    @Rule
    @JvmField
    var rule: ActivityTestRule<SampleActivity> = ActivityTestRule(SampleActivity::class.java, false, false)
    private var screenInfoListener = ScreenInfoListenerImpl()

    @Before
    fun before() {
        ScreenManagerProvider.getScreenManager().addScreenInfoListener(screenInfoListener)
        rule.launchActivity(null)
    }

    @After
    fun after() {
        switchFragmentManagerStateToSingleScreen()
        unfreezeRotation()
        ScreenManagerProvider.getScreenManager().clear()
        screenInfoListener.resetScreenInfo()
        screenInfoListener.resetScreenInfoCounter()
        rule.finishActivity()
        FragmentManagerStateHandler.instance?.clear()
    }

    private fun switchFragmentManagerStateToSingleScreen() {
        switchFromDualToSingleScreen()
    }

    @Test
    fun testFragmentManagerStateHandler() {
        screenInfoListener.waitForScreenInfoChanges()

        assertThat(rule.lastSavedInstanceState).isNull()

        screenInfoListener.resetScreenInfoCounter()
        switchFromSingleToDualScreen()
        screenInfoListener.waitForScreenInfoChanges()

        assertThat(rule.lastSavedInstanceState).isNotNull()

        screenInfoListener.resetScreenInfoCounter()
        switchFromDualToSingleScreen()
        screenInfoListener.waitForScreenInfoChanges()

        assertThat(rule.lastSavedInstanceState).isNotNull()
        assertThat(rule.fragmentManagerState).isNotNull()

        screenInfoListener.resetScreenInfoCounter()
        switchFromSingleToDualScreen()
        screenInfoListener.waitForScreenInfoChanges()

        assertThat(rule.lastSavedInstanceState).isNotNull()
        assertThat(rule.fragmentManagerState).isNotNull()
    }

    @Test
    fun testFragmentManagerStateHandlerWithRotation270() {
        screenInfoListener.waitForScreenInfoChanges()
        screenInfoListener.resetScreenInfoCounter()

        setOrientationRight()
        screenInfoListener.waitForScreenInfoChanges()

        assertThat(rule.lastSavedInstanceState).isNotNull()
        assertThat(rule.fragmentManagerState).isNotNull()

        screenInfoListener.resetScreenInfoCounter()
        switchFromSingleToDualScreen()
        screenInfoListener.waitForScreenInfoChanges()

        assertThat(rule.lastSavedInstanceState).isNotNull()

        screenInfoListener.resetScreenInfoCounter()
        switchFromDualToSingleScreen()
        screenInfoListener.waitForScreenInfoChanges()

        assertThat(rule.lastSavedInstanceState).isNotNull()
        assertThat(rule.fragmentManagerState).isNotNull()

        screenInfoListener.resetScreenInfoCounter()
        switchFromSingleToDualScreen()
        screenInfoListener.waitForScreenInfoChanges()

        assertThat(rule.lastSavedInstanceState).isNotNull()
        assertThat(rule.fragmentManagerState).isNotNull()
    }

    @Test
    fun testFragmentManagerStateHandlerWithRotation90() {
        screenInfoListener.waitForScreenInfoChanges()
        screenInfoListener.resetScreenInfoCounter()

        setOrientationLeft()
        screenInfoListener.waitForScreenInfoChanges()

        assertThat(rule.lastSavedInstanceState).isNotNull()
        assertThat(rule.fragmentManagerState).isNotNull()

        screenInfoListener.resetScreenInfoCounter()
        switchFromSingleToDualScreen()
        screenInfoListener.waitForScreenInfoChanges()

        assertThat(rule.lastSavedInstanceState).isNotNull()

        screenInfoListener.resetScreenInfoCounter()
        switchFromDualToSingleScreen()
        screenInfoListener.waitForScreenInfoChanges()

        assertThat(rule.lastSavedInstanceState).isNotNull()
        assertThat(rule.fragmentManagerState).isNotNull()

        screenInfoListener.resetScreenInfoCounter()
        switchFromSingleToDualScreen()
        screenInfoListener.waitForScreenInfoChanges()

        assertThat(rule.lastSavedInstanceState).isNotNull()
        assertThat(rule.fragmentManagerState).isNotNull()
    }
}

private val ActivityTestRule<SampleActivity>.lastSavedInstanceState: Bundle?
    get() = activity.lastSavedInstanceState

private val ActivityTestRule<SampleActivity>.fragmentManagerState: Parcelable?
    get() = activity.lastSavedInstanceState?.getParcelable(FragmentManagerStateWrapper.FM_STATE_KEY)