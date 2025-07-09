package devoid.secure.dm.domain

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.core.app.NotificationCompat
import androidx.core.app.Person
import androidx.core.app.RemoteInput
import androidx.core.app.TaskStackBuilder
import androidx.core.graphics.drawable.IconCompat
import androidx.core.net.toUri
import co.touchlab.kermit.Logger
import devoid.secure.dm.MainActivity
import devoid.secure.dm.R
import devoid.secure.dm.data.remote.utils.BitmapUtils
import devoid.secure.dm.domain.model.Message
import devoid.secure.dm.domain.model.UserClass
import devoid.secure.dm.ui.compose.getLabelFromMessage
import devoid.secure.dm.ui.navigation.APP_URI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import androidx.core.graphics.createBitmap
import kotlinx.coroutines.withContext

const val iconSize = 256

class PSNotificationManager(private val context: Context) : AppNotificationManager {
    private val MSG_CHANNEL_ID = "msgChannel"
    private val GROUP_CHAT = "groupChat"
    private val msgNotificationChannel =
        NotificationChannel(MSG_CHANNEL_ID, "Message", NotificationManager.IMPORTANCE_HIGH).apply {
            description = "Chat specific notifications"
        }
    private var notificationManager: NotificationManager = context.getSystemService(NotificationManager::class.java)

    init {
        notificationManager.createNotificationChannel(msgNotificationChannel)
    }

    @SuppressLint("RestrictedApi")
    override fun showMsgNotification(profile: UserClass.RemoteUser, message: Message) {
        try {
                CoroutineScope(Dispatchers.IO).launch {
                   val avtar = if (profile.avatarUrl==null) null else  BitmapUtils.getBitmapFromUrl(profile.avatarUrl, size = iconSize, context).getOrNull()
                    val person = Person.Builder()
                        .setName(profile.fullName)
                        .setIcon(IconCompat.createWithBitmap(avtar?: createBitmap(
                            iconSize,
                            iconSize,
                            Bitmap.Config.RGB_565
                        )))
                        .build()
                    withContext(Dispatchers.Main){
                        val notificationId = message.chatId.hashCode()
                        val oldNotification =
                            notificationManager.activeNotifications.lastOrNull { it.id == notificationId }?.notification

                        val messageStyle =
                            oldNotification?.let { NotificationCompat.MessagingStyle.extractStyleFromNotification(it) as NotificationCompat.MessagingStyle }
                                ?: NotificationCompat.MessagingStyle(profile.fullName ?: "person")
                        messageStyle.addMessage(
                            getLabelFromMessage(message),
                            Instant.parse(message.date).toEpochMilliseconds(),
                            person
                        )
                        val notification = NotificationCompat.Builder(context, MSG_CHANNEL_ID)
                            .setContentTitle(profile.fullName)
                            .setContentText(getLabelFromMessage(message))
                            .setSmallIcon(R.mipmap.ic_launcher_round)
                            .setStyle(messageStyle)
                            .setGroup(GROUP_CHAT)
//                .addAction(createReplyAction(message.chatId))
                            .setContentIntent(createContentIntent(message.chatId))
                            .setAutoCancel(true)
                            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                            .build()
                        notificationManager.notify(notificationId, notification)

                    }
                }

        } catch (e: Exception) {
            Logger.e(e) { "failed to notify user about a remote notification" }
        }
    }

    fun createReplyAction(chatId: String): NotificationCompat.Action {
        val remoteInput = RemoteInput.Builder("reply_text")
            .setLabel("reply")
            .build()
        val deepLinkIntent = Intent(
            Intent.ACTION_VIEW,
            "$APP_URI/chat/$chatId".toUri(),
            context,
            MainActivity::class.java
        )

        val deepLinkPendingIntent = TaskStackBuilder.create(context).run {
            addNextIntentWithParentStack(deepLinkIntent)
            getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        return NotificationCompat.Action.Builder(null, "Reply", deepLinkPendingIntent)
            .addRemoteInput(remoteInput)
            .setAllowGeneratedReplies(true)
            .build()
    }

    private fun createContentIntent(chatId: String): PendingIntent? {
        val deepLinkIntent = Intent(
            Intent.ACTION_VIEW,
            "$APP_URI/chat/$chatId".toUri(),
            context,
            MainActivity::class.java
        )

        return TaskStackBuilder.create(context).run {
            addNextIntentWithParentStack(deepLinkIntent)
            getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        }
    }

    override fun clearNotifications(chatId: String) {
        val notificationId = chatId.hashCode()
        notificationManager.cancel(notificationId)
    }
}