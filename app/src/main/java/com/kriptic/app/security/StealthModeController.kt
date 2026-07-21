package com.kriptic.app.security

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager

class StealthModeController(private val context: Context) {

    private val mainComponent = ComponentName(context, "com.kriptic.app.MainActivity")
    private val calculatorAliasComponent = ComponentName(context, "com.kriptic.app.CalculatorAlias")

    fun enableStealthCalculatorMode() {
        val pm = context.packageManager

        // Enable Calculator alias launcher icon
        pm.setComponentEnabledSetting(
            calculatorAliasComponent,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )

        // Disable standard Kriptic launcher icon
        pm.setComponentEnabledSetting(
            mainComponent,
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )
    }

    fun disableStealthCalculatorMode() {
        val pm = context.packageManager

        // Enable standard Kriptic launcher icon
        pm.setComponentEnabledSetting(
            mainComponent,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )

        // Disable Calculator alias launcher icon
        pm.setComponentEnabledSetting(
            calculatorAliasComponent,
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )
    }
}
