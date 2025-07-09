package devoid.secure.dm.domain

import devoid.secure.dm.domain.model.Message
import devoid.secure.dm.domain.model.UserClass

interface AppNotificationManager {
    fun showMsgNotification(profile:UserClass.RemoteUser,message: Message)
    fun clearNotifications(chatId:String)
}