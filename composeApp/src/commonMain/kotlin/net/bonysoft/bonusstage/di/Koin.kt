package net.bonysoft.bonusstage.di

import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import net.bonysoft.bonusstage.data.account.AccountRepository
import net.bonysoft.bonusstage.data.account.UserDataCleaner
import net.bonysoft.bonusstage.data.cache.RefreshPolicy
import net.bonysoft.bonusstage.data.cache.Refresher
import net.bonysoft.bonusstage.data.repository.CacheCleaner
import net.bonysoft.bonusstage.data.repository.GameRepository
import net.bonysoft.bonusstage.data.repository.LibraryRepository
import net.bonysoft.bonusstage.data.repository.ProfileRepository
import net.bonysoft.bonusstage.data.repository.RecentAchievementsRepository
import net.bonysoft.bonusstage.data.repository.WishlistRepository
import net.bonysoft.bonusstage.db.BonusStageDatabase
import net.bonysoft.bonusstage.ui.signin.SignInViewModel
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/** Credential store, SQL driver and other platform services. */
expect val platformModule: Module

const val DATABASE_NAME = "bonus-stage.db"

val dataModule = module {
    single { CoroutineScope(SupervisorJob()) }
    single { HttpClient() }
    single { AccountRepository(store = get(), httpClient = get(), scope = get(), cleaners = { getAll() }) }
    single { BonusStageDatabase(get()) }
    single { RefreshPolicy() }
    single { Refresher(get()) }
    singleOf(::CacheCleaner) { bind<UserDataCleaner>() }
    single { ProfileRepository(get(), get(), get(), get()) }
    single { LibraryRepository(get(), get(), get(), get()) }
    single { GameRepository(get(), get(), get(), get()) }
    single { RecentAchievementsRepository(get(), get(), get(), get()) }
    single { WishlistRepository(get(), get(), get(), get()) }
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
