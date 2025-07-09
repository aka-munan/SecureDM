package devoid.secure.dm.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import devoid.secure.dm.domain.model.User
import devoid.secure.dm.domain.model.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

open class ProfileViewModel(private val userRepository: UserRepository) : ViewModel() {
    val user = userRepository.currentUser


    fun getUser(uid: String) =
        userRepository.getUserById(uid)


    fun updateUserProfile() {
        viewModelScope.launch {
//            userRepository.updateUser(user)
        }
    }

    fun pickAvatar(onPicked: (String?) -> Unit) {

//        _avatarUri.value = _avatarUri.value.orEmpty()
    }


}

class UserNameFormatException : Exception {
    constructor() : super()
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
    constructor(cause: Throwable) : super(cause)
}
