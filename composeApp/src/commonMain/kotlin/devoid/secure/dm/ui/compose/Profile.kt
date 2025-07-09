package devoid.secure.dm.ui.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewModelScope
import coil3.compose.AsyncImage
import devoid.secure.dm.domain.model.AttachmentType
import devoid.secure.dm.domain.model.FriendRequestStatus
import devoid.secure.dm.domain.model.MessageAttachment
import devoid.secure.dm.domain.model.UserClass
import devoid.secure.dm.ui.state.AudioPlayerState
import devoid.secure.dm.ui.state.AudioPlayerStateManager
import devoid.secure.dm.ui.theme.backgroundBrush
import devoid.secure.dm.ui.theme.backgroundMiddleColor
import devoid.secure.dm.ui.theme.blurColor
import devoid.secure.dm.ui.viewmodel.AttachmentsViewModel
import devoid.secure.dm.ui.viewmodel.FriendRequestViewModel
import devoid.secure.dm.ui.viewmodel.HomeViewModel
import devoid.secure.dm.ui.viewmodel.ProfileViewModel
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import secure_dm.composeapp.generated.resources.Res
import secure_dm.composeapp.generated.resources.chat
import secure_dm.composeapp.generated.resources.person
import secure_dm.composeapp.generated.resources.person_add

