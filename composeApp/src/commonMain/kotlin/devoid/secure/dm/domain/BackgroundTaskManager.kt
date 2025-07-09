package devoid.secure.dm.domain

import devoid.secure.dm.di.getIOCommonDispatcher
import devoid.secure.dm.domain.model.Message
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class BackgroundTaskManager {
    val scope = CoroutineScope(getIOCommonDispatcher())
    private val _taskFlow = MutableStateFlow<TaskCompletedState?>(null)
    val completedTaskFlow = _taskFlow.asStateFlow()
    fun <T>runTask(task: Task<T>,input:T){
        scope.launch {
            task.onRun(input).collect{
                when(it){
                    TaskState.IN_PROGRESS -> {}
                    else -> {
                        _taskFlow.emit(TaskCompletedState(id = task.id,it))
                    }
                }
            }
        }
    }
}

interface Task<T>{
    val id:String
    suspend fun onRun(input : T):Flow<TaskState>
}
expect class MessageUploadTask(taskId:String):Task<Message>

sealed class TaskState{
    data object  IN_PROGRESS:TaskState()
    data object SUCCESS:TaskState()
    data class FAILURE(val throwable: Throwable):TaskState()
}

data class TaskCompletedState(
    val id: String,
    val taskState: TaskState
)