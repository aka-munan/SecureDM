package devoid.secure.dm.domain

import co.touchlab.kermit.Logger
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import devoid.secure.dm.data.local.LocalChatItemRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class FirebaseNotificationService() : FirebaseMessagingService() {
    private val notificationManager: AppNotificationManager by inject()
    private val localChatItemRepository: LocalChatItemRepository by inject()
    private val scope = CoroutineScope(Dispatchers.IO)
    override fun onNewToken(token: String) {
        Logger.i("new fcm token generated")
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Logger.i("new fcm message received: ${remoteMessage.data} ")
        if (remoteMessage.data["msg_type"] == RemoteMessageType.CHAT_MESSAGE.name) {
            val message = remoteMessage.toMessage()
            scope.launch {
                if (!localChatItemRepository.exists(message.messageId)){
                    localChatItemRepository.updateChatItemsLastMessage(message, true)
                    val profile = localChatItemRepository.getProfileById(message.senderId)
                    notificationManager.showMsgNotification(profile, message)
                }
            }
        }

    }

}

enum class RemoteMessageType {
    CHAT_MESSAGE
}