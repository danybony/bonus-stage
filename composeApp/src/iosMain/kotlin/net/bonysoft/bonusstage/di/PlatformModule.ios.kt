package net.bonysoft.bonusstage.di

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import net.bonysoft.bonusstage.data.account.CredentialStore
import net.bonysoft.bonusstage.data.account.KeychainCredentialStore
import net.bonysoft.bonusstage.data.alerts.AlertNotifier
import net.bonysoft.bonusstage.data.alerts.BackgroundChecks
import net.bonysoft.bonusstage.data.alerts.IosAlertNotifier
import net.bonysoft.bonusstage.data.alerts.NoBackgroundChecks
import net.bonysoft.bonusstage.db.BonusStageDatabase
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single<CredentialStore> { KeychainCredentialStore() }
    single<AlertNotifier> { IosAlertNotifier() }
    single<BackgroundChecks> { NoBackgroundChecks() }
    single<SqlDriver> { NativeSqliteDriver(BonusStageDatabase.Schema, DATABASE_NAME) }
}
