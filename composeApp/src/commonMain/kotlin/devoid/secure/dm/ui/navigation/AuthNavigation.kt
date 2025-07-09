package devoid.secure.dm.ui.navigation

import androidx.navigation.*
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import devoid.secure.dm.ui.compose.AuthScreen
import devoid.secure.dm.ui.compose.ForgotPasswordScreen
import devoid.secure.dm.ui.compose.SetupProfileScreen
import kotlinx.serialization.Serializable

fun NavController.navigateLoginScreen(navOptions: NavOptionsBuilder.() -> Unit = {}) =
    navigate(AuthRoute.Login(), navOptions)

fun NavController.navigateSignupScreen(navOptions: NavOptionsBuilder.() -> Unit = {}) =
    navigate(AuthRoute.SignUp(), navOptions)

fun NavController.navigateSetupProfileScreen(navOptions: NavOptionsBuilder.() -> Unit = {}) =
    navigate(AuthRoute.SetupProfile, navOptions)

fun NavGraphBuilder.authScreen(onLoginSuccess: () -> Unit, onNavigate: (AuthRoute) -> Unit) {
    navigation<RootRoute.Auth>(startDestination = AuthRoute.Login()) {
        composable<AuthRoute.Login> {
            AuthScreen(
                isLogin = true,
                onAuthSuccess = onLoginSuccess,
                onNavigate = {
                    onNavigate(it)
                }
            )
        }
        composable<AuthRoute.SignUp> {
            AuthScreen(
                isLogin = false,
                onAuthSuccess = onLoginSuccess,
                onNavigate = {
                    onNavigate(it)
                }
            )
        }
        composable<AuthRoute.SetupProfile> {
            SetupProfileScreen(
                onNavigate = onNavigate,
                onAuthSuccess = onLoginSuccess
            )
        }
        composable<AuthRoute.ForgotPassword> {
            ForgotPasswordScreen(onNavigate = onNavigate)
        }
    }
}

sealed interface AuthRoute {
    @Serializable
    data class SignUp(
        val email: String = "",
        val password: String = "",
        val error: String? = null
    ) : AuthRoute

    @Serializable
    data class Login(
        val email: String = "",
        val password: String = "",
        val error: String? = null
    ) : AuthRoute

    @Serializable
    data object SetupProfile : AuthRoute

    @Serializable
    data object ForgotPassword: AuthRoute
}