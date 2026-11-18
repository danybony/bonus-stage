package net.bonysoft.bonusstage

import net.bonysoft.bonusstage.data.account.AccountRepository
import net.bonysoft.bonusstage.data.account.CredentialStore
import net.bonysoft.bonusstage.di.dataModule
import net.bonysoft.bonusstage.di.uiModule
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import kotlin.test.Test
import kotlin.test.assertNotNull

class KoinModulesTest {

    private val fakePlatform = module {
        single<CredentialStore> {
            object : CredentialStore {
                override suspend fun load(): String? = null
                override suspend fun save(value: String) = Unit
                override suspend fun clear() = Unit
            }
        }
    }

    @Test
    fun dataModulesResolve() {
        val koin = koinApplication { modules(fakePlatform, dataModule, uiModule) }.koin

        assertNotNull(koin.get<AccountRepository>())
        koin.close()
    }
}
