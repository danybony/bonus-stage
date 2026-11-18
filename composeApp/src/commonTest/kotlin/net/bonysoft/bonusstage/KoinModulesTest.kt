package net.bonysoft.bonusstage

import net.bonysoft.bonusstage.di.appModule
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.test.verify.verify
import kotlin.test.Test

class KoinModulesTest {
    @OptIn(KoinExperimentalAPI::class)
    @Test
    fun appModuleResolves() {
        appModule.verify()
    }
}
