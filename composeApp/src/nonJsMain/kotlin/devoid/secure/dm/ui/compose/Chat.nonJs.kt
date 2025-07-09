package devoid.secure.dm.ui.compose

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import androidx.paging.LoadState
import androidx.paging.PagingData
import co.touchlab.kermit.Logger
import coil3.compose.AsyncImage
import devoid.secure.dm.domain.*
import devoid.secure.dm.domain.date.TimeFormater
import devoid.secure.dm.domain.files.rememberCameraLauncher
import devoid.secure.dm.domain.files.rememberDocumentPicker
import devoid.secure.dm.domain.files.rememberGalleryManager
import devoid.secure.dm.domain.model.*
import devoid.secure.dm.domain.settings.LocalSettingConfig
import devoid.secure.dm.domain.settings.SettingsConfig
import devoid.secure.dm.ui.state.AudioPlayerState
import devoid.secure.dm.ui.state.AudioPlayerStateManager
import devoid.secure.dm.ui.theme.secondaryBackgroundBrush
import devoid.secure.dm.ui.viewmodel.AudioRecordState
import devoid.secure.dm.ui.viewmodel.ChatViewModel
import devoid.secure.dm.ui.viewmodel.PSHomeViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.koin.compose.viewmodel.koinViewModel
import secure_dm.composeapp.generated.resources.Res
import secure_dm.composeapp.generated.resources.person


