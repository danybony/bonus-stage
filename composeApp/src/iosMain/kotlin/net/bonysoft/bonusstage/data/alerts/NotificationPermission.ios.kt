package net.bonysoft.bonusstage.data.alerts

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNAuthorizationStatusAuthorized
import platform.UserNotifications.UNAuthorizationStatusDenied
import platform.UserNotifications.UNAuthorizationStatusProvisional
import platform.UserNotifications.UNUserNotificationCenter

@Composable
actual fun rememberNotificationPermissionState(): NotificationPermissionState {
    var granted by remember { mutableStateOf(false) }
    var denied by remember { mutableStateOf(false) }
    val center = UNUserNotificationCenter.currentNotificationCenter()
    LaunchedEffect(Unit) {
        center.getNotificationSettingsWithCompletionHandler { settings ->
            val status = settings?.authorizationStatus
            granted = status == UNAuthorizationStatusAuthorized || status == UNAuthorizationStatusProvisional
            denied = status == UNAuthorizationStatusDenied
        }
    }
    return object : NotificationPermissionState {
        override val isGranted get() = granted
        override val isDenied get() = denied

        override fun request() {
            center.requestAuthorizationWithOptions(UNAuthorizationOptionAlert or UNAuthorizationOptionSound) { ok, _ ->
                granted = ok
                denied = !ok
            }
        }
    }
}
