package devoid.secure.dm.data.local

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import co.touchlab.kermit.Logger
import devoid.secure.dm.data.remote.ChatsRemoteMediator
import devoid.secure.dm.domain.toChatItemEntity
import devoid.secure.dm.domain.toProfileEntity
import devoid.secure.dm.domain.toRemoteUser
import devoid.secure.dm.domain.model.LocalChatItem
import devoid.secure.dm.domain.model.ProfileEntity
import devoid.secure.dm.domain.model.ChatItem
import devoid.secure.dm.domain.model.ChatRepository
import devoid.secure.dm.domain.model.Message
import devoid.secure.dm.domain.model.UserClass

class LocalChatItemRepository(
    private val chatItemDao: ChatItemDao,
    private val profileDao: ProfileDao,
    private val localMessageRepository: LocalMessageRepository,
    private val chatRepository: ChatRepository
) {
    @OptIn(ExperimentalPagingApi::class)
    fun getChatItemsPaged(pageSize: Int): Pager<Int, LocalChatItem> {
        return Pager(
            config = PagingConfig(pageSize = pageSize, prefetchDistance = 2),
            remoteMediator = ChatsRemoteMediator(localChatItemRepository = this, chatRepository),
            pagingSourceFactory = { chatItemDao.pagingSource() }
        )
    }

    suspend fun addProfile(profileEntity: ProfileEntity) {
        profileDao.upsert(profileEntity)
    }

    suspend fun deleteMessage(messageId: String) {
        localMessageRepository.deleteByMessageId(messageId)
    }

    suspend fun updateChatItemsLastMessage(
        message: Message,
        incrementUnseenCount: Boolean,
        autoUpdateRelatedTable: Boolean = true
    ) {
        if (autoUpdateRelatedTable) {
            val chatItem = chatItemDao.getSingle(message.chatId)
            val profileId = chatItem?.profileId
            var profile = profileId?.let { profileDao.getSingle(it) }
            //check if chat item exists
            if (chatItem == null) {
                val result = chatRepository.getChatItemById(message.chatId)
                val newChatItem: ChatItem = if (result.isSuccess) {
                    result.getOrNull()!!
                } else {
                    Logger.e(result.exceptionOrNull()) { "Failure to autoUpdateProfilesTable, chatId: ${message.chatId}" }
                    return
                }
                insertChatItems(listOf(newChatItem))
                return
            } else if (profile == null) {
                //check if profile exists else update from remote profile
                val result = chatRepository.getProfileByChatID(message.chatId)
                if (result.isSuccess) {
                    val remoteProfile = result.getOrNull()!!
                    profileDao.upsert(remoteProfile.toProfileEntity())
                    localMessageRepository.addMessage(message)
                    if (incrementUnseenCount)
                        chatItemDao.incrementUnseenCount(message.chatId)
                    return
                } else {
                    Logger.e(result.exceptionOrNull()) { "Failure to autoUpdateProfilesTable, chatId: ${message.chatId}" }
                    return
                }
            } else {
                if (incrementUnseenCount) {
                    chatItemDao.incrementUnseenCount(message.chatId)
                }
                localMessageRepository.addMessage(message,true)
            }
        }
    }

    suspend fun exists(messageId: String): Boolean {
        return localMessageRepository.getMessageById(messageId) != null
    }

    suspend fun clearUnseenCount(chatId: String) {
        chatItemDao.clearUnseenCount(chatId)
    }

    suspend fun insertChatItems(chatItems: List<ChatItem>) {
        val chatItemEntities = chatItems.map { it.toChatItemEntity() }
        localMessageRepository.upsertMessages(chatItems.map { it.lastMessage })
        profileDao.upsertAll(chatItems.map { it.profile.toProfileEntity() })
        chatItemDao.upsert(chatItemEntities)//update message and profiles table first
        Logger.i("inserted chat items size: ${chatItems.size}")
    }

    suspend fun getProfileById(id: String): UserClass.RemoteUser {
        return profileDao.getSingle(id).toRemoteUser()
    }

    suspend fun getProfileByChatId(chatId: String): UserClass.RemoteUser? {
        return chatItemDao.getProfileByChatId(chatId)?.toRemoteUser()
    }

    suspend fun clearAll() {
        chatItemDao.clearAll()
    }
}