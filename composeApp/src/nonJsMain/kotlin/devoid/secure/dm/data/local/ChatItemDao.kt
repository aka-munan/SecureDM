package devoid.secure.dm.data.local

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import devoid.secure.dm.domain.model.ChatItemEntity
import devoid.secure.dm.domain.model.LocalChatItem
import devoid.secure.dm.domain.model.ProfileEntity

@Dao
interface ChatItemDao {
    @Upsert
    suspend fun upsert(item: ChatItemEntity)

    @Upsert
    suspend fun upsert(item: List<ChatItemEntity>)

    @Query("SELECT * FROM chats_entity WHERE chatId = :chatId")
    suspend fun getSingle(chatId: String): ChatItemEntity?

    @Query(
        "SELECT p.* FROM chats_entity ci " +
                "JOIN profiles p ON p.id = ci.profileId " +
                "WHERE ci.chatId = :chatId"
    )
    suspend fun getProfileByChatId(chatId: String): ProfileEntity?

    @Query("DELETE FROM chats_entity WHERE chatId = :chatId")
    suspend fun delete(chatId: String)

    @Query(
        "SELECT ci.chatId, m.messageId AS lastMessageId, ci.profileId, ci.unseenCount " +
                ",m.*" +
//                ", m.messageId as sub_messageId, m.senderId as sub_senderId, m.chatId as sub_chatId, m.text as sub_text, m.synced as sub_synced, m.seen as sub_seen, m.date as sub_date, m.attachmentId " +
                ", at.id as at_id,at.messageId as at_messageId, at.fileUri as at_fileUri, at.name as at_name,at.size as at_size, at.duration as at_duration, at.type as at_type " +
                "FROM chats_entity ci " +
                "JOIN message_entity m ON m.messageId = (SELECT messageId FROM message_entity WHERE chatId = ci.chatId ORDER BY date DESC LIMIT 1) " +
                "left Join attachments at On at.id = m.attachmentId " +
                "ORDER BY m.date DESC"
    )
    fun pagingSource(): PagingSource<Int, LocalChatItem>

    @Query(
        "UPDATE chats_entity SET unseenCount = 0 " +
                "WHERE chatId = :chatId"
    )
    suspend fun clearUnseenCount(chatId: String)

    @Query(
        "UPDATE chats_entity SET unseenCount = unseenCount + 1 " +
                "WHERE chatId = :chatId"
    )
    suspend fun incrementUnseenCount(chatId: String)

    @Query("DELETE FROM chats_entity")
    suspend fun clearAll()
}