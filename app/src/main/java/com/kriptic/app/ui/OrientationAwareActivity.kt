package com.kriptic.app.ui

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.kriptic.app.utils.DeviceUtils

/**
 * Base activity that automatically sets orientation based on device type.
 * Tablets can rotate to landscape, phones are locked to portrait.
 */
abstract class OrientationAwareActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setOrientationBasedOnDeviceType()
    }

    private fun setOrientationBasedOnDeviceType() {
        requestedOrientation = if (DeviceUtils.isTablet(this)) {
            // Allow all orientations on tablets
            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        } else {
            // Lock to portrait on phones
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }
}
