package devoid.secure.dm.data.remote

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import co.touchlab.kermit.Logger
import devoid.secure.dm.data.local.LocalMessageRepository
import devoid.secure.dm.domain.model.LocalMessage
import devoid.secure.dm.domain.model.ChatRepository
import kotlinx.coroutines.delay
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalPagingApi::class)
class MessageRemoteMediator(
    private val localMessageRepository: LocalMessageRepository,
    private val chatRepository: ChatRepository,
    private val chatId: String
) : RemoteMediator<Int, LocalMessage>() {
    @OptIn(ExperimentalTime::class)
    override suspend fun load(loadType: LoadType, state: PagingState<Int, LocalMessage>): MediatorResult {
        return try {
            val startTime = Clock.System.now().toEpochMilliseconds()
            val loadKey = when (loadType) {
                LoadType.REFRESH -> {
                    println("refresh")
                    ""
                }

                LoadType.PREPEND -> {
                    return MediatorResult.Success(true)
                }

                LoadType.APPEND -> {
                    val lastItem = state.lastItemOrNull()
                    lastItem?.messageId ?: ""
                }
            }
            val result =
                    chatRepository.getMessagesBeforeId(
                        chatId,
                        lastItemKey = loadKey,
                        if (loadType == LoadType.REFRESH)
                            20
                        else
                            state.config.pageSize
                    )
            result.onSuccess { messages ->
                if (loadType == LoadType.APPEND) {
                    val timeDiff = startTime - Clock.System.now().toEpochMilliseconds()
                    if (timeDiff < 2_000) delay(1_500 - timeDiff)//minimum 1.5 sec time difference for loading widget
                }
                if (messages.isNotEmpty() && loadType == LoadType.REFRESH) {
                    localMessageRepository.clearAndInsert(messages)
                } else {
                    localMessageRepository.upsertMessages(messages)
                }
                return MediatorResult.Success(endOfPaginationReached = messages.size < state.config.pageSize)
            }
            throw result.exceptionOrNull()!!
        } catch (e: Exception) {
            Logger.e(e){"message mediator error"}
            MediatorResult.Error(e)
        }
    }

}