@OptIn(ExperimentalResourceApi::class, ExperimentalMaterial3Api::class)
@Composable
actual fun ChatScreen(
    chatId: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier,
    onNavigateToProfile: (profileId: String) -> Unit
) {
    val viewModel = koinViewModel<ChatViewModel>()
    val targetUser by viewModel.getProfileByChatId(chatId).collectAsState(initial = null)
    val avatarUri =
        remember(targetUser) { targetUser?.avatarUrl ?: /*Res.getUri("files/person.svg")*/ Res.drawable.person }
    val pagingFlow = remember { viewModel.getMessagesFlow(chatId) }
    val messages = pagingFlow.collectAsLazyPagingItems()
    val currentUser by viewModel.currentUser.collectAsState()
    var textMessage by rememberSaveable { mutableStateOf("") }
    val attachments by viewModel.pickedAttachments.collectAsState()
    val snackbarState = remember { SnackbarHostState() }
    var permissionsManager: PermissionsManager? = remember { null }

    DisposableEffect(viewModel) {
        Logger.e("recomposed")
        onDispose {
            Logger.e("disposed")
        }
    }
    val permissionCallback = remember {
        object : PermissionCallback {
            override fun onPermissionStatus(permissionType: PermissionType, status: PermissionStatus) {
                if (permissionType == PermissionType.RECORD_AUDIO) {
                    if (status != PermissionStatus.GRANTED) {
                        viewModel.viewModelScope.launch {
                            val snackbarResult =
                                snackbarState.showSnackbar(
                                    "Grant Microphone permission.",
                                    actionLabel = "Settings"
                                )
                            if (snackbarResult == SnackbarResult.ActionPerformed) {
                                permissionsManager?.launchSettings()
                            }
                        }
                    }
                }
            }

        }
    }
    permissionsManager = rememberPermissionsManager(permissionCallback)
    val galleryManager = rememberGalleryManager { sharedFile ->
        Logger.i("image picked: ${sharedFile?.uri}")
        viewModel.onImagePicked(sharedFile)
    }
    val documentPicker = rememberDocumentPicker {
        viewModel.onDocumentPicked(it)
    }
    val cameraLauncher = rememberCameraLauncher {
        Logger.i("photo captured: ${it.getFileName()}")
        viewModel.onImagePicked(it)
    }
    var isAddAttachmentExpanded by remember { mutableStateOf(false) }
    val settingsConfig = LocalSettingConfig.current
    val messageStyle = settingsConfig.messageStyle
    val onSendBtnClick: () -> Unit = {
        if (textMessage.isNotBlank() || attachments.isNotEmpty()) {
            viewModel.sendMessages(
                chatId = chatId,
                sender = currentUser?.id ?: "",
                text = textMessage,
                viewModel.replyToMessage.value,
                attachments
            )
            viewModel.clearPickedAttachments()
            textMessage = ""
        }
    }
    val urlMetadata by viewModel.urlMetadataPreview.collectAsState()
    val urlDetector = rememberUrlDetector { startIndex, endIndex, url ->
        if (!settingsConfig.linkPreview) {
            return@rememberUrlDetector
        }
        Logger.i("url found: $url")
        viewModel.getUrlMetadata(url)
    }
    var recordAudio by remember { mutableStateOf(false) }
    if (recordAudio) {
        if (permissionsManager.isPermissionGranted(PermissionType.RECORD_AUDIO)) {
            viewModel.startRecordingAudio()
        } else {
            permissionsManager.askPermission(PermissionType.RECORD_AUDIO)
        }
    }
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(snackbarState) },
        topBar = {
            ChatTopBar(
                avatarUri,
                targetUser?.fullName ?: "Unknown User",
                onNavigateBack = onNavigateBack,
                onNavigateToProfile = { targetUser?.let { onNavigateToProfile(it.id) } })
        },
        bottomBar = {
            val audioRecordState by viewModel.audioRecordState.collectAsState()
            DisposableEffect(null) {
                onDispose {
                    if (audioRecordState is AudioRecordState.Recording)
                        viewModel.stopRecordingAudio()
                }
            }
            AnimatedContent(audioRecordState, contentKey = {
                it::class.qualifiedName
            }) {
                Box(
                    modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 16.dp).fillMaxWidth()
                        .wrapContentHeight(),
                    contentAlignment = Alignment.BottomStart
                ) {
                    when (it) {
                        AudioRecordState.NotRecording -> {
                            val replyToMessage by viewModel.replyToMessage.collectAsState()
                            Column(Modifier.fillMaxWidth()) {
                                if (replyToMessage != null) {
                                    MessageReplyBottomBarAttachment(
                                        Modifier.fillMaxWidth().clip(MaterialTheme.shapes.medium)
                                            .background(MaterialTheme.colorScheme.background)
                                            .padding(vertical = 4.dp, horizontal = 8.dp),
                                        message = replyToMessage!!,
                                        onClear = {
                                            viewModel.replyToMessage(null)
                                        })
                                    Spacer(Modifier.height(2.dp))
                                }
                                Box(contentAlignment = Alignment.BottomStart) {
                                    MessageComposer(
                                        modifier = Modifier
                                            .padding(start = 58.dp, bottom = 8.dp)
                                            .clip(RoundedCornerShape(30.dp))
                                            .background(MaterialTheme.colorScheme.secondaryBackgroundBrush())
                                            .border(
                                                width = 1.dp,
                                                shape = RoundedCornerShape(30.dp),
                                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
                                            ).animateContentSize { _, _ -> }
                                            .padding(horizontal = 8.dp),
                                        text = textMessage,
                                        onTextMessageChange = {
                                            urlDetector.textWatcher.watch(it)
                                            textMessage = it
                                        }, previews = {
                                            val displaySize = LocalDisplaySize.current
                                            AnimatedVisibility(attachments.isNotEmpty() || urlMetadata != null) {
                                                if (attachments.isNotEmpty()) {
                                                    AttachmentDisplayer(
                                                        Modifier.padding(8.dp),
                                                        attachments = attachments,
                                                        onRemoveItem = viewModel::removePickedAttachment
                                                    )
                                                } else if (urlMetadata != null) {
                                                    urlMetadata?.apply {
                                                        UrlPreview(
                                                            modifier = Modifier.heightIn(max = 400.dp),
                                                            title,
                                                            subTitle,
                                                            image,
                                                            vertical = displaySize == DisplaySize.SMALL
                                                        )
                                                    }
                                                }
                                            }
                                        },
                                        attachmentsPicked = attachments.isNotEmpty(),
                                        onSendBtnClick = onSendBtnClick,
                                        onLaunchGallery = { galleryManager.launch() },
                                        onRecordAudio = {
                                            recordAudio = true
                                        })
                                    AddAttachment(
                                        modifier = Modifier.padding(bottom = 12.dp, end = 12.dp),
                                        isSecondaryBtnVisible = isAddAttachmentExpanded,
                                        onToggleVisibility = { isAddAttachmentExpanded = !isAddAttachmentExpanded },
                                        onImageClick = {
                                            isAddAttachmentExpanded = false
                                            galleryManager.launch()
                                        },
                                        onCameraClick = {
                                            isAddAttachmentExpanded = false
                                            cameraLauncher.launch()
                                        },
                                        onFileClick = {
                                            isAddAttachmentExpanded = false
                                            documentPicker.launch()
                                        })
                                }
                            }
                        }

                        is AudioRecordState.Recorded -> {
                            val audioPlayerState by viewModel.audioPlayerState.map { it.takeIf { it?.id == null } }
                                .collectAsState(null)
                            Logger.i("audio recorded player:$audioPlayerState")
                            DisposableEffect(Unit) {
                                viewModel.initAudioPlayer(it.file, false)
                                onDispose { viewModel.releaseAudioPlayer() }
                            }
                            val duration = when (audioPlayerState) {
                                is AudioPlayerState.Ready -> {
                                    (audioPlayerState as AudioPlayerState.Ready).duration
                                }

                                else -> 0
                            }
                            AudioRecorded(
                                audioPlayerState = audioPlayerState,
                                onDelete = {
                                    viewModel.pauseAudio()
                                    viewModel.deleteAudioRecording()
                                },
                                duration = duration,
                                onSeek = { fraction ->
                                    if (audioPlayerState is AudioPlayerState.Ready && audioPlayerState?.id == null) {
                                        viewModel.updateAudioState(
                                            (audioPlayerState as AudioPlayerState.Ready).copy(
                                                playerProgress = fraction
                                            ), null
                                        )
                                    }
                                },
                                onTogglePlayerState = {
                                    when (audioPlayerState) {
                                        is AudioPlayerState.Ready -> {
                                            val state = audioPlayerState as AudioPlayerState.Ready
                                            if (state.isPlaying) {
                                                viewModel.pauseAudio()
                                            } else
                                                viewModel.playAudio()
                                        }

                                        else -> {
                                            viewModel.initAudioPlayer(it.file, true)
                                        }
                                    }
                                }
                            ) {
                                viewModel.pauseAudio()
                                viewModel.sendAudioMessage(chatId = chatId, duration)
                            }
                        }

                        is AudioRecordState.Recording -> {
                            val recordedDuration = TimeFormater.formatToString(it.millis)
                            AudioRecording(recordedDuration = recordedDuration) {
                                viewModel.stopRecordingAudio()
                            }
                        }
                    }
                }

            }
        }) { innerPadding ->
        DisposableEffect(Unit) {
            onDispose { viewModel.releaseAudioPlayer() }
        }
        Box(Modifier.fillMaxSize().padding(horizontal = 16.dp).padding(bottom = 8.dp)) {
            targetUser?.let {
                MessagesList(
                    messageStyle = messageStyle,
                    contentPadding = innerPadding,
                    currentUser = currentUser,
                    targetUser = it,
                    messages = messages,
                    audioPlayerStateManager = object : AudioPlayerStateManager(viewModel.audioPlayerState) {
                        override fun onUpdateState(
                            state: AudioPlayerState.Ready,
                            attachment: MessageAttachment?
                        ) {
                            viewModel.updateAudioState(state, attachment)
                        }
                    },
                    onDeleteMessage = viewModel::deleteMessage,
                    onDownloadAttachment = {
                        viewModel.downloadAttachment(it) { error ->
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
                    }, onReply = viewModel::replyToMessage,
                    settingsConfig = settingsConfig,
                    onGetUrlMetadata = viewModel::getUrlMetaDataFlow
                )
            }
        }
    }
}

