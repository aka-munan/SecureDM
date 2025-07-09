package devoid.secure.dm.di

import devoid.secure.dm.data.local.AppDatabase
import devoid.secure.dm.data.local.LocalChatItemRepository
import devoid.secure.dm.data.local.LocalMessageRepository
import devoid.secure.dm.ui.viewmodel.ChatViewModel
import devoid.secure.dm.ui.viewmodel.PSHomeViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

actual val JsNonJsModule: Module = module {
    single { LocalMessageRepository(appDatabase = get(), chatRepository = get(),get<AppDatabase>().attachmentDao) }
    single {
        val appDatabase = get<AppDatabase>()
        LocalChatItemRepository(
            chatItemDao = appDatabase.chatItemDao,
            profileDao = appDatabase.profileDao,
            localMessageRepository = get(),
            chatRepository = get()
        )
    }
    viewModelOf(::ChatViewModel)
    viewModelOf(::PSHomeViewModel)
}