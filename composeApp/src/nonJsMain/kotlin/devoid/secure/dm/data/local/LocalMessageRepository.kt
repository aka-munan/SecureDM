package devoid.secure.dm.data.local

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import devoid.secure.dm.data.remote.MessageRemoteMediator
import devoid.secure.dm.domain.model.LocalMessage
import devoid.secure.dm.domain.toMessageEntity
import devoid.secure.dm.domain.model.MessageEntity
import devoid.secure.dm.domain.model.ChatRepository
import devoid.secure.dm.domain.model.Message
import devoid.secure.dm.domain.toAttachmentsEntity

class LocalMessageRepository(
    private val appDatabase: AppDatabase,
    private val chatRepository: ChatRepository,
    private val attachmentDao: MessageAttachmentDao
) {

    @OptIn(ExperimentalPagingApi::class)
    fun getMessages(chatId: String): Pager<Int, LocalMessage> {
        return Pager(
            config = PagingConfig(pageSize = 20, prefetchDistance = 2),
            remoteMediator = MessageRemoteMediator(this, chatRepository, chatId = chatId),
            pagingSourceFactory = { appDatabase.messageDao.pagingSource(chatId) }
        )

    }
    suspend fun getMessageById(id:String): MessageEntity?{
      return  appDatabase.messageDao.getById(id)
    }

    suspend fun addMessage(message: Message,synced:Boolean = false) {
        if (message.attachment!=null){
            attachmentDao.upsert(message.attachment.toAttachmentsEntity())
        }
        appDatabase.messageDao.upsert(message.toMessageEntity(synced))
    }

    suspend fun markAsSynced(messageId: String) {
        appDatabase.messageDao.setSyncedStatus(true, messageId = messageId)
    }
    suspend fun clearAndInsert(messages: List<Message>,synced: Boolean = true){
        val attachments = messages.mapNotNull { it.attachment?.toAttachmentsEntity() }
        appDatabase.attachmentDao.clearAll()
        appDatabase.attachmentDao.upsertAll(attachments)
        val repliedMessages = messages.map { it.replyTo }.mapNotNull { it?.toMessageEntity(synced=synced) }
        var messageEntities = messages.map { it.toMessageEntity(synced = synced)}
        messageEntities = messageEntities.plus(repliedMessages)
        appDatabase.messageDao.clearAndInsert(messageEntities)
    }

    suspend fun deleteByMessageId(messageId: String) {
        appDatabase.messageDao.delete(messageId = messageId)
    }

    suspend fun upsertMessages(messages: List<Message>,synced: Boolean = true){
        val attachments = messages.mapNotNull { it.attachment?.toAttachmentsEntity() }
        appDatabase.attachmentDao.upsertAll(attachments)
        val messageEntities = messages.map { it.toMessageEntity().copy(synced = synced) }
        appDatabase.messageDao.upsertAll(messageEntities)
    }
}