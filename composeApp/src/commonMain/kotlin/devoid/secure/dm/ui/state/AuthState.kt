package devoid.secure.dm.ui.state

import kotlinx.serialization.Serializable

@Serializable
sealed interface AuthState {
    @Serializable
    data object LoggedIn : AuthState
    @Serializable
    data class Login(
        val email: String,
        val password: String,
        val loginError: String? = null
    ) : AuthState
@Serializable
    data class SignUp(
        val email: String,
        val password: String,
        val signupError: String? = null
    ) : AuthState
@Serializable
    data class Setup(val profileUrl: String?, val name: String, val uName: String, val bio: String) :
        AuthState
}