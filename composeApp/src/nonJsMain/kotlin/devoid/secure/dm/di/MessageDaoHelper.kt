package devoid.secure.dm.di

import devoid.secure.dm.data.local.AppDatabase
import devoid.secure.dm.data.local.MessageDao
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class MessageDaoHelper:KoinComponent {
    private val database: AppDatabase by inject()
    val messageDao : MessageDao = database.messageDao
}