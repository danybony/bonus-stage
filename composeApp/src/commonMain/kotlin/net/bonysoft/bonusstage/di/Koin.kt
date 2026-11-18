package net.bonysoft.bonusstage.di

import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import net.bonysoft.bonusstage.data.account.AccountRepository
import net.bonysoft.bonusstage.data.account.UserDataCleaner
import net.bonysoft.bonusstage.data.alerts.BackgroundCheckCoordinator
import net.bonysoft.bonusstage.data.alerts.WishlistAlertChecker
import net.bonysoft.bonusstage.data.cache.RefreshPolicy
import net.bonysoft.bonusstage.data.widget.WidgetDataSource
import net.bonysoft.bonusstage.data.widget.WidgetSync
import net.bonysoft.bonusstage.data.cache.Refresher
import net.bonysoft.bonusstage.data.repository.CacheCleaner
import net.bonysoft.bonusstage.data.repository.CatalogRepository
import net.bonysoft.bonusstage.data.repository.FriendsRepository
import net.bonysoft.bonusstage.data.repository.GameRepository
import net.bonysoft.bonusstage.data.repository.LibraryRepository
import net.bonysoft.bonusstage.data.repository.ProfileRepository
import net.bonysoft.bonusstage.data.repository.RecapRepository
import net.bonysoft.bonusstage.data.repository.RecentAchievementsRepository
import net.bonysoft.bonusstage.data.repository.SettingsRepository
import net.bonysoft.bonusstage.data.repository.WishlistRepository
import net.bonysoft.bonusstage.data.rom.RomScanRepository
import net.bonysoft.bonusstage.db.BonusStageDatabase
import net.bonysoft.bonusstage.ui.friends.FriendsListViewModel
import net.bonysoft.bonusstage.ui.game.GameDetailViewModel
import net.bonysoft.bonusstage.ui.library.LibraryViewModel
import net.bonysoft.bonusstage.ui.profile.ProfileViewModel
import net.bonysoft.bonusstage.ui.recent.FriendsViewModel
import net.bonysoft.bonusstage.ui.recent.RecentViewModel
import net.bonysoft.bonusstage.ui.rom.RomScanViewModel
import net.bonysoft.bonusstage.ui.search.GameSearchViewModel
import net.bonysoft.bonusstage.ui.share.RecapViewModel
import net.bonysoft.bonusstage.ui.signin.SignInViewModel
import net.bonysoft.bonusstage.ui.wishlist.WishlistViewModel
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
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
    single { SettingsRepository(get()) }
    single { FriendsRepository(get(), get(), get(), get()) }
    single { RecapRepository(get()) }
    // RomLibrary comes from the platform (Android only for now); resolved only when scanning.
    single { CatalogRepository(get(), get(), get(), get()) }
    single { RomScanRepository(get(), get(), get()) }
    single { WishlistAlertChecker(get(), get(), get()) }
    single(createdAtStart = true) { BackgroundCheckCoordinator(get(), get(), get()) }
    single { WidgetDataSource(get(), get(), get()) }
    single(createdAtStart = true) { WidgetSync(get(), get(), get()) }
}

val uiModule = module {
    viewModelOf(::SignInViewModel)
    viewModelOf(::ProfileViewModel)
    viewModelOf(::LibraryViewModel)
    viewModelOf(::RecentViewModel)
    viewModelOf(::FriendsViewModel)
    viewModelOf(::RecapViewModel)
    viewModelOf(::RomScanViewModel)
    viewModelOf(::GameSearchViewModel)
    viewModelOf(::FriendsListViewModel)
    viewModelOf(::WishlistViewModel)
    viewModel { (gameId: Long) -> GameDetailViewModel(gameId, get(), get(), get()) }
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
