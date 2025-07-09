package devoid.secure.dm.data.local.quartz

import co.touchlab.kermit.Logger
import devoid.secure.dm.domain.files.SharedFileImpl
import devoid.secure.dm.domain.files.getExtentionFromName
import devoid.secure.dm.domain.model.AttachmentsRepository
import devoid.secure.dm.domain.model.ChatRepository
import devoid.secure.dm.domain.model.MessageAttachment
import devoid.secure.dm.domain.toAttachment
import devoid.secure.dm.domain.toMessage
import kotlinx.coroutines.runBlocking
import org.koin.core.context.GlobalContext
import org.quartz.Job
import org.quartz.JobDataMap
import org.quartz.JobExecutionContext
import org.quartz.JobExecutionException
import uuidV4

class MessageUploadJob : Job {
    private val TAG = "MessageUploadJob"
    private val attachmentsRepository = GlobalContext.get().get<AttachmentsRepository>()
    private val chatRepository = GlobalContext.get().get<ChatRepository>()
    override fun execute(context: JobExecutionContext?) {
        Logger.i(tag = TAG, messageString = "message upload worker started")
        val inputData = context?.jobDetail?.jobDataMap ?: kotlin.run {
            Logger.e(tag = TAG, messageString = "null input data received")
            return
        }
        runBlocking{
            try {
                val messageAttachment = if (inputData.containsKey("attachment_id")) {
                    val remoteAttachment = uploadAttachment(inputData)
                        ?: //                    return if failed to upload attachment
                        throw JobExecutionException(Exception("failed to upload attachment"))
                    remoteAttachment
                } else null
                var message = inputData.toMessage()
                messageAttachment?.run {
                    message = message.copy(attachment = this)
                }
                chatRepository.sendMessage(message)
            }catch (e: Exception){
                Logger.e(tag = TAG, throwable = e, messageString = "failed to send a message")
                throw e
            }
        }
    }

    private suspend fun uploadAttachment(inputData: JobDataMap): MessageAttachment? {
        val attachment = inputData.toAttachment()
        val sharedFile = SharedFileImpl(attachment.fileUri)
        val chatId = inputData.getString("chat_id")
        if (sharedFile.getFileSize() / 1024 / 1024 > 50) {//size limit 50 mb
            Logger.e(
                tag = TAG,
                throwable = IllegalArgumentException("The file size is greater then max file size 50mb"),
                messageString = "failed to upload attachment"
            )
            return null
        }
        val data = sharedFile.toByteArray() ?: return null
        val remoteFileName = "${uuidV4()}${getExtentionFromName(attachment.name)}"
        val uploadFileResult = attachmentsRepository.uploadFile(
            name = remoteFileName,
            chatId,
            attachmentType = attachment.type,
            data = data
        ).onFailure {
            return null
        }.onSuccess {
        }
        if (uploadFileResult.isSuccess) {
            val remoteFileUrlRequest =
                attachmentsRepository.getUrl(name = remoteFileName, chatId, attachmentType = attachment.type)
            if (remoteFileUrlRequest.isSuccess) {
                val remoteAttachment = attachment.copy(fileUri = remoteFileUrlRequest.getOrNull() ?: return null)
                return remoteAttachment
            }
        }
        return null
    }
}