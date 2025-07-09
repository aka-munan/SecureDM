package devoid.secure.dm.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import devoid.secure.dm.domain.model.UserRepository
import devoid.secure.dm.ui.navigation.RootRoute
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(val supabaseClient: SupabaseClient, private val userRepository: UserRepository) : ViewModel() {
    private val _rootDestination = MutableStateFlow<RootRoute>(RootRoute.Main)
    val rootDestination = _rootDestination.asStateFlow()
    private val _shouldShowSplashScreen = MutableStateFlow(false)
    val shouldShowSplashScreen = _shouldShowSplashScreen.asStateFlow()

    init {
        viewModelScope.launch {
            supabaseClient.auth.sessionStatus.collect {
                when (it) {
                    SessionStatus.Initializing -> {
                        _shouldShowSplashScreen.value = true
                    }

                    is SessionStatus.NotAuthenticated -> {
                        _shouldShowSplashScreen.value = false
                        _rootDestination.value = RootRoute.Auth
                    }

                    else -> {
                        _shouldShowSplashScreen.value = false
                        _rootDestination.value = RootRoute.Main
                    }
                }
            }
        }
    }

    fun updateFcmToken(token: String) {
        viewModelScope.launch {
            userRepository.updateFCMToken(token).onSuccess {
                Logger.i("fcm token updated")
            }.onFailure {
                Logger.e(throwable = it) {
                    "failed to update fcm token"
                }
            }
        }
    }
}