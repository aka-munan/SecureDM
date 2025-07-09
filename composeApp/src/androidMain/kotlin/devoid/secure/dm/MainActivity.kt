package devoid.secure.dm

import android.os.Bundle
import android.view.animation.AnticipateInterpolator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import co.touchlab.kermit.Logger
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import devoid.secure.dm.domain.model.FriendRequest
import devoid.secure.dm.domain.model.FriendRequestStatus
import devoid.secure.dm.domain.model.UserClass
import devoid.secure.dm.ui.compose.App
import devoid.secure.dm.ui.compose.FriendRequestItem
import devoid.secure.dm.ui.viewmodel.MainViewModel
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {
    private val viewModel by inject<MainViewModel>()
    private var sessionStatus: SessionStatus = SessionStatus.Initializing
    private val TAG = "MainActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        getFcmTokenOnLogin()
        splashScreen.setOnExitAnimationListener { splashScreenView ->
            splashScreenView.view.animate().scaleY(0.8f).scaleX(0.8f).alpha(0.4f)
                .setDuration(200L)
                .setInterpolator(AnticipateInterpolator()).withEndAction {
                    splashScreenView.remove()
                }
        }
        splashScreen.setKeepOnScreenCondition { viewModel.shouldShowSplashScreen.value }
        setContent {
            App(viewModel)
//            BuildConfig.
        }
    }

    private fun getFcmTokenOnLogin() {
        CoroutineScope(Dispatchers.Main).launch {
            viewModel.supabaseClient.auth.sessionStatus.collect {
                if (it is SessionStatus.NotAuthenticated) {
                    FirebaseMessaging.getInstance().deleteToken()
                }
                if (it is SessionStatus.Authenticated && sessionStatus is SessionStatus.NotAuthenticated) {
                    //this is a fresh login
                    //get the fcm token
                    FirebaseMessaging.getInstance().token
                        .addOnFailureListener { error ->
                            Logger.e(tag = TAG, messageString = "failed to get fcm token", throwable = error)
                        }.addOnSuccessListener { token ->
                            Logger.i(tag = TAG, messageString = " Updating FCM token")
                            viewModel.updateFcmToken(token)
                        }
                }
                sessionStatus = it
            }
        }
    }
}

//@OptIn(ExperimentalMaterial3Api::class, ExperimentalResourceApi::class)
//@Preview(showBackground = true)
@Composable
fun RemoteUSerProfile() {
    val user = UserClass.RemoteUser(
        id = "",
        uName = "aka_munan",
        fullName = "Haris Mehraj",
        bio = "Pationate Android devloper ",
    )
//    RemoteUserProfile(uId = "", onMessageBtnClick = {}) { }
}

//@Preview
@Composable
fun PreviewFriendRequestItem() {
    val user = UserClass.RemoteUser(
        id = "",
        uName = "aka_munan",
        fullName = "Haris Mehraj",
        bio = "Pationate Android devloper ",
    )
    FriendRequestItem(user = user, onAcceptRequest = {}, onRejectRequest = {})
}

@OptIn(ExperimentalMaterial3Api::class)
//@Preview
@Composable
fun FriendRequestsScreen() {
    var demoItems by remember{mutableStateOf(buildList<FriendRequest> {
        for (i in 0..10) {
            add(
                FriendRequest(
                    chatID = "$i",
                    FriendRequestStatus.PENDING,
                    UserClass.RemoteUser(
                        id = "$i",
                        uName = "aka_munan",
                        fullName = "Haris Mehraj",
                        bio = "Pationate Android devloper ",
                    )
                )
            )
        }
    })}
    Scaffold(topBar = {
        TopAppBar(title = { Text("Friend Requests") })
    }) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(demoItems) { friendRequest ->
                FriendRequestItem(
                    user = friendRequest.user,
                    onAcceptRequest = {
                        demoItems = demoItems.minus(friendRequest)
                    }, onRejectRequest = {
                        demoItems.minus(friendRequest)
                    })
            }
        }
    }
}

//@OptIn(ExperimentalMaterial3Api::class)
////@Preview
//@Composable
//fun StartConversationLayout() {
//    val demoList = buildList {
//        for (i in 0..20) {
//            add(UserClass.RemoteUser("$i", "user$i"))
//        }
//    }
//    val isLargeSize by mutableStateOf(false)
//    Dialog(properties = DialogProperties(), onDismissRequest = {}) {
//        if (!isLargeSize) {
//            TopAppBar(title = { Text("") })
//        }
//        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
//            items(demoList) { user ->
//                UserSearchItem(user = user)
//            }
//        }
//    }
//}


