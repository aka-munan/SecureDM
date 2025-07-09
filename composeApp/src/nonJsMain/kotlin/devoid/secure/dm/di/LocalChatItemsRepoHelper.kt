package devoid.secure.dm.di

import devoid.secure.dm.data.local.LocalChatItemRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class LocalChatItemsRepoHelper:KoinComponent {
    val localChatItemRepository:LocalChatItemRepository by inject()
}