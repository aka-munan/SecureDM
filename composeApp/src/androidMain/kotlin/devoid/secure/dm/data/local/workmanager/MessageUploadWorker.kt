package devoid.secure.dm.data.local.workmanager

import android.app.Notification
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import co.touchlab.kermit.Logger
import devoid.secure.dm.domain.model.AttachmentType
import devoid.secure.dm.domain.model.ChatRepository
import devoid.secure.dm.domain.model.MessageAttachment
import devoid.secure.dm.domain.toMessage
import org.koin.core.context.GlobalContext

class MessageUploadWorker(private val context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {
    private val CHANNEL_ID = "uploadWorNotification"
    private val NOTIFICATION_ID = 11
    private val chatRepository = GlobalContext.get().get<ChatRepository>()
    override suspend fun doWork(): Result {
        return try {
            Logger.i("message upload worker started \n message: ${inputData.keyValueMap}")
            val attachmentUrl = inputData.getString("attachment_url")
            var message = inputData.keyValueMap.toMessage()
            message = attachmentUrl?.let {
                val attachmentType = inputData.getString("attachment_type") ?: return Result.failure()
                val attachmentName = inputData.getString("attachment_name") ?: return Result.failure()
                val attachmentDuration = inputData.getInt("attachment_duration", -1)
                val attachmentSize = inputData.getLong("attachment_size", 0)
                val attachment = MessageAttachment(
                    id = "",
                    messageId = message.messageId,
                    fileUri = it,
                    name = attachmentName,
                    size = attachmentSize,
                    duration = attachmentDuration.takeIf { it != -1 },
                    type = AttachmentType.valueOf(attachmentType)
                )
                message.copy(attachment = attachment)
            } ?: message
            val sendMesageResult = chatRepository.sendMessage(message)
            return if (sendMesageResult.isSuccess) {
                Result.success()
            } else {
                Logger.e(sendMesageResult.exceptionOrNull()) {
                    "failed to post message"
                }
                Result.failure()
            }
        } catch (e: Exception) {
            Logger.e(e) {
                "failed to post message"
            }
            Result.failure()
        }
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return ForegroundInfo(NOTIFICATION_ID, createNotification())
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Sync messages")
            .setContentText("Sync messages to cloud.")
            .build()
    }
}