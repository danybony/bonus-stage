package net.bonysoft.bonusstage.di

import net.bonysoft.bonusstage.data.account.CredentialStore
import net.bonysoft.bonusstage.data.account.KeystoreCredentialStore
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single<CredentialStore> { KeystoreCredentialStore(androidContext()) }
}
