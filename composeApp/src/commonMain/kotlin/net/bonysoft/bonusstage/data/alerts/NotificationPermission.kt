package net.bonysoft.bonusstage.data.alerts

import androidx.compose.runtime.Composable

/** Whether the app may post notifications, and a way to ask. */
interface NotificationPermissionState {
    val isGranted: Boolean

    /** True once the user said no: the system will not show the prompt again. */
    val isDenied: Boolean

    fun request()
}

@Composable
expect fun rememberNotificationPermissionState(): NotificationPermissionState

/** Schedules or cancels the periodic background check for the signed-in user. */
interface BackgroundChecks {
    fun schedule()
    fun cancel()
}
