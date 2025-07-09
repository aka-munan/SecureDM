package devoid.secure.dm.ui.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.*
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import co.touchlab.kermit.Logger
import devoid.secure.dm.data.datastore.SettingsSource
import devoid.secure.dm.domain.model.UserClass
import devoid.secure.dm.domain.model.UserNavType
import devoid.secure.dm.domain.settings.LocalSettingConfig
import devoid.secure.dm.domain.settings.SettingsConfig
import devoid.secure.dm.ui.compose.*
import devoid.secure.dm.ui.viewmodel.PSHomeViewModel
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import secure_dm.composeapp.generated.resources.Res
import secure_dm.composeapp.generated.resources.home
import secure_dm.composeapp.generated.resources.person
import secure_dm.composeapp.generated.resources.person_add
import secure_dm.composeapp.generated.resources.settings
import kotlin.reflect.typeOf

const val APP_URI = "https://www.secure.dm"
fun NavController.navigateToHome(navOptions: NavOptionsBuilder.() -> Unit = {}) = navigate(MainRoute.Home, navOptions)


fun NavController.navigateToChat(
    chatId: String,
    navOptions: NavOptionsBuilder.() -> Unit = {
        popUpTo<HomeRoute.Home> {
            saveState = true
        }
        restoreState = true
    }
) =
    navigate(HomeRoute.Chat(chatId), navOptions)

fun NavController.navigateToLocalProfile(navOptions: NavOptionsBuilder.() -> Unit = {}) =
    navigate(MainRoute.LocalProfile, navOptions)

fun NavController.navigateToRemoteProfile(uid: String, navOptions: NavOptionsBuilder.() -> Unit = {}) =
    navigate(MainRoute.RemoteProfile(uid), navOptions)

fun NavController.navigateToRequests(navOptions: NavOptionsBuilder.() -> Unit = {}) =
    navigate(MainRoute.Requests, navOptions)

fun NavController.navigateToSettings(navOptions: NavOptionsBuilder.() -> Unit = {}) =
    navigate(MainRoute.Settings, navOptions)


fun NavGraphBuilder.mainScreen() {
    composable<RootRoute.Main> {
        val settingsSource = koinInject<SettingsSource>()
        val mainNavController = rememberNavController()
        val lifecycleOwner = LocalLifecycleOwner.current
        val displaySize = LocalDisplaySize.current
        val navBackStackEntry by mainNavController.currentBackStackEntryAsState()
        val navBarVisible by derivedStateOf {
            !(navBackStackEntry?.destination?.route?.contains(
                HomeRoute.Chat::class.qualifiedName ?: ""
            ) ?: false && displaySize == DisplaySize.SMALL)
        }
        val currentDestination by remember(navBackStackEntry) {
            mutableStateOf<MainRoute>(MainRoute.Home).apply {
                val route = navBackStackEntry?.destination?.route
                val newDestination = MainRoute.fromRoute(route)
                if (newDestination != null) {
                    value = newDestination
                }
            }
        }
        val viewModel = koinViewModel<PSHomeViewModel>()
        DisposableEffect(lifecycleOwner) {
            lifecycleOwner.lifecycle.addObserver(viewModel)
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(viewModel)
            }
        }
        val settingsConfig by settingsSource.getSettingsConfig().collectAsState(SettingsConfig())
        CompositionLocalProvider(LocalSettingConfig provides settingsConfig){
            MainScreen(
                displaySize = displaySize,
                navBarVisible = navBarVisible,
                currentDestination = currentDestination,
                onNavigate = mainNavController::navigate
            ) {
                NavHost(navController = mainNavController, startDestination = MainRoute.Home) {
                    when (displaySize) {
                        DisplaySize.SMALL, DisplaySize.MEDIUM -> {
                            mainScreenSmallToMedium(mainNavController, viewModel)
                        }

                        DisplaySize.LARGE -> {
                            mainScreenLarge(viewModel) { mainNavController.navigateToRemoteProfile(it);Logger.i("nev to remote profile") }
                        }
                    }

                    mainScreenCommon(mainNavController, viewModel)
                }
            }
        }


    }
}


