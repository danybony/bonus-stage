package net.bonysoft.bonusstage

import net.bonysoft.bonusstage.data.alerts.BgTaskBackgroundChecks
import net.bonysoft.bonusstage.di.initKoin

/**
 * Swift cannot use Kotlin default arguments, so iOS gets its own entry point. It also registers
 * the background task, which iOS only accepts before the app finishes launching.
 */
@Suppress("unused") // Called from Swift as KoinIosKt.doInitKoinIos().
fun initKoinIos() {
    BgTaskBackgroundChecks.register()
    initKoin()
}
