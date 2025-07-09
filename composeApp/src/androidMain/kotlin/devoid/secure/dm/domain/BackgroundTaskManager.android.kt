package devoid.secure.dm.domain

import android.content.Context
import androidx.lifecycle.asFlow
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.work.*
import co.touchlab.kermit.Logger
import devoid.secure.dm.data.local.workmanager.AttachmentUploadWorker
import devoid.secure.dm.data.local.workmanager.MessageUploadWorker
import devoid.secure.dm.domain.model.Message
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import org.koin.core.context.GlobalContext
import java.util.*

actual class MessageUploadTask actual constructor(private val taskId: String): Task<Message> {
    override val id: String
        get() = taskId
    private val context = GlobalContext.get().get<Context>()
    private val workManager = WorkManager.getInstance(context)

    //getting result.success() from this function doesn't mean the whole request is successful . it still needs do do network operations.
    override suspend fun onRun(input: Message): Flow<TaskState> {
        return try {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val messageRequest = OneTimeWorkRequestBuilder<MessageUploadWorker>()
                .setId(UUID.fromString(input.messageId))
                .setConstraints(constraints)
                .setInputData(input.toWorkData())
                .build()
           if (input.attachment != null) {
                val attachmentRequest = OneTimeWorkRequestBuilder<AttachmentUploadWorker>()
                    .setConstraints(constraints)
                    .setInputData(input.attachment.toWorkData(input.chatId))
                    .build()
                workManager.beginUniqueWork(
                    uniqueWorkName = input.messageId,
                    ExistingWorkPolicy.REPLACE,
                    request = attachmentRequest
                )
                    .then(messageRequest)
                    .enqueue()
            } else {
                workManager.beginUniqueWork(
                    uniqueWorkName = input.messageId,
                    ExistingWorkPolicy.REPLACE,
                    request = messageRequest
                ).enqueue()
            }
            workManager.getWorkInfosForUniqueWorkFlow(input.messageId).map {
                it.firstOrNull { it.id.toString() == input.messageId }?.toTaskState()?:TaskState.IN_PROGRESS
            }
        } catch (e: Exception) {
            flow<TaskState> { emit(TaskState.FAILURE(e)) }
        }
    }
}

fun WorkInfo.toTaskState():TaskState{
    return when(state){
        WorkInfo.State.SUCCEEDED ->TaskState.SUCCESS
        WorkInfo.State.FAILED ->TaskState.FAILURE(Exception("Worker completed with failure state."))
        else-> TaskState.IN_PROGRESS
    }
}