package devoid.secure.dm.di

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun getIOCommonDispatcher(): CoroutineDispatcher = Dispatchers.IO