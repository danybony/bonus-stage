package net.bonysoft.bonusstage.di

import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.dsl.module

val appModule = module {
}

/**
 * Starts Koin once per process. Android calls it from the Application, iOS from the app's init.
 */
fun initKoin(platformConfig: KoinApplication.() -> Unit = {}) {
    startKoin {
        platformConfig()
        modules(appModule)
    }
}
