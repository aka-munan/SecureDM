package devoid.secure.dm.data

import co.touchlab.kermit.Logger
import devoid.secure.dm.domain.model.ChatItem
import devoid.secure.dm.domain.model.ChatRepository
import devoid.secure.dm.domain.model.Message
import devoid.secure.dm.domain.model.UserClass
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class ChatRepoImpl(private val supabaseClient: SupabaseClient, private val scope: CoroutineScope) : ChatRepository {
    private val TAG = "ChatRepoImpl"

    override suspend fun getChatItems(pageNumber: Int, pageSize: Int): Result<List<ChatItem>> {
        return try {
            val request = supabaseClient.postgrest.rpc("get_chat_items", parameters = buildJsonObject {
                put("p_page_number", pageNumber)
                put("p_page_size", pageSize)
            })
            Logger.i(tag = TAG, messageString = "chatItems: ${request.data}")
            val result = request.decodeList<ChatItem>()
            Result.success(result)
        } catch (e: Exception) {
            Logger.e(e, tag = TAG) { "Get chat items error" }
            Result.failure(e)
        }
    }

    override suspend fun getMessagesAfterId(
        chatId: String,
        firstItemKey: String,
        pageCount: Int
    ): Result<List<Message>> {
        val currentUser = supabaseClient.auth.currentUserOrNull()
        return try {
            currentUser?.let { user ->
                val request = supabaseClient.postgrest.rpc("get_message_after_id", parameters = buildJsonObject {
                    put("p_chat_id", chatId)
                    put("p_limit", pageCount)
                    put("p_first_id", firstItemKey.ifBlank { null })
                })
                val result = request.decodeList<Message>()
                Result.success(result)
            } ?: Result.failure(IllegalStateException("Current User not found"))
        } catch (e: Exception) {
            Logger.e(tag = TAG,e) { "getMessagesAfterId error" }
            Result.failure(e)
        }
    }

    override suspend fun subscribeToMessages(chatId: String): Flow<PostgresAction> {
        val currentUser = supabaseClient.auth.currentUserOrNull()
        return currentUser?.let { user ->
            val channel = supabaseClient.realtime.channel("new-messages")
            val messageChangesFlow = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
                table = "messages"
                filter("chat_id", FilterOperator.EQ, chatId)
                filter("sender_id", operator = FilterOperator.NEQ, value = user.id)
            }
            channel.subscribe()
            messageChangesFlow
        } ?: throw IllegalStateException("got null user while trying to subscribe to messages")
    }

    override suspend fun unSubscribeToMessages(): Result<Unit> {
        return try {
            val channel = supabaseClient.realtime.channel("new-messages")
            channel.unsubscribe()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getMessagesBeforeId(
        chatId: String,
        lastItemKey: String,
        pageCount: Int
    ): Result<List<Message>> {
        return try {
            val request = supabaseClient.postgrest.rpc("get_message_before_id", parameters = buildJsonObject {
                put("p_chat_id", chatId)
                put("p_limit", pageCount)
                put("p_last_id", lastItemKey.ifBlank { null })
            })
            val result = request.decodeList<Message>()
            Result.success(result)
        } catch (e: Exception) {
            Logger.e(tag = TAG,e) { "getMessagesBeforeId error" }
            Result.failure(e)
        }
    }

    override suspend fun sendMessage(message: Message): Result<Unit> {
        return try {
            supabaseClient.postgrest.rpc("send_message_to_chat", parameters = message.toJsonObject())
            Logger.i(tag = TAG, messageString = "posting message: ${message.toJsonObject()}")
            Result.success(Unit)
        } catch (e: Exception) {
            Logger.e(tag = TAG,e) { "Send message error" }
            Result.failure(e)
        }
    }

    override suspend fun getChatIdFromUid(targetUid: String): Result<String> {
        return try {
            val request = supabaseClient.postgrest.rpc("get_chat_id_for_uid", parameters = buildJsonObject {
                put("p_target_uid", targetUid)
            })
            val result = request.decodeSingle<Map<String, String>>()
            Result.success(result["chat_id"] ?: "")
        } catch (e: Exception) {
            Logger.e(tag = TAG,e) { "getChatIdFromUid Error" }
            Result.failure(e)
        }
    }

    override suspend fun markAllMessagesAsSeen(chatId: String): Result<Unit> {
        val currentUser = supabaseClient.auth.currentUserOrNull()
        return try {
            currentUser?.let { user ->
                supabaseClient.from("messages").update(update = {
                    set("seen", true)
                }) {
                    filter {
                        and {
                            eq("chat_id", chatId)
                            neq("sender_id", user.id)
                        }
                    }
                }
            } ?: throw IllegalStateException("Current User Not Found")
            Result.success(Unit)
        } catch (e: Exception) {
            Logger.e(tag = TAG,e) { "Failure to mark messages as seen, chatId: $chatId" }
            Result.failure(e)
        }
    }

    override suspend fun deleteMessage(messageId: String): Result<Unit> {
       return try {
            supabaseClient.postgrest.from("messages").delete {
                filter {
                    eq("messageid", messageId)
                }
            }
           Result.success(Unit)
        }catch (e: Exception){
           Result.failure(e)
        }
    }

    override suspend fun getProfileByChatID(chatId: String): Result<UserClass.RemoteUser> {
        return try {
            val request = supabaseClient.postgrest.rpc("get_profile_by_chat_id", parameters = buildJsonObject {
                put("p_chat_id", chatId)
            })
            val result = request.decodeSingle<UserClass.RemoteUser>()
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getChatItemById(chatId: String): Result<ChatItem> {
        return try {
            val request = supabaseClient.postgrest.rpc("get_chatitem_by_id", parameters = buildJsonObject {
                put("p_chat_id", chatId)
            })
            val result = request.decodeSingle<ChatItem>()
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}