fun NavGraphBuilder.mainScreenSmallToMedium(
    navController: NavController,
    viewModel: PSHomeViewModel
) {
    println("Layout: Small-Medium")
    navigation<MainRoute.Home>(HomeRoute.Home) {
        composable<HomeRoute.Home>(exitTransition = { fadeOut() }) {
            HomeScreen(
                onItemClick = {
                    navController.navigateToChat(chatId = it.lastMessage.chatId)
                    viewModel.setActiveChat(it.chatId)
                },
                viewModel = viewModel,
                onProfileClick = { navController.navigateToRemoteProfile(it) })
        }
        composable<HomeRoute.Chat>(
            typeMap = mapOf(typeOf<UserClass.RemoteUser>() to UserNavType()),
            deepLinks = listOf(
                navDeepLink<HomeRoute.Chat>(
                    basePath = "$APP_URI/chat"
                )
            ),
            enterTransition = { fadeIn() + slideInHorizontally(initialOffsetX = { it }) },
            exitTransition = { fadeOut() + slideOutHorizontally { it } }
        ) { backStackEntry ->
            val route = backStackEntry.toRoute<HomeRoute.Chat>()
            ChatScreen(
                chatId = route.chatId,
                onNavigateBack = {
                    navController.popBackStack<HomeRoute.Home>(inclusive = false)
                    viewModel.setActiveChat(null)
                }, onNavigateToProfile = navController::navigateToRemoteProfile
            )
            DisposableEffect(route) {
                onDispose {
                    viewModel.setActiveChat(null)
                }
            }
        }
    }

}

fun NavGraphBuilder.mainScreenLarge(viewModel: PSHomeViewModel,onNavigateToProfile:(profileId: String)->Unit) {
    println("Layout: Large")
    navigation<MainRoute.Home>(HomeRoute.Home) {
        composable<HomeRoute.Home> {
            HomeScreenLarge(viewModel = viewModel,onNavigateToProfile)
        }
        composable<HomeRoute.Chat> {backStackEntry->
            val route = backStackEntry.toRoute<HomeRoute.Chat>()
            viewModel.setActiveChat(route.chatId)
            HomeScreenLarge(viewModel = viewModel,onNavigateToProfile)
        }

    }
}

fun NavGraphBuilder.mainScreenCommon(navController: NavController, viewModel: PSHomeViewModel) {
    composable<MainRoute.LocalProfile> {
        LocalUserProfile()
    }
    composable<MainRoute.Requests> {
        FriendRequestsScreen()
    }
    composable<MainRoute.Settings> {
        SettingsScreen()
    }
    composable<MainRoute.RemoteProfile>() { backStackEntry ->
        Logger.i("remote profile screen")
        val remoteProfile = backStackEntry.toRoute<MainRoute.RemoteProfile>()
        RemoteUserProfile(
            uId = remoteProfile.uid,
            viewModel = viewModel,
            onMessageBtnClick =navController::navigateToChat,
            onNavigateBack = navController::popBackStack
        )
    }
}

@Serializable
sealed interface RootRoute {
    @Serializable
    data object Main : RootRoute

    @Serializable
    data object Auth : RootRoute
}

@Serializable
sealed interface HomeRoute {
    @Serializable
    data object Home : MainRoute

    @Serializable
    data class Chat(val chatId: String) : MainRoute
}

@Serializable
sealed interface MainRoute {
    @Serializable
    data object Home : MainRoute

    @Serializable
    data class RemoteProfile(val uid: String) : MainRoute

    @Serializable
    data object LocalProfile : MainRoute

    @Serializable
    data object Requests : MainRoute

    @Serializable
    data object Settings : MainRoute

    fun toNavBarItem(): MainNavBarItem {
        return when (this) {
            Home -> MainNavBarItem.HOME
            LocalProfile -> MainNavBarItem.PROFILE
            Requests -> MainNavBarItem.REQUESTS
            Settings -> MainNavBarItem.SETTINGS
            else -> throw IllegalArgumentException("No NavBar Item For ${this::class.qualifiedName}")
        }
    }

    companion object {
        fun fromRoute(route: String?): MainRoute? {
            return when (route) {
                Home::class.qualifiedName -> Home
                LocalProfile::class.qualifiedName -> LocalProfile
                Requests::class.qualifiedName -> Requests
                Settings::class.qualifiedName -> Settings
                else -> null
            }
        }
    }
}

@OptIn(ExperimentalResourceApi::class)
enum class MainNavBarItem(val label: String, val drawable: DrawableResource, val description: String) {
    HOME("Home", Res.drawable.home, "navigate Home"),
    PROFILE("Profile", Res.drawable.person, "navigate Profile"),
    REQUESTS("Requests", Res.drawable.person_add, "navigate Requests"),
    SETTINGS("Settings", Res.drawable.settings, "navigate Settings");

    fun toRoute(): MainRoute {
        return when (this) {
            HOME -> MainRoute.Home
            PROFILE -> MainRoute.LocalProfile
            REQUESTS -> MainRoute.Requests
            SETTINGS -> MainRoute.Settings
        }
    }
}