@Composable
fun MessagesList(
    modifier: Modifier = Modifier,
    settingsConfig: SettingsConfig,
    messageStyle: MessageStyle = MessageStyle.SENDER_ON_RHS,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    currentUser: UserClass.LocalUser?,
    targetUser: UserClass.RemoteUser,
    messages: LazyPagingItems<LocalMessage>,
    audioPlayerStateManager: AudioPlayerStateManager? = null,
    onDownloadAttachment: (MessageAttachment) -> Unit,
    onDeleteMessage: (messageId: String) -> Unit,
    onReply: (Message) -> Unit,
    onGetUrlMetadata: (url: String) -> Flow<UrlMetadata?>
) {
    BoxWithConstraints(modifier) {
        val messageMaxWidth = (maxWidth * 0.8f).coerceAtMost(400.dp)
        if (messages.loadState.refresh is LoadState.Loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            val dropDownState = rememberMessageDropDownState()
            var selectedMessage by remember { mutableStateOf<LocalMessage?>(null) }
            val clipboardManager = LocalClipboardManager.current
            var fullscreenAttachment by remember { mutableStateOf<MessageAttachment?>(null) }
            LaunchedEffect(selectedMessage) {
                if (selectedMessage == null) {
                    dropDownState.isVisible = false
                    return@LaunchedEffect
                }
                dropDownState.containsText = selectedMessage?.text?.isNotBlank() == true
                dropDownState.canDownload = selectedMessage?.attachmentId != null
                Logger.i("slectedMessage: $selectedMessage")
                dropDownState.canUnsend =
                    selectedMessage?.senderId != null && selectedMessage?.senderId == currentUser?.id
                if (messageStyle == MessageStyle.SENDER_ON_RHS && selectedMessage?.senderId == currentUser?.id) {
                    dropDownState.offset = dropDownState.offset.copy(x = 5000.dp)
                }
                dropDownState.isVisible = true
            }
            LaunchedEffect(Unit) {
                dropDownState.callback = object : MessageDropDownCallback {
                    override fun onReply() {
                        onReply(selectedMessage!!.toMessage())
                        selectedMessage = null
                    }

                    override fun onCopy() {
                        clipboardManager.setText(AnnotatedString(text = selectedMessage?.text ?: ""))
                        selectedMessage = null
                    }

                    override fun onUnsend() {
                        onDeleteMessage(selectedMessage!!.messageId)
                        selectedMessage = null
                    }

                    override fun onDownload() {
                        selectedMessage?.attachment?.getOrNull(0)?.toAttachment()?.run {
                            onDownloadAttachment(this)
                        }
                    }
                }
            }

            LazyColumn(
                contentPadding = contentPadding,
                modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp),
                reverseLayout = true
            ) {
                items(messages.itemCount) { index ->
                    val localMessage = messages[index] ?: return@items
                    var position = Offset.Zero
                    Box(
                        Modifier.fillMaxWidth()
                            .onGloballyPositioned {
                                position = it.positionInParent()
                            }) {
                        val isMessageSentByCurrentUser = localMessage.senderId == currentUser?.id
                        val shouldRTL = isMessageSentByCurrentUser && messageStyle == MessageStyle.SENDER_ON_RHS
                        val align = if (shouldRTL) {
                            Alignment.TopEnd
                        } else Alignment.BottomStart
                        if (!shouldRTL) {
                            if (index == 0 || messages[index - 1]?.senderId != localMessage.senderId) {
                                val avatar =
                                    if (localMessage.senderId == targetUser.id) targetUser.avatarUrl
                                        ?: "" else currentUser?.avatarUrl
                                        ?: ""

                                AsyncImage(
                                    model = avatar,
                                    modifier = Modifier.size(30.dp).clip(CircleShape).align(align),
                                    contentDescription = null
                                )

                            }
                        }
                        MessageCompose(
                            modifier = Modifier.align(align).offset(x = if (shouldRTL) 0.dp else 35.dp, y = 0.dp)
                                .widthIn(max = messageMaxWidth)
                                .rightClickMenu { offset ->
                                    val clickPosition = position.plus(offset)
                                    dropDownState.offset = DpOffset(clickPosition.x.toDp(), clickPosition.y.toDp())
                                    selectedMessage = localMessage
                                }
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onLongPress = { offset ->
                                            val clickPosition = position.plus(offset)
                                            dropDownState.offset =
                                                DpOffset(clickPosition.x.toDp(), clickPosition.y.toDp())
                                            selectedMessage = localMessage
                                        },
                                        onTap = {
                                            if (localMessage.attachment?.getOrNull(0)?.type == AttachmentType.IMAGE || localMessage.attachment?.getOrNull(
                                                    0
                                                )?.type == AttachmentType.VIDEO
                                            ) {//on tap
                                                fullscreenAttachment = localMessage.toMessage().attachment
                                            } else null
                                        }
                                    )

                                },
                            isSynced = localMessage.synced,
                            message = localMessage.toMessage(),
                            shouldRTL = shouldRTL,
                            audioPlayerStateManager = audioPlayerStateManager,
                            urlPreviewEnabled = settingsConfig.linkPreview,
                            onGetUrlMetadata = onGetUrlMetadata
                        ) {
                            onDownloadAttachment(localMessage.attachment!!.getOrNull(0)!!.toAttachment())
                        }
                    }
                    if (shouldShowDateHeader(messages, index)) {
                        DateHeader(Instant.fromEpochMilliseconds(localMessage.date))
                    }
                }
                if (messages.loadState.append is LoadState.Loading) {
                    item {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
            MessageDropDown(
                state = dropDownState,
                onDismiss = { selectedMessage = null },
                date = selectedMessage?.date?.let { getRelativeTime(Instant.fromEpochMilliseconds(it)) } ?: ""
            )
            if (fullscreenAttachment != null) {
                FullscreenAttachment(fullscreenAttachment!!, onClose = { fullscreenAttachment = null })
            }
        }
    }
}

