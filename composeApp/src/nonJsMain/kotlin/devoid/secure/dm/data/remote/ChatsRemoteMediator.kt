package devoid.secure.dm.data.remote

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import devoid.secure.dm.data.local.LocalChatItemRepository
import devoid.secure.dm.domain.model.LocalChatItem
import devoid.secure.dm.domain.model.ChatRepository

@OptIn(ExperimentalPagingApi::class)
class ChatsRemoteMediator(
    private val localChatItemRepository: LocalChatItemRepository,
    private val chatRepository: ChatRepository
) : RemoteMediator<Int, LocalChatItem>() {
    protected val TAG = "ChatsRemoteMediator"

    override suspend fun load(loadType: LoadType, state: PagingState<Int, LocalChatItem>): MediatorResult {
        return try {
            val pageNumber = when (loadType) {
                LoadType.REFRESH -> {
//                    Logger.i(tag = TAG, messageString = "REFRESH")
                    1
                }

                LoadType.PREPEND -> {
//                    Logger.i(tag = TAG, messageString = "PREPEND")
                    return MediatorResult.Success(true)
                }

                LoadType.APPEND -> {
//                    Logger.i(tag = TAG, messageString = "APPEND")
                    val pageNumber = state.pages.size + 1
                    pageNumber
                }
            }
            val result = chatRepository.getChatItems(pageNumber = pageNumber, pageSize = state.config.pageSize)
            result.onSuccess { chatItems ->
                localChatItemRepository.insertChatItems(chatItems)
                return MediatorResult.Success(endOfPaginationReached = chatItems.size < state.config.pageSize)
            }
            return MediatorResult.Error(result.exceptionOrNull()!!)
        } catch (e: Exception) {
            MediatorResult.Error(e)
        }
    }

}