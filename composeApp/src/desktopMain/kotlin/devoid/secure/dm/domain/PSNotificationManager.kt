package devoid.secure.dm.domain

import devoid.secure.dm.domain.model.Message
import devoid.secure.dm.domain.model.UserClass

class PSNotificationManager:AppNotificationManager {
    override fun showMsgNotification(profile: UserClass.RemoteUser, message: Message) {

    }

    override fun clearNotifications(chatId: String) {

    }
}