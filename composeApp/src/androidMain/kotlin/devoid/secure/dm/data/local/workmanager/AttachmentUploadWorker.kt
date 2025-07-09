package devoid.secure.dm.data.local.workmanager

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import co.touchlab.kermit.Logger
import devoid.secure.dm.domain.files.SharedFileImpl
import devoid.secure.dm.domain.files.getExtentionFromName
import devoid.secure.dm.domain.model.AttachmentsRepository
import devoid.secure.dm.domain.toAttachment
import org.koin.core.context.GlobalContext
import uuidV4

class AttachmentUploadWorker(private val context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {
    private val attachmentsRepository = GlobalContext.get().get<AttachmentsRepository>()
    override suspend fun doWork(): Result {
        val attachment = inputData.keyValueMap.toAttachment()
        val sharedFile = SharedFileImpl(context.contentResolver, attachment.fileUri)
        val chatId = inputData.getString("chat_id") ?: return Result.failure()
        if (sharedFile.getFileSize() / 1024 / 1024 > 50) {//size limit 50 mb
            return Result.failure()
        }
        val data = sharedFile.toByteArray() ?: return Result.failure()
        val remoteFileName = "${uuidV4()}${getExtentionFromName(attachment.name)}"
        val result = attachmentsRepository.uploadFile(
            name = remoteFileName,
            chatId,
            attachmentType = attachment.type,
            data = data
        ).onFailure {
            Logger.e(it) { "failed to upload attachment" }
            return Result.failure()
        }
        return if (result.isSuccess) {
            val remoteFileUrlRequest =
                attachmentsRepository.getUrl(name = remoteFileName, chatId, attachmentType = attachment.type)
            remoteFileUrlRequest.onFailure {
                Logger.e(it) { "failed to upload attachment" }
            }
            if (remoteFileUrlRequest.isSuccess){
                val output = workDataOf(
                    "attachment_url" to (remoteFileUrlRequest.getOrNull()),
                    "attachment_type" to attachment.type.name,
                    "attachment_name" to attachment.name,
                    "attachment_duration" to attachment.duration,
                    "attachment_size" to attachment.size
                )
                Result.success(output)
            }else{
                Result.failure()
            }
        } else
            Result.failure()
    }
}