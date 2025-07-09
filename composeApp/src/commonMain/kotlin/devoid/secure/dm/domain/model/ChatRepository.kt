package devoid.secure.dm.domain.model

import io.github.jan.supabase.realtime.PostgresAction
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    suspend fun getChatItems(pageNumber :Int,pageSize:Int=10):Result<List<ChatItem>>
    suspend fun getProfileByChatID(chatId: String):Result<UserClass.RemoteUser>
    suspend fun getChatItemById(chatId: String):Result<ChatItem>
    suspend fun getMessagesAfterId(chatId:String, firstItemKey:String, pageCount:Int):Result<List<Message>>
    suspend fun getMessagesBeforeId(chatId:String, lastItemKey:String, pageCount:Int):Result<List<Message>>
    suspend fun sendMessage(message: Message):Result<Unit>
    suspend fun getChatIdFromUid(targetUid:String):Result<String>
    suspend fun subscribeToMessages(chatId: String):Flow<PostgresAction>?
    suspend fun unSubscribeToMessages():Result<Unit>
    suspend fun markAllMessagesAsSeen(chatId: String):Result<Unit>
    suspend fun deleteMessage(messageId: String): Result<Unit>
}