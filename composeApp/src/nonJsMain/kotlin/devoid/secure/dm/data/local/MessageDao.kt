package devoid.secure.dm.data.local

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import devoid.secure.dm.domain.model.LocalMessage
import devoid.secure.dm.domain.model.MessageEntity

@Dao
interface MessageDao {
    @Upsert
    suspend fun upsertAll(messages: List<MessageEntity>)

    @Upsert
    suspend fun upsert(message: MessageEntity)

    @Query(
        "SELECT m.*,m.replyTo as sub_replyTo, at.id as at_id,at.messageId as at_messageId, at.fileUri as at_fileUri, at.name as at_name,at.size as at_size, at.duration as at_duration, at.type as at_type  FROM message_entity m " +
                "left Join attachments at On at.id = m.attachmentId " +
                "WHERE (m.chatId = :chatId) " +
                " ORDER BY date DESC"
    )
    fun pagingSource(chatId: String): PagingSource<Int, LocalMessage>

    @Query("DELETE FROM message_entity")
    suspend fun clearAll()

    @Query("SELECT * FROM message_entity " +
            "WHERE messageId = :messageId")
    suspend fun getById(messageId: String): MessageEntity?

    @Query("UPDATE message_entity SET synced = :synced " +
            "WHERE messageid = :messageId" )
    suspend fun setSyncedStatus(synced:Boolean,messageId: String)

    @Query("DELETE FROM message_entity " +
            "WHERE messageid = :messageId" )
    suspend fun delete(messageId:String)

    @Query("SELECT * FROM message_entity ORDER BY date DESC")
    suspend fun getAll():List<MessageEntity>

    @Transaction
    suspend fun clearAndInsert(messages: List<MessageEntity>) {
        clearAll()
        upsertAll(messages)
    }
}