package devoid.secure.dm.ui.compose

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import devoid.secure.dm.domain.model.MessageAttachment
import devoid.secure.dm.domain.model.UserClass
import devoid.secure.dm.domain.toDisplaySize
import devoid.secure.dm.ui.navigation.*
import devoid.secure.dm.ui.theme.SecureDmTheme
import devoid.secure.dm.ui.theme.backgroundBrush
import devoid.secure.dm.ui.theme.blurColor
import devoid.secure.dm.ui.viewmodel.MainViewModel
import devoid.secure.dm.ui.viewmodel.PSHomeViewModel
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.KoinContext
import secure_dm.composeapp.generated.resources.Res
import secure_dm.composeapp.generated.resources.person

val LocalDisplaySize = staticCompositionLocalOf { DisplaySize.SMALL }

@Composable
fun App(viewModel: MainViewModel) {
    val rootDestination by viewModel.rootDestination.collectAsStateWithLifecycle()
    SecureDmTheme {
        KoinContext {
            BoxWithConstraints(
                modifier = Modifier.background(MaterialTheme.colorScheme.backgroundBrush())
            ) {
                val windowWidth = maxWidth.value.toInt()
                val navController = rememberNavController()
                val displaySize = remember(maxWidth) { windowWidth.toDisplaySize() }

                CompositionLocalProvider(LocalDisplaySize provides displaySize) {
                    NavHost(
                        navController,
                        startDestination = rootDestination,
                        enterTransition = { EnterTransition.None },
                        exitTransition = { ExitTransition.None }) {
                        mainScreen()
                        authScreen(
                            onNavigate = navController::navigate,
                            onLoginSuccess = navController::navigateToHome
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    displaySize: DisplaySize,
    currentDestination: MainRoute,
    navBarVisible: Boolean = true,
    onNavigate: (MainRoute) -> Unit,
    content: @Composable () -> Unit
) {
//    var currentDestination by remember { mutableStateOf<MainRoute>(MainRoute.Home) }
    val layoutType =
        if (displaySize == DisplaySize.SMALL) NavigationSuiteType.NavigationBar else NavigationSuiteType.NavigationRail
    NavigationSuiteScaffold(
        modifier = modifier,
        containerColor = Color.Transparent,
        layoutType = if (navBarVisible) layoutType else NavigationSuiteType.None,
        navigationSuiteColors = NavigationSuiteDefaults.colors(
            navigationRailContainerColor = MaterialTheme.colorScheme.blurColor,
            navigationBarContainerColor = MaterialTheme.colorScheme.blurColor,
            navigationDrawerContainerColor = MaterialTheme.colorScheme.blurColor,
        ),
        navigationSuiteItems = {
            MainNavBarItem.entries.forEach { navBarItem ->
                item(
                    selected = currentDestination.toNavBarItem() == navBarItem,
                    onClick = {
                        println(navBarItem)
                        onNavigate(navBarItem.toRoute())
                    },
                    icon = {
                        Image(
                            modifier = Modifier.size(24.dp),
                            painter = painterResource(navBarItem.drawable),
                            contentDescription = navBarItem.description,
                            colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.secondary)
                        )
                    },
                    label = {
                        Text(navBarItem.label, color = MaterialTheme.colorScheme.secondary)
                    }
                )
            }
        }) {
        content()
    }
}


@Composable
fun HomeScreenLarge(viewModel: PSHomeViewModel, onNavigateToProfile: (profileId: String) -> Unit) {
    Row() {
        val activeChatId by viewModel.currentChatId.collectAsState()
        HomeScreen(
            modifier = Modifier.widthIn(min = 300.dp)
                .fillMaxWidth(fraction = 0.25f)
                .fillMaxHeight(),
            viewModel = viewModel,
            onItemClick = {
                viewModel.setActiveChat(it.chatId)
            }, onProfileClick = onNavigateToProfile
        )
        VerticalDivider(thickness = Dp.Hairline, color = MaterialTheme.colorScheme.blurColor)
        Box(Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
            if (activeChatId == null) {
                Text(
                    "Select a Chat to start messaging",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            } else {
                ChatScreen(activeChatId!!, onNavigateBack = {
                    viewModel.setActiveChat(null)
                }, onNavigateToProfile = onNavigateToProfile)
            }
        }
    }
}


@Composable
fun UserSearchResults(
    modifier: Modifier = Modifier,
    users: List<UserClass.RemoteUser>,
    onItemClick: (UserClass.RemoteUser) -> Unit
) {
    LazyColumn(modifier) {
        itemsIndexed(users) { index, user ->
            UserSearchItem(
                user = user,
                modifier = Modifier.clickable { onItemClick(user) }.fillMaxWidth().height(72.dp)
            )
            if (index != users.size - 1) {
                HorizontalDivider()
            }
        }
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun UserSearchItem(modifier: Modifier = Modifier, user: UserClass.RemoteUser) {
    val avatarUri = user.avatarUrl ?: Res.drawable.person
    Row(modifier) {
        AsyncImage(
            modifier = Modifier.fillMaxHeight().aspectRatio(1f).padding(8.dp).clip(CircleShape),
            contentScale = ContentScale.Fit,
            model = avatarUri,
            contentDescription = null
        )
        Column(modifier = Modifier.padding(8.dp)) {
            Text(text = user.uName, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(
                text = user.fullName ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
            )
        }
    }
}


@Composable
fun ImageMessage(
    modifier: Modifier = Modifier,
    attachment: MessageAttachment,
    contentScale: ContentScale = ContentScale.Inside,
    placeHolder: @Composable () -> Unit = { CircularProgressIndicator() },
) {
    val imagePainter = rememberAsyncImagePainter(model = attachment.fileUri)
    Box(modifier = modifier) {
        AnimatedContent(imagePainter.state.value) { imageState ->
            when (imageState) {
                is AsyncImagePainter.State.Loading -> {
                    placeHolder()
                }

                is AsyncImagePainter.State.Error -> {
                    Text("Failure to load image", color = MaterialTheme.colorScheme.error)
                }

                is AsyncImagePainter.State.Success, AsyncImagePainter.State.Empty -> {
                    Image(
                        painter = imagePainter,
                        contentDescription = null,
                        modifier = Modifier.heightIn(max = 400.dp).wrapContentSize(),
                        contentScale = contentScale
                    )
                }
            }
        }
    }
}

@Composable
fun SecureDmSnackBar(snackBarHostState: SnackbarHostState, modifier: Modifier = Modifier) {
    SnackbarHost(hostState = snackBarHostState, modifier) {
        Snackbar(
            it,
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}


