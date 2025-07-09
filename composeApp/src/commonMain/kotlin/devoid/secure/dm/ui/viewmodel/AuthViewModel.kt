package devoid.secure.dm.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import devoid.secure.dm.domain.model.AuthRepository
import devoid.secure.dm.domain.model.UserRepository
import kotlinx.coroutines.launch

class AuthViewModel(private val authRepository: AuthRepository, private val userRepository: UserRepository) :
    ViewModel() {

    val currentUser = userRepository.currentUser

    fun signIn(email: String, password: String, onResult: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            onResult(authRepository.signInEmail(email, password))
        }
    }

    fun signUp(email: String, password: String, onResult: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            onResult(authRepository.signUpEmail(email, password))
        }
    }

    fun sendPasswordReset(email: String, onResult: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            onResult(authRepository.resetPassword(email))
        }
    }

    fun signOut(onResult: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            onResult(authRepository.signOut())
        }
    }

    fun getLoginProvider(): LoginProvider {
        return if (authRepository.getProvider()?.replace("\"","")?.equals(LoginProvider.GOOGLE.name, true) == true)
            LoginProvider.GOOGLE
        else
            LoginProvider.EMAIL
    }
}

enum class LoginProvider {
    GOOGLE, EMAIL
}