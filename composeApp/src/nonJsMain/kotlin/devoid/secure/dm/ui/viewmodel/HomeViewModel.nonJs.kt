package devoid.secure.dm.ui.viewmodel

import androidx.lifecycle.viewModelScope
import androidx.paging.map
import co.touchlab.kermit.Logger
import devoid.secure.dm.domain.toChatItem
import devoid.secure.dm.di.LocalChatItemsRepoHelper
import devoid.secure.dm.domain.AppNotificationManager
import devoid.secure.dm.domain.model.ChatRepository
import devoid.secure.dm.domain.model.FriendsRepository
import devoid.secure.dm.domain.model.Message
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.decodeIfNotEmptyOrDefault
import io.github.jan.supabase.realtime.PostgresAction
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@OptIn(SupabaseInternal::class)
actual class PSHomeViewModel actual constructor(
    friendsRepository: FriendsRepository,
    private val chatRepository: ChatRepository,
    private val notificationManager: AppNotificationManager,
    private val supabaseClient: SupabaseClient
) : HomeViewModel(friendsRepository, supabaseClient) {
    private val localChatItemRepository = LocalChatItemsRepoHelper().localChatItemRepository
    private val autoClearChatsOnLogout = true

    init {
        viewModelScope.launch {
            isCurrentChatActive.collect {
                if (it){
                    currentChatId.value?.let { chatId->
                        subscribeToMessages(chatId)
                        notificationManager.clearNotifications(chatId)
                    }
                }else{
                    unSubscribeToMessages()
                }
            }
        }

        if (autoClearChatsOnLogout) {
            viewModelScope.launch {
                supabaseClient.auth.sessionStatus.collect {
                    if (it is SessionStatus.NotAuthenticated) {
                        localChatItemRepository.clearAll()
                    }
                }
            }
        }
    }

    override fun subscribeToMessages(chatId: String) {
        viewModelScope.launch {
            try {
                chatRepository.subscribeToMessages(chatId = chatId)?.collect {
                    when (it) {
                        is PostgresAction.Delete -> {
                            val msg = it.oldRecord.decodeIfNotEmptyOrDefault<Message?>(null)
                            msg?.let { message ->
                                localChatItemRepository.deleteMessage(message.messageId)
                            }
                        }

                        is PostgresAction.Insert -> {
                            Logger.i(messageString = "inserted: $it.record", tag = "PSHomeViewModel")
                            val msg = it.record.decodeIfNotEmptyOrDefault<Message?>(null)
                            msg?.let { message ->
                                localChatItemRepository.updateChatItemsLastMessage(
                                    message,
                                    incrementUnseenCount = isCurrentChatActive.value && currentChatId.value != message.chatId
                                )
                                if (!isCurrentChatActive.value) {
                                    val profile = localChatItemRepository.getProfileById(message.senderId)
                                    notificationManager.showMsgNotification(profile = profile, message)
                                }
                            }
                        }

                        is PostgresAction.Select -> {
                        }

                        is PostgresAction.Update -> {
                            val msg = it.record.decodeIfNotEmptyOrDefault<Message?>(null)
                            msg?.let { message ->
                                localChatItemRepository.updateChatItemsLastMessage(message, false)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Logger.w(e) { "listenToMessages ERROR" }
            }
        }
    }

    override fun unSubscribeToMessages() {
        viewModelScope.launch {
            chatRepository.unSubscribeToMessages()
        }
    }

    override fun getChatItemsFlow(): Any =
        localChatItemRepository.getChatItemsPaged(pageSize).flow.map { it.map { it.toChatItem() } }

    override fun clearUnseenCount(chatId: String) {
        viewModelScope.launch {
            chatRepository.markAllMessagesAsSeen(chatId = chatId).onSuccess {
                localChatItemRepository.clearUnseenCount(chatId)
            }
        }
    }


}