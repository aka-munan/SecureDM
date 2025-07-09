package devoid.secure.dm.di

import devoid.secure.dm.data.datastore.SettingsSource
import devoid.secure.dm.data.datastore.createDatastore
import devoid.secure.dm.data.local.AppDatabase
import devoid.secure.dm.data.local.getDatabaseBuilder
import devoid.secure.dm.data.local.getRoomDatabase
import devoid.secure.dm.domain.AppNotificationManager
import devoid.secure.dm.domain.PSNotificationManager
import devoid.secure.dm.ui.viewmodel.SettingsViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module

actual fun getIOCommonDispatcher(): CoroutineDispatcher = Dispatchers.IO
actual val platformModule: Module= module {
    single { getRoomDatabase(getDatabaseBuilder()) }.bind<AppDatabase>()
    single { PSNotificationManager() }.bind<AppNotificationManager>()
//    single<PrefsDataStore>{ createDatastore() }
    single<SettingsSource>{ SettingsSource(createDatastore()) }
    viewModel { SettingsViewModel(get()) }
}