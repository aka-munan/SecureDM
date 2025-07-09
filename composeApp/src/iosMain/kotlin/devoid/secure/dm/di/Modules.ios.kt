package devoid.secure.dm.di

import devoid.secure.dm.data.local.AppDatabase
import devoid.secure.dm.data.local.getDatabaseBuilder
import devoid.secure.dm.data.local.getRoomDatabase
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module

actual val platformModule: Module= module {
    single { getRoomDatabase(getDatabaseBuilder()) }.bind<AppDatabase>()
}