package devoid.secure.dm.ui.compose

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update

sealed class LoadState(
    val endOfPaginationReached: Boolean
) {
    class NotLoading(
        endOfPaginationReached: Boolean
    ) : LoadState(endOfPaginationReached) {
        override fun toString(): String {
            return "NotLoading(endOfPaginationReached=$endOfPaginationReached)"
        }

        override fun equals(other: Any?): Boolean {
            return other is NotLoading &&
                    endOfPaginationReached == other.endOfPaginationReached
        }

        override fun hashCode(): Int {
            return endOfPaginationReached.hashCode()
        }

        internal companion object {
            internal val Complete = NotLoading(endOfPaginationReached = true)
            internal val Incomplete = NotLoading(endOfPaginationReached = false)
        }
    }

    object Loading : LoadState(false) {
        override fun toString(): String {
            return "Loading(endOfPaginationReached=$endOfPaginationReached)"
        }

        override fun equals(other: Any?): Boolean {
            return other is Loading &&
                    endOfPaginationReached == other.endOfPaginationReached
        }

        override fun hashCode(): Int {
            return endOfPaginationReached.hashCode()
        }
    }

    class Error(
        val error: Throwable
    ) : LoadState(false) {
        override fun equals(other: Any?): Boolean {
            return other is Error &&
                    endOfPaginationReached == other.endOfPaginationReached &&
                    error == other.error
        }

        override fun hashCode(): Int {
            return endOfPaginationReached.hashCode() + error.hashCode()
        }

        override fun toString(): String {
            return "Error(endOfPaginationReached=$endOfPaginationReached, error=$error)"
        }
    }
}


data class CombinedLoadStates(
    val refresh: LoadState,
    val prepend: LoadState,
    val append: LoadState,
)

class PagingItems<T>() {
    companion object {
        val defaultLoadState = CombinedLoadStates(
            refresh = LoadState.Loading,
            prepend = LoadState.NotLoading(false),
            append = LoadState.NotLoading(false),
        )
    }

    var items by mutableStateOf<List<T>>(emptyList())
    var loadState: CombinedLoadStates by mutableStateOf<CombinedLoadStates>(
        defaultLoadState
    )
}

suspend fun <T> PagingItems<T>.getPagedIntoState(
    state: MutableStateFlow<PagingItems<T>>,
    pageSize: Int,
    isRefreshRequest :Boolean,
    onFetchItems:suspend () -> Result<List<T>>
){
    if (isRefreshRequest) {
        state.update { it.apply { loadState = loadState.copy(refresh = LoadState.Loading) } }
    } else {
        state.update { it.apply { loadState = loadState.copy(append = LoadState.Loading) } }
    }
    onFetchItems().onSuccess { items ->
        state.update {
            if (isRefreshRequest) {
                it.apply {
                    it.items = items
                    loadState = loadState.copy(refresh = LoadState.NotLoading(items.size < pageSize))
                }
            } else {
                it.apply {
                    it.items += items
                    loadState = loadState.copy(append = LoadState.NotLoading(items.size < pageSize))
                }
            }
        }
    }.onFailure { error ->
        println("pagingError: $error")
        if (isRefreshRequest) {
            state.update {
                it.apply { loadState = loadState.copy(refresh = LoadState.Error(error)) }
            }
        } else {
            state.update {
                it.apply { loadState = loadState.copy(append = LoadState.Error(error)) }
            }
        }
    }
}


suspend fun watchListStateForPagination(
    listState: LazyListState,
    loadMoreThreshold: Int = 3,
    loadState: CombinedLoadStates,
    onLoadMore: () -> Unit
) {
    snapshotFlow {
        val layoutInfo = listState.layoutInfo
        val totalItemsCount = layoutInfo.totalItemsCount
        val lastVisibleItemIndex = (layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0) + 1
        lastVisibleItemIndex > 0 && totalItemsCount > 0 &&
                (totalItemsCount - lastVisibleItemIndex) <= loadMoreThreshold
    }.distinctUntilChanged() // Only react to actual changes
        .collect { shouldLoadMore ->
            if (shouldLoadMore && !loadState.append.endOfPaginationReached && loadState.append !is LoadState.Loading) {
                onLoadMore()
            }
        }
}