@OptIn(ExperimentalResourceApi::class, ExperimentalMaterial3Api::class)
@Composable
fun LocalUserProfile() {
    val viewModel = koinViewModel<ProfileViewModel>()
    val user by viewModel.user.collectAsState()
    val profileUrl by derivedStateOf { user?.avatarUrl ?: Res.drawable.person }
    var editProfile by remember { mutableStateOf(false) }
    Scaffold(
        modifier = Modifier.then(if (editProfile) Modifier.blur(4.dp) else Modifier),
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                colors = TopAppBarDefaults.topAppBarColors().copy(
                    containerColor = MaterialTheme.colorScheme.blurColor
                )
            )
        }) { padding ->
        if (user == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
                return@Scaffold
            }
        }
        if (editProfile) {
            var dialogVisible by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) { dialogVisible = true }
            Dialog(
                onDismissRequest = { editProfile = false }
            ) {
                AnimatedVisibility(
                    visible = dialogVisible, enter = fadeIn() + scaleIn(
                        initialScale = 0.8f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    )
                ) {
                    Card(
                        Modifier.widthIn(max = 450.dp).fillMaxWidth().clip(CardDefaults.shape)
                            .background(MaterialTheme.colorScheme.backgroundBrush()),
                        colors = CardDefaults.cardColors().copy(containerColor = Color.Transparent)
                    ) {
                        SetupProfileLayout(enableBackNavigation = false, onSuccess = { editProfile = false })
                    }
                }
            }
        }


        Column(
            Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    modifier = Modifier.size(62.dp).clip(CircleShape)
                        .background(color = MaterialTheme.colorScheme.surfaceContainer),
                    model = profileUrl,
                    colorFilter =
                        if (user?.avatarUrl == null)
                            ColorFilter.tint(color = MaterialTheme.colorScheme.secondary)
                        else null,
                    contentDescription = null
                )
                Spacer(Modifier.width(16.dp))
                Text(
                    text = user?.fullName ?: "",
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = user?.bio ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary,
                maxLines = 5,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.Medium
            )
            Card(
                modifier = Modifier.widthIn(max = 450.dp).fillMaxWidth(),
                colors = CardDefaults.cardColors().copy(containerColor = MaterialTheme.colorScheme.blurColor)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = "Personal Information",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Row(Modifier.height(IntrinsicSize.Min)) {
                        Text(
                            "Username",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        VerticalDivider(modifier = Modifier.padding(horizontal = 8.dp))
                        Spacer(Modifier.fillMaxWidth().weight(1f))
                        Text(
                            text = user?.uName ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                        Text(
                            "Email",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        VerticalDivider(modifier = Modifier.padding(horizontal = 8.dp))
                        Spacer(Modifier.fillMaxWidth().weight(1f))
                        Text(
                            text = user?.email ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.widthIn(max = 450.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
            ) {
                OutlinedButton(onClick = { editProfile = true }, shape = MaterialTheme.shapes.medium) {
                    Text("Edit Profile")
                }
                OutlinedButton(onClick = {}, shape = MaterialTheme.shapes.medium) {
                    Text("Share Profile")
                }
            }
        }
    }
}

@OptIn(ExperimentalResourceApi::class, ExperimentalMaterial3Api::class)
@Composable
fun RemoteUserProfile(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel,
    uId: String,
    onMessageBtnClick: (chatId: String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val userResult by viewModel.getUserById(uId).collectAsState(initial = null)
    var friendshipStatus by remember { mutableStateOf<FriendRequestStatus?>(null) }
    val user by derivedStateOf {
        userResult?.getOrNull()
    }
    val chatId by viewModel.getChatIdFromProfile(uId).collectAsState(null)
    val avatarUri = user?.avatarUrl ?: Res.drawable.person
    var friendReqBtnEnabled by remember { mutableStateOf(friendshipStatus == null) }
    val messageBtnEnabled by derivedStateOf {
        when (friendshipStatus) {
            FriendRequestStatus.ACCEPTED -> true
            else -> false
        }
    }
    val snackbarState = remember { SnackbarHostState() }
    LaunchedEffect(Unit) {
        friendshipStatus = viewModel.getFriendshipStatus(uId).single()
    }

    val friendReqBtnText by derivedStateOf {
        when (friendshipStatus) {
            FriendRequestStatus.ACCEPTED -> "Accepted"
            FriendRequestStatus.REJECTED -> "Rejected"
            FriendRequestStatus.PENDING -> "Requested"
            null -> "Add Friend"
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarState) },
        containerColor = Color.Transparent,
    ) {
        Column(
            Modifier
                .fillMaxSize()
        ) {
            Box(//top gradient component
                Modifier
                    .fillMaxHeight(0.18f)
                    .fillMaxWidth()
                    .shadow(elevation = 4.dp)
                    .background(
                        Brush.radialGradient(
                            0.0f to MaterialTheme.colorScheme.backgroundMiddleColor,
                            1.0f to MaterialTheme.colorScheme.surface,
                            center = Offset(200f, Float.POSITIVE_INFINITY),
                            radius = 500f
                        )
                    )
            ) {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                    title = {
                        Text(user?.uName ?: "", modifier = Modifier.padding(8.dp))
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null)
                        }
                    }
                )
            }
            Column(Modifier.padding(horizontal = 16.dp)) {

                AsyncImage(
                    modifier = Modifier
                        .size(56.dp)
                        .offset(y = -28.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainer),
//                        .border(width = 4.dp, shape = CircleShape, color = MaterialTheme.colorScheme.surface),
                    contentScale = ContentScale.Fit,
                    model = avatarUri,
                    contentDescription = null
                )
                Text(
                    user?.fullName ?: "",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    user?.uName ?: "",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    user?.bio ?: "",
                    color = MaterialTheme.colorScheme.secondary,
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedButton(
                        enabled = friendReqBtnEnabled,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            when (friendshipStatus) {
                                null -> {
                                    viewModel.sendFriendRequest(targetUid = uId) { success ->
                                        if (success) {
                                            friendshipStatus = FriendRequestStatus.PENDING
                                        }
                                    }
                                }

                                FriendRequestStatus.PENDING -> {}
                                else -> {}
                            }
                        }, shape = MaterialTheme.shapes.medium
                    ) {
                        AsyncImage(
                            modifier = Modifier
                                .size(24.dp),
                            model = Res.drawable.person_add,
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(color = LocalContentColor.current)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(friendReqBtnText)
                    }
                    Button(
                        enabled = messageBtnEnabled && chatId != null,
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.medium,
                        onClick = {
                            onMessageBtnClick(chatId!!)
                        }) {
                        AsyncImage(
                            modifier = Modifier
                                .size(24.dp),
                            model = Res.drawable.chat,
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(color = LocalContentColor.current)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Message")
                    }
                }
                if (friendshipStatus != FriendRequestStatus.ACCEPTED || chatId == null) {
                    return@Column//show attachments share in chat if request is accepted
                }
                val attachmentViewModel = koinInject<AttachmentsViewModel>()
                val dropDownState = rememberMessageDropDownState()
                val tabRowItems = remember { listOf("Files", "Audio", "Media") }
                var selectedTabIndex by remember { mutableIntStateOf(0) }
                var selectedAttachment by remember { mutableStateOf<MessageAttachment?>(null) }
                var fullscreenAttachment by remember { mutableStateOf<MessageAttachment?>(null) }
                LaunchedEffect(selectedAttachment) {
                    if (selectedAttachment == null) {
                        dropDownState.isVisible = false
                        return@LaunchedEffect
                    }
                    dropDownState.isVisible = true
                }
                LaunchedEffect(Unit) {
                    dropDownState.apply {
                        containsText = false
                        canDownload = true
                        canUnsend = false
                        canReply = false
//                        isVisible = true
                    }
                    dropDownState.callback = object : MessageDropDownCallback {
                        override fun onReply() {
                        }

                        override fun onCopy() {
                        }

                        override fun onUnsend() {
                        }

                        override fun onDownload() {
                            attachmentViewModel.downloadAttachment(selectedAttachment!!) {
                                attachmentViewModel.viewModelScope.launch {
                                    if (it == null) {
                                        snackbarState.showSnackbar(message = "Download successful.")
                                    } else {
                                        snackbarState.showSnackbar(message = "Download failed.")

                                    }
                                }
                            }
                        }
                    }
                }
                PrimaryTabRow(containerColor = Color.Transparent, selectedTabIndex = selectedTabIndex) {
                    tabRowItems.forEachIndexed { index, value ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = { Text(value) })
                    }
                }
                val attachmentLazyItems by attachmentViewModel.attachments.collectAsState()
                val attachmentsLoadState by derivedStateOf {
                    attachmentLazyItems.loadState
                }
                val attachments by derivedStateOf {
                    attachmentLazyItems.items
                }

                val attachmentType = remember(selectedTabIndex) {
                    when (selectedTabIndex) {
                        0 -> arrayOf(AttachmentType.DOCUMENT)
                        1 -> arrayOf(AttachmentType.AUDIO)
                        else -> {
                            arrayOf(AttachmentType.IMAGE, AttachmentType.VIDEO)
                        }
                    }
                }
                LaunchedEffect(attachmentType) {
                    attachmentViewModel.getAttachments(chatId = chatId!!, 1, attachmentType = attachmentType)
                }
                val listState = rememberLazyListState()
                LaunchedEffect(listState) {
                    watchListStateForPagination(listState = listState, loadState = attachmentsLoadState) {
                        if (attachments.size < viewModel.pageSize)
                            return@watchListStateForPagination
                        attachmentViewModel.getAttachments(
                            chatId = chatId!!,
                            pageNumber = attachments.size / attachmentViewModel.pageSize,
                            *attachmentType
                        )
                    }
                }
                LazyVerticalStaggeredGrid(
//                    state = listState,
                    columns = StaggeredGridCells.Adaptive(
                        when (attachmentType[0]) {
                            AttachmentType.DOCUMENT -> 300.dp
                            AttachmentType.AUDIO -> 400.dp
                            else -> {
                                200.dp
                            }
                        }
                    ),
                    modifier = Modifier.padding(8.dp).fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (attachmentsLoadState.refresh is LoadState.Loading) {
                        item {
                            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                    itemsIndexed(attachments) { index, attachment ->
                        var position = Offset.Zero
                        ProfileAttachmentDisplayer(
                            modifier = Modifier
                                .onGloballyPositioned {
                                    position = it.positionInRoot()
                                }.rightClickMenu { offset ->
                                    val clickPosition = position.plus(offset)
                                    dropDownState.offset = DpOffset(clickPosition.x.toDp(), clickPosition.y.toDp())
                                    selectedAttachment = attachment
                                }.pointerInput(Unit) {
                                    detectTapGestures(
                                        onLongPress = { offset ->
                                            val clickPosition = Offset.Zero
                                            dropDownState.offset =
                                                DpOffset(clickPosition.x.toDp(), clickPosition.y.toDp())
                                            selectedAttachment = attachment
                                        },
                                        onTap = {
                                            if (attachment.type == AttachmentType.IMAGE || attachment.type == AttachmentType.VIDEO
                                            ) {//on tap
                                                fullscreenAttachment = attachment
                                            } else null
                                        }
                                    )
                                },
                            audioPlayerStateManager = attachmentViewModel.audioPlayerStateManager,
                            attachment = attachment
                        ) {
                            attachmentViewModel.downloadAttachment(attachment) { error ->
                                if (error != null) {
                                    viewModel.viewModelScope.launch {
                                        snackbarState.showSnackbar("Download failed.")
                                    }
                                } else {
                                    viewModel.viewModelScope.launch {
                                        snackbarState.showSnackbar("Download Successful.")
                                    }
                                }
                            }
                        }
                    }
                    if (attachmentsLoadState.append is LoadState.Loading) {
                        item {
                            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                }
                MessageDropDown(
                    state = dropDownState,
                    onDismiss = { selectedAttachment = null },
                    date = ""
                )
                if (fullscreenAttachment != null) {
                    FullscreenAttachment(fullscreenAttachment!!, onClose = { fullscreenAttachment = null })
                }
            }
        }
    }
}

@Composable
fun FullscreenAttachment(attachment: MessageAttachment, onClose: () -> Unit) {
    Dialog(onDismissRequest = onClose) {
        Card(Modifier.sizeIn(minWidth = 200.dp)) {
            AsyncImage(model = attachment.fileUri, contentDescription = null, contentScale = ContentScale.FillWidth)
        }
    }
}

@Composable
fun ProfileAttachmentDisplayer(
    modifier: Modifier = Modifier,
    audioPlayerStateManager: AudioPlayerStateManager,
    attachment: MessageAttachment,
    onDownloadAttachment: () -> Unit
) {
    val audioPlayerState by audioPlayerStateManager.state.collectAsState(null)
    Box(modifier) {
        when (attachment.type) {
            AttachmentType.AUDIO -> {
                Row(
                    Modifier.widthIn(max = 400.dp).clip(MaterialTheme.shapes.medium)
                        .background(MaterialTheme.colorScheme.inversePrimary).padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AudioPlayer(
                        state = audioPlayerState,
                        duration = attachment.duration ?: 0,
                        onSeek = {
                            if (audioPlayerState is AudioPlayerState.Ready && (audioPlayerState as AudioPlayerState.Ready).id == attachment.messageId)
                                audioPlayerStateManager.onUpdateState(
                                    (audioPlayerState as AudioPlayerState.Ready).copy(
                                        playerProgress = it
                                    ), null
                                )
                        }
                    ) {
                        audioPlayerStateManager.onUpdateState(
                            (audioPlayerState.takeIf { it?.id == attachment.messageId } as AudioPlayerState.Ready?)?.copy(
                                isPlaying = !(audioPlayerState as AudioPlayerState.Ready).isPlaying
                            )
                                ?: AudioPlayerState.Ready(//new audio player request
                                    id = attachment.messageId,
                                    isPlaying = true,
                                    duration = attachment.duration ?: 0,
                                    playerProgress = 0f
                                ), attachment
                        )
                    }
                }
            }

            AttachmentType.VIDEO -> {

            }

            AttachmentType.IMAGE -> {
                ImageMessage(attachment = attachment, contentScale = ContentScale.FillWidth)
            }

            AttachmentType.DOCUMENT -> {
                DocumentAttachment(
                    modifier = Modifier.widthIn(max = 300.dp),
                    shape = MaterialTheme.shapes.medium,
                    attachment = attachment,
                    onDownloadClick = onDownloadAttachment
                )
            }
        }
    }
}

@Composable
fun FriendRequestItem(
    modifier: Modifier = Modifier.fillMaxWidth(),
    user: UserClass.RemoteUser,
    onAcceptRequest: () -> Unit,
    onRejectRequest: () -> Unit
) {
    Card(modifier, shape = MaterialTheme.shapes.medium) {
        Row(Modifier.height(IntrinsicSize.Min).padding(8.dp)) {
            AsyncImage(
                model = user.avatarUrl,
                modifier = Modifier
                    .fillMaxHeight()
                    .aspectRatio(1f)
                    .clip(MaterialTheme.shapes.large)
                    .background(MaterialTheme.colorScheme.onSurface),
                contentDescription = null
            )
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    text = user.fullName ?: "",
                    style = MaterialTheme.typography.titleLarge,
                    color = LocalContentColor.current
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = user.uName,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
                )
                Row {
                    Button(
                        modifier = Modifier.weight(1f),
//                        shape = MaterialTheme.shapes.medium,
                        onClick = onAcceptRequest
                    ) {
                        Text("Accept")
                    }
                    Spacer(Modifier.width(16.dp))
                    OutlinedButton(
                        modifier = Modifier.weight(1f),
//                        shape = MaterialTheme.shapes.medium,
                        onClick = onRejectRequest
                    ) {
                        Text("Reject")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendRequestsScreen(modifier: Modifier = Modifier) {
    val viewModel = koinViewModel<FriendRequestViewModel>()
    LaunchedEffect(Unit) {
        viewModel.getFriendRequests()
    }
    val requests by viewModel.friendRequests.collectAsState()
    val requestItems by derivedStateOf {
        requests.items
    }
    val requestsLoadState by derivedStateOf {
        requests.loadState
    }
    val listState = rememberLazyListState()
    LaunchedEffect(Unit) {
        watchListStateForPagination(listState = listState, loadState = requestsLoadState) {
            if (requestItems.size < viewModel.pageSize)
                return@watchListStateForPagination
            viewModel.getFriendRequests(requestItems.size / viewModel.pageSize)
        }
    }
    Scaffold(
        containerColor = Color.Transparent,
        modifier = modifier,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.blurColor),
                title = { Text("Friend Requests") })
        }) { innerPadding ->
        if (requestItems.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No Friend Requests!", color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f))
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(horizontal = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(requestItems) { friendRequest ->
                    FriendRequestItem(
                        user = friendRequest.user,
                        onAcceptRequest = {
                            viewModel.acceptFriendRequest(friendRequest.chatID, friendRequest.user.id)
                        }, onRejectRequest = {
                            viewModel.rejectFriendRequest(friendRequest.chatID, friendRequest.user.id)
                        })
                }
                if (requestsLoadState.append == LoadState.Loading) {
                    item {
                        Box(Modifier.fillMaxWidth()) { CircularProgressIndicator() }
                    }
                }
            }
        }
    }
}
