package net.bonysoft.bonusstage.di

import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import net.bonysoft.bonusstage.data.account.AccountRepository
import net.bonysoft.bonusstage.ui.signin.SignInViewModel
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/** Credential store and other platform services. */
expect val platformModule: Module

val dataModule = module {
    single { CoroutineScope(SupervisorJob()) }
    single { HttpClient() }
    single { AccountRepository(store = get(), httpClient = get(), scope = get(), cleaners = { getAll() }) }
}

val uiModule = module {
    viewModelOf(::SignInViewModel)
}

val appModules = listOf(platformModule, dataModule, uiModule)

/**
 * Starts Koin once per process. Android calls it from the Application, iOS from the app's init.
 */
fun initKoin(platformConfig: KoinApplication.() -> Unit = {}) {
    startKoin {
        platformConfig()
        modules(appModules)
    }
}
