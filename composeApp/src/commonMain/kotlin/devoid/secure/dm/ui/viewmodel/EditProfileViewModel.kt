package devoid.secure.dm.ui.viewmodel

import androidx.lifecycle.viewModelScope
import devoid.secure.dm.domain.model.UserRepository
import devoid.secure.dm.domain.SimplifiedError
import devoid.secure.dm.domain.toSimplifiedError
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
class EditProfileViewModel(private val userRepository: UserRepository) : ProfileViewModel(userRepository) {
    private val _userName = MutableStateFlow("")
    val userName = _userName.asStateFlow()

    private val _fullName = MutableStateFlow("")
    val fullName = _fullName.asStateFlow()
    private val _bio = MutableStateFlow("")
    val bio = _bio.asStateFlow()
    private var avatarUrl = MutableStateFlow("")

    private val _isUserNameAvailable = MutableStateFlow(false)
    val isUserNameAvailable = _isUserNameAvailable.asStateFlow()
    private val _userNameError = MutableStateFlow<SimplifiedError?>(null)
    val userNameError = _userNameError.asStateFlow()

    init {
        viewModelScope.launch {
            launch {
                user.collect {
                    _userName.value = it?.uName ?: ""
                    _fullName.value = it?.fullName ?: ""
                    _bio.value = it?.bio ?: ""
                    avatarUrl.value = it?.avatarUrl ?: ""
                }
            }
            launch {
                userName.debounce(400).collect {
                    if (it == user.value?.uName)
                        return@collect
                    if (it.trim().length < 5) {
                        _userNameError.value = SimplifiedError("Username must contain at least then 5 characters")
                        return@collect
                    }
                    launch {
                        val result = validateUserName(it).single()
                        result.onFailure { error ->
                            _isUserNameAvailable.value = false
                            _userNameError.value = error.toSimplifiedError()
                        }.onSuccess { isAvailable ->
                            _isUserNameAvailable.value = isAvailable
                            _userNameError.value = if (isAvailable) null else SimplifiedError("Username not available")
                        }
                    }
                }
            }
        }
    }

    fun updateUserName(userName: String) {
        _userName.update {
            userName.lowercase().replace(Regex("[^a-z0-9_.]"), "").take(20)
        }
    }

    fun updateFullName(fullName: String) {
        _fullName.update { fullName.take(50) }
    }

    fun updateBio(bio: String) {
        _bio.update { bio.take(100) }
    }

    private fun validateUserName(userName: String): Flow<Result<Boolean>> {
        if (userName.trim().length < 5) {
            return flowOf(Result.failure(UserNameFormatException("Username doesn't meat minimum requirement")))
        }
        return userRepository.validateUserName(userName)
    }

    fun updateProfile(): Flow<Result<Unit>> {
        if (userNameError.value != null) {
            return flowOf(Result.failure(Throwable()))
        }
        user.value?.let {
            val newUser = it.copy(fullName = _fullName.value, uName = userName.value, bio = _bio.value)
            if (newUser == it)
                return flowOf(Result.success(Unit))
            else
                return userRepository.updateUser(newUser)
        } ?: return flowOf(Result.failure(Throwable()))
    }
}