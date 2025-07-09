package devoid.secure.dm

import android.app.Application
import devoid.secure.dm.di.initKoin
import org.koin.android.ext.koin.androidContext


class SecureDmApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@SecureDmApplication)
        }
    }
}