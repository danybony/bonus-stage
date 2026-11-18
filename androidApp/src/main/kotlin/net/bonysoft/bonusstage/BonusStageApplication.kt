package net.bonysoft.bonusstage

import android.app.Application
import net.bonysoft.bonusstage.di.initKoin
import org.koin.android.ext.koin.androidContext

class BonusStageApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@BonusStageApplication)
        }
    }
}
