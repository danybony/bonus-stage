package net.bonysoft.bonusstage

import android.app.Application
import net.bonysoft.bonusstage.data.rom.RomLibrary
import net.bonysoft.bonusstage.di.initKoin
import net.bonysoft.bonusstage.rom.SafRomLibrary
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

class BonusStageApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@BonusStageApplication)
            // The native rhash lives in this module (the KMP library plugin has no NDK support).
            modules(module { single<RomLibrary> { SafRomLibrary(androidContext()) } })
        }
    }
}
