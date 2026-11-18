package net.bonysoft.bonusstage

import net.bonysoft.bonusstage.di.initKoin

/** Swift cannot use Kotlin default arguments, so iOS gets its own entry point. */
@Suppress("unused") // Called from Swift as KoinIosKt.doInitKoinIos().
fun initKoinIos() = initKoin()
