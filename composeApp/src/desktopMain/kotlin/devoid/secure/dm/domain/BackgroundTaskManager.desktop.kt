package devoid.secure.dm.domain

import co.touchlab.kermit.Logger
import devoid.secure.dm.data.local.LocalMessageRepository
import devoid.secure.dm.data.local.quartz.MessageUploadJob
import devoid.secure.dm.domain.model.Message
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.koin.core.context.GlobalContext
import org.quartz.*
import org.quartz.impl.StdSchedulerFactory

actual class MessageUploadTask actual constructor(taskId: String) : Task<Message> {
    //task id should be message id
    override val id: String = taskId
    private val scheduler = StdSchedulerFactory.getDefaultScheduler().apply {
        start()
    }
    private val localMessageRepository  = GlobalContext.get().get<LocalMessageRepository>()

    override suspend fun onRun(input: Message): Flow<TaskState> {
        return try {
            flow {
                emit(TaskState.IN_PROGRESS)
                val messageUploadJob = JobBuilder.newJob(MessageUploadJob::class.java)
                    .setJobData(input.toJobDataMap())
                    .withIdentity(id)
                    .build()
                Logger.i { "job data map: ${input.toJobDataMap().wrappedMap}"}
                Logger.i { "job data toi message: ${input.toJobDataMap().toMessage()}"}
                val trigger = TriggerBuilder.newTrigger()
                    .withIdentity(id)
                    .startNow()
                    .build()
                scheduler.scheduleJob(messageUploadJob, trigger)
                scheduler.listenerManager.addJobListener(object : JobListener {
                    override fun getName(): String = "oneTimeListener"

                    override fun jobToBeExecuted(context: JobExecutionContext?) {}

                    override fun jobExecutionVetoed(context: JobExecutionContext?) {}

                    override fun jobWasExecuted(context: JobExecutionContext?, jobException: JobExecutionException?) {
                        if (context?.jobDetail?.key?.name == id) {
                            runBlocking {
                                if (jobException != null) {
                                    localMessageRepository.deleteByMessageId(id)
                                    emit(TaskState.FAILURE(jobException))
                                } else {
                                    localMessageRepository.markAsSynced(id)
                                    emit(TaskState.SUCCESS)
                                }
                            }
                        }
                    }

                })
            }
        } catch (e: Exception) {
            flowOf(TaskState.FAILURE(e))
        }
    }

}