@Composable
actual fun ChatsLayoutItems(
    viewModel: PSHomeViewModel,
    onItemClick: (ChatItem) -> Unit,
    onNavigateToProfile: (profileId: String) -> Unit
) {
    val pagingFlow = (viewModel.getChatItemsFlow() as Flow<PagingData<ChatItem>>)
    val items = pagingFlow.collectAsLazyPagingItems()
    ChatItems(items, onProfileClicked = onNavigateToProfile, onItemClick = {
        viewModel.clearUnseenCount(it.chatId)
        onItemClick(it)
    })
}

@Composable
fun ChatItems(
    items: LazyPagingItems<ChatItem>,
    onItemClick: (ChatItem) -> Unit,
    onProfileClicked: (profileId: String) -> Unit
) {
    if (items.loadState.refresh == LoadState.Loading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (items.itemCount == 0) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                "Click on the \"+\" FAB icon to start chatting!",
                textAlign = TextAlign.Center,
                color = LocalContentColor.current.copy(alpha = 0.6f)
            )
        }
    }

    LazyColumn(Modifier.padding(top = 8.dp)) {
        items(items.itemCount) { index ->
            val chatItem = items[index] ?: return@items
            ChatItem(
                modifier = Modifier.height(72.dp)
                    .clickable(onClick = {
                        onItemClick(chatItem)
                    })
                    .padding(8.dp),
                chatItem = chatItem,
                onProfileClick = onProfileClicked
            )
            if (index != items.itemCount - 1) HorizontalDivider()
        }
        if (items.loadState.append is LoadState.Loading) {
            item {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

fun shouldShowDateHeader(messages: LazyPagingItems<LocalMessage>, currentIndex: Int): Boolean {
    // Always show header for first message
    if (currentIndex == messages.itemCount - 1) return true

    val currentDate = Instant.fromEpochMilliseconds(messages[currentIndex]?.date ?: 0)
        .toLocalDateTime(TimeZone.currentSystemDefault())
    val previousDate =
        Instant.fromEpochMilliseconds(messages[currentIndex + 1]?.date ?: 0)
            .toLocalDateTime(TimeZone.currentSystemDefault())
    // Show header if dates are different
    return currentDate.date != previousDate.date
}

@Composable
fun DateHeader(date: Instant) {
    Text(
        text = formatDateHeader(date),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
    )
}
