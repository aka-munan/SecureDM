package devoid.secure.dm.ui.viewmodel

import androidx.lifecycle.*
import co.touchlab.kermit.Logger
import devoid.secure.dm.domain.AppNotificationManager
import devoid.secure.dm.domain.model.ChatItem
import devoid.secure.dm.domain.model.ChatRepository
import devoid.secure.dm.domain.model.FriendsRepository
import devoid.secure.dm.domain.model.UserClass
import devoid.secure.dm.ui.compose.PagingItems
import devoid.secure.dm.ui.compose.getPagedIntoState
import io.github.jan.supabase.SupabaseClient
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

expect class PSHomeViewModel(
    friendsRepository: FriendsRepository,
    chatRepository: ChatRepository,
    notificationManager: AppNotificationManager,
    supabaseClient: SupabaseClient
) : HomeViewModel

open class HomeViewModel(private val friendsRepository: FriendsRepository,private val supabaseClient: SupabaseClient) :
    ViewModel(), LifecycleEventObserver {
    val pageSize = 20
    private val _searchUserQueryResult = MutableStateFlow<Result<List<UserClass.RemoteUser>>>(Result.success(listOf()))
    val searchUserQueryResult = _searchUserQueryResult.asStateFlow()

    private val _friends = MutableStateFlow<PagingItems<UserClass.RemoteUser>>(PagingItems())
    val friends = _friends.asStateFlow()
    private var searchUserJob: Job? = null

    private val _currentChatId = MutableStateFlow<String?>(null)
    val currentChatId = _currentChatId.asStateFlow()
    private val currentLifecycleState = MutableStateFlow(Lifecycle.State.INITIALIZED)
    protected val isCurrentChatActive = combine(flow= currentChatId, flow2 =  currentLifecycleState, transform = { currentChat, lifecycleEvent->
        currentChat!=null && lifecycleEvent==Lifecycle.State.RESUMED
    }).stateIn(viewModelScope, SharingStarted.Eagerly,false)


    fun getFriends(pageNumber: Int = 1) {
        viewModelScope.launch {
            _friends.value.getPagedIntoState(
                state = _friends,
                isRefreshRequest = pageNumber == 1,
                pageSize = pageSize
            ) {
                friendsRepository.getFriends(pageNumber = pageNumber, pageSize = pageSize)
            }
        }
    }

    fun findUserByUname(uName: String) {
        searchUserJob?.cancel()
        searchUserJob = viewModelScope.launch {
            delay(400)
            _searchUserQueryResult.value = friendsRepository.searchUserByUname(uName.trim())
        }
    }

    fun getUserById(uid: String) = flow<Result<UserClass.RemoteUser>> {
        emit(friendsRepository.getUserById(uid))
    }

    fun getFriendshipStatus(uid: String) = flow {
        friendsRepository.getFriendShipStatus(uid).onSuccess {
            emit(it)
        }.onFailure {
            emit(null)
        }
    }

    fun getChatIdFromProfile(profileId: String) = flow{
        friendsRepository.getChatIdFromOfTarget(profileId).onSuccess {
            emit(it)
        }.onFailure {
            emit(null)
        }
    }

    open fun getChatItemsFlow(): Any = Unit

    open fun clearUnseenCount(chatId: String) {}

    open fun subscribeToMessages(chatId: String){}

    open fun unSubscribeToMessages(){}

    fun setActiveChat(chatId: String?) {
        _currentChatId.value = chatId
    }

    fun sendFriendRequest(targetUid: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            friendsRepository.sendFriendRequest(targetUid).onSuccess {
                onResult(true)
            }.onFailure {
                onResult(false)
            }
        }
    }

    //    lifecycle event observer
    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        Logger.i("lifecycle event: "+event.name)
        currentLifecycleState.value = event.targetState
    }
}


