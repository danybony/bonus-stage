package net.bonysoft.bonusstage.di

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import net.bonysoft.bonusstage.data.account.CredentialStore
import net.bonysoft.bonusstage.data.account.KeychainCredentialStore
import net.bonysoft.bonusstage.data.alerts.AlertNotifier
import net.bonysoft.bonusstage.data.alerts.BackgroundChecks
import net.bonysoft.bonusstage.data.alerts.IosAlertNotifier
import net.bonysoft.bonusstage.data.alerts.BgTaskBackgroundChecks
import net.bonysoft.bonusstage.data.rom.IosRomLibrary
import net.bonysoft.bonusstage.data.rom.RomLibrary
import net.bonysoft.bonusstage.data.widget.WidgetUpdater
import net.bonysoft.bonusstage.db.BonusStageDatabase
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single<CredentialStore> { KeychainCredentialStore() }
    single<AlertNotifier> { IosAlertNotifier() }
    single<BackgroundChecks> { BgTaskBackgroundChecks() }
    single<RomLibrary> { IosRomLibrary() }
    // WidgetKit comes with the iOS release.
    single<WidgetUpdater> { WidgetUpdater { } }
    single<SqlDriver> { NativeSqliteDriver(BonusStageDatabase.Schema, DATABASE_NAME) }
}