//@Preview
//@Composable
//fun AttachmentDisplayer() {
//    val attachments = remember {
//        mutableListOf<MessageAttachment>(
//            MessageAttachment("0", "", "", "tezt.pdf", AttachmentType.DOCUMENT),
//            MessageAttachment("0", "", "", "tezt2.pdf", AttachmentType.DOCUMENT),
//            MessageAttachment("0", "", "", "tezt2.pdf", AttachmentType.DOCUMENT),
//            MessageAttachment("0", "", "", "tezt2.pdf", AttachmentType.DOCUMENT),
//            MessageAttachment("0", "", "", "tezt2.pdf", AttachmentType.DOCUMENT)
//        )
//    }
//    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
//        itemsIndexed(attachments) { index, attachment ->
//            AttachmentDisplayerItem(
//                Modifier
//                    .heightIn(max = 100.dp)
//                    .widthIn(max = 160.dp),
//                attachment = attachment
//            ) {
//                attachments.removeAt(index)
//            }
//        }
//    }
//}

//@Preview
//@Composable
//fun AudioRecord() {
//    Row() {
//        IconButton(
//            onClick = {},
//            colors = IconButtonDefaults.filledTonalIconButtonColors()
//        ) {
//            Icon(Icons.Rounded.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
//        }
//        Spacer(Modifier.width(8.dp))
//        Row(
//            modifier = Modifier
//                .clip(RoundedCornerShape(30.dp))
//                .background(MaterialTheme.colorScheme.secondaryBackgroundBrush())
//                .border(
//                    width = 1.dp,
//                    shape = RoundedCornerShape(30.dp),
//                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
//                )
//                .padding(end = 6.dp), verticalAlignment = Alignment.CenterVertically,
//            horizontalArrangement = Arrangement.spacedBy(8.dp)
//        ) {
//            AudioPlayer()
//            Button(
//                enabled = true, onClick = { }
//            ) {
//                Icon(
//                    imageVector = Icons.AutoMirrored.Rounded.Send,
//                    contentDescription = "send message"
//                )
//            }
//        }
//    }
//}

//@Composable
//fun RowScope.AudioPlayer(){
//    var playerProgress by remember { mutableFloatStateOf(0.0f) }
//    var progIndicatorMaxWidth = 1
//    Card(onClick = {}, shape = CircleShape) {
//        Icon(
//            modifier = Modifier.padding(4.dp),
//            imageVector = Icons.Rounded.PlayArrow,
//            contentDescription = null
//        )
//    }
//    LinearProgressIndicator(
//        progress = { playerProgress },
//        modifier = Modifier
//            .weight(1f)
//            .onSizeChanged { progIndicatorMaxWidth = it.width }
//            .pointerInput(key1 = null) {
//                detectHorizontalDragGestures(onDragEnd = {
//                    //invoke the player to update
//                }) { _, dragAmount ->
//                    playerProgress += dragAmount / progIndicatorMaxWidth
//                }
//            },
//    )
//    Text("00:00", color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f))
//
//}

//@Preview
//@Composable
//fun RecordingAudio() {
//    Row(
//        modifier = Modifier
//            .clip(RoundedCornerShape(30.dp))
//            .background(MaterialTheme.colorScheme.tertiary)
//            .border(
//                width = 1.dp,
//                shape = RoundedCornerShape(30.dp),
//                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
//            )
//            .padding(start = 8.dp), verticalAlignment = Alignment.CenterVertically,
//        horizontalArrangement = Arrangement.spacedBy(8.dp)
//    ) {
//       Icon(painter = painterResource(Res.drawable.circle_filled), contentDescription = null,tint = MaterialTheme.colorScheme.onTertiary)
//        Text("recording...", modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onTertiary.copy(alpha = 0.6f))
//        Text("00:00",color = MaterialTheme.colorScheme.onTertiary.copy(alpha = 0.6f))
//        Card(onClick = {}, shape = CircleShape) {
//            Icon(
//                modifier = Modifier.padding(4.dp),
//                painter = painterResource(Res.drawable.stop),
//                contentDescription = null
//            )
//        }
//    }
//}

//@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
//@Composable
//fun AudioAttachmentComponent(){
//    var isAudioPlaying by remember { mutableStateOf(false) }
//    val duration  =1000*10
//    var playerProgress by remember { mutableStateOf(0.5f) }
//    Row(Modifier.clip(MaterialTheme.shapes.medium).background(MaterialTheme.colorScheme.inversePrimary).padding(8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
//        AudioPlayer(
//            isAudioPlaying = isAudioPlaying,
//            duration = duration,
//            playerProgress = playerProgress,
//            onSeek = { playerProgress = it }
//        ) { isAudioPlaying = !isAudioPlaying }
//    }
//}
//
