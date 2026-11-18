package net.bonysoft.bonusstage.di

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import net.bonysoft.bonusstage.data.account.CredentialStore
import net.bonysoft.bonusstage.data.account.KeystoreCredentialStore
import net.bonysoft.bonusstage.data.alerts.AlertNotifier
import net.bonysoft.bonusstage.data.alerts.AndroidAlertNotifier
import net.bonysoft.bonusstage.data.alerts.BackgroundChecks
import net.bonysoft.bonusstage.data.alerts.WorkManagerBackgroundChecks
import net.bonysoft.bonusstage.data.widget.WidgetUpdater
import net.bonysoft.bonusstage.db.BonusStageDatabase
import net.bonysoft.bonusstage.widget.GlanceWidgetUpdater
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single<CredentialStore> { KeystoreCredentialStore(androidContext()) }
    single<AlertNotifier> { AndroidAlertNotifier(androidContext()) }
    single<BackgroundChecks> { WorkManagerBackgroundChecks(androidContext()) }
    single<WidgetUpdater> { GlanceWidgetUpdater(androidContext()) }
    single<SqlDriver> { AndroidSqliteDriver(BonusStageDatabase.Schema, androidContext(), DATABASE_NAME) }
}
