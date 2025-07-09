package devoid.secure.dm.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import devoid.secure.dm.domain.model.FriendRequest
import devoid.secure.dm.domain.model.FriendRequestStatus
import devoid.secure.dm.domain.model.FriendsRepository
import devoid.secure.dm.ui.compose.PagingItems
import devoid.secure.dm.ui.compose.getPagedIntoState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FriendRequestViewModel(private val friendsRepository: FriendsRepository) : ViewModel() {
     val pageSize = 20
    private val _friendRequests = MutableStateFlow(PagingItems<FriendRequest>())
    val friendRequests = _friendRequests.asStateFlow()


    fun getFriendRequests(pageNo: Int = 1) {
        viewModelScope.launch {
            _friendRequests.value.getPagedIntoState(_friendRequests, isRefreshRequest = pageNo==1, pageSize = pageSize){
                friendsRepository.getFriendRequests(pageSize = pageSize, pageNo = pageNo)
            }
        }
    }

    fun acceptFriendRequest(chatId: String,targetUid:String) {
        viewModelScope.launch {
            friendsRepository.setFriendRequestStatus(chatId = chatId, targetUid = targetUid, status = FriendRequestStatus.ACCEPTED)
                .onSuccess {
                    _friendRequests.update {
                        it.apply {
                         items = items.filterNot { it.chatID == chatId && it.user.id == targetUid }
                        }
                    }
                }.onFailure {
                    println("acceptFriendRequestError: $it")
                }
        }
    }

    fun rejectFriendRequest(chatId: String,targetUid:String) {
        viewModelScope.launch {
            friendsRepository.setFriendRequestStatus(chatId=chatId,targetUid = targetUid, status = FriendRequestStatus.REJECTED)
                .onSuccess {
                    _friendRequests.update {
                        it.apply {
                            items = items.filterNot { it.chatID == chatId && it.user.id == targetUid }
                        }
                    }
                }.onFailure {
                    println("rejectFriendRequestError: $it")
                }
        }
    }
}