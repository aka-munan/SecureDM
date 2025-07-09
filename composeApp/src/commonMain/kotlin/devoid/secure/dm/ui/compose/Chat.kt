package devoid.secure.dm.ui.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import co.touchlab.kermit.Logger
import coil3.compose.AsyncImage
import devoid.secure.dm.domain.*
import devoid.secure.dm.domain.files.CommonFileUtils
import devoid.secure.dm.domain.files.getExtentionFromName
import devoid.secure.dm.domain.model.*
import devoid.secure.dm.ui.state.AudioPlayerState
import devoid.secure.dm.ui.state.AudioPlayerStateManager
import devoid.secure.dm.ui.theme.backgroundBrush
import devoid.secure.dm.ui.viewmodel.HomeViewModel
import devoid.secure.dm.ui.viewmodel.PSHomeViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import secure_dm.composeapp.generated.resources.*
import kotlin.time.ExperimentalTime

@Composable
expect fun ChatScreen(
    chatId: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    onNavigateToProfile: (profileId: String) -> Unit
)

@Composable
expect fun ChatsLayoutItems(
    viewModel: PSHomeViewModel, onItemClick: (ChatItem) -> Unit, onNavigateToProfile: (profileId: String) -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: PSHomeViewModel,
    onProfileClick: (profileId: String) -> Unit,
    onItemClick: (ChatItem) -> Unit
) {
    var isSearchBarExpanded by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    val padding by animateIntAsState(targetValue = if (!isSearchBarExpanded) 16 else 0)
    val focusManager = LocalFocusManager.current
    var newChatDialogVisible by remember { mutableStateOf(false) }
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val density = LocalDensity.current
    val expandedHeightPx =
        with(LocalDensity.current) { TopAppBarDefaults.TopAppBarExpandedHeight.toPx().coerceAtLeast(0f) }
    val topBarHeight by remember(density) {
        derivedStateOf {
            with(density) {
                (expandedHeightPx + scrollBehavior.state.heightOffset).toDp()
            }
        }
    }
    SideEffect {
        if (scrollBehavior.state.heightOffsetLimit != -expandedHeightPx) {
            scrollBehavior.state.heightOffsetLimit = -expandedHeightPx
        }
    }
    val inputField = @Composable {
        SearchBarDefaults.InputField(
            modifier = Modifier,
            query = query,
            expanded = isSearchBarExpanded,
            onExpandedChange = { isSearchBarExpanded = !isSearchBarExpanded },
            onQueryChange = {
                query = it
                if (it.trim().length >= 3) viewModel.findUserByUname(it)
            },
            onSearch = { focusManager.clearFocus() },
            placeholder = { Text("Search...") },
            leadingIcon = {
                if (isSearchBarExpanded) {
                    IconButton(
                        onClick = { isSearchBarExpanded = false },
                    ) {
                        Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Back")
                    }
                } else {
                    Icon(Icons.Default.Search, contentDescription = null)
                }
            })
    }

    Scaffold(
        modifier = modifier, containerColor = Color.Transparent, floatingActionButton = {
            FloatingActionButton(onClick = { newChatDialogVisible = true }) {
                Icon(Icons.Rounded.Add, contentDescription = null)
            }
        }) { paddingValues ->

        Box(Modifier.nestedScroll(scrollBehavior.nestedScrollConnection), contentAlignment = Alignment.TopCenter) {
            Column {
                Spacer(Modifier.height(topBarHeight))
                // platform specific items composable
                ChatsLayoutItems(viewModel = viewModel, onItemClick = onItemClick, onNavigateToProfile = onProfileClick)
            }

            val fullScreen = LocalDisplaySize.current != DisplaySize.LARGE
            SelectFriendDialog(
                viewModel = viewModel,
                visible = newChatDialogVisible,
                fullScreen = fullScreen,
                onItemClick = {//neads fix
                    onItemClick(ChatItem(chatId = "", profile = it, lastMessage = Message.EMPTY, unseenCount = 0))
                    newChatDialogVisible = false
                },
                onDismiss = { newChatDialogVisible = false })
            SearchBar(
                modifier = Modifier.padding(horizontal = padding.dp).offset {
                    IntOffset(
                        0, scrollBehavior.state.heightOffset.toInt()
                    )
                },
                inputField = inputField,
                expanded = isSearchBarExpanded,
                onExpandedChange = { isSearchBarExpanded = !isSearchBarExpanded }) {
                val searchResults by viewModel.searchUserQueryResult.collectAsState()
                Box(Modifier.fillMaxSize()) {
                    if (query.trim().length < 3) return@Box
                    searchResults.onSuccess {
                        if (it.isEmpty()) {
                            Text("No Users Found!", Modifier.align(Alignment.Center), color = Color.Gray)
                        } else {
                            UserSearchResults(users = it, onItemClick = { onProfileClick(it.id) })
                        }
                    }.onFailure {
                        println(it)
                        Text("Unexpected Error Occurred", modifier = Modifier.align(Alignment.Center))
                    }
                }
            }
        }

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectFriendDialog(
    viewModel: HomeViewModel,
    visible: Boolean,
    fullScreen: Boolean = false,
    onItemClick: (UserClass.RemoteUser) -> Unit,
    onDismiss: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    var isSearchBarExpanded by mutableStateOf(false)
    var searchQuery by mutableStateOf("")
    val modifier = Modifier.then(
        if (fullScreen) Modifier.fillMaxSize()
        else Modifier.widthIn(max = 420.dp).heightIn(max = 600.dp)
    )
    val inputField = @Composable {
        SearchBarDefaults.InputField(
            modifier = Modifier,
            query = searchQuery,
            expanded = isSearchBarExpanded,
            onExpandedChange = { isSearchBarExpanded = !isSearchBarExpanded },
            onQueryChange = {
                searchQuery = it
//                    if (it.trim().length >= 3) viewModel.findUserByUname(it)
            },
            onSearch = { focusManager.clearFocus() },
            placeholder = { Text("Search friends...") },
            leadingIcon = {
                if (isSearchBarExpanded) {
                    IconButton(
                        onClick = { isSearchBarExpanded = false },
                    ) {
                        Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Back")
                    }
                } else {
                    Icon(Icons.Default.Search, contentDescription = null)
                }
            })
    }

    if (visible) {
        LaunchedEffect(Unit) {
            viewModel.getFriends()
        }
        Dialog(properties = DialogProperties(usePlatformDefaultWidth = false), onDismissRequest = onDismiss) {
            val searchBarPadding by animateIntAsState(targetValue = if (isSearchBarExpanded) 0 else 16)
            val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
            val expandedHeightPx =
                with(LocalDensity.current) { TopAppBarDefaults.TopAppBarExpandedHeight.toPx().coerceAtLeast(0f) }
            val density = LocalDensity.current
            val spaceHeight by remember(density) {
                derivedStateOf {
                    with(density) {
                        (expandedHeightPx + scrollBehavior.state.heightOffset).toDp()
                    }
                }
            }
            SideEffect {
                if (scrollBehavior.state.heightOffsetLimit != -expandedHeightPx) {
                    scrollBehavior.state.heightOffsetLimit = -expandedHeightPx
                }
            }
            val pagingItems by viewModel.friends.collectAsState()
            val friends by derivedStateOf {
                pagingItems.items
            }
            val loadState by derivedStateOf {
                pagingItems.loadState
            }
            val listState = rememberLazyListState()
            LaunchedEffect(listState) {
                watchListStateForPagination(listState = listState, loadState = loadState) {
                    if (friends.size < viewModel.pageSize) return@watchListStateForPagination
                    viewModel.getFriends(pageNumber = friends.size / viewModel.pageSize)
                }
            }
            Scaffold(
                modifier = modifier.clip(if (LocalDisplaySize.current == DisplaySize.SMALL) MaterialTheme.shapes.small else MaterialTheme.shapes.large)
                    .background(MaterialTheme.colorScheme.backgroundBrush()), containerColor = Color.Transparent
            ) { paddingValues ->
                Box(Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)) {
                    Column {
                        Spacer(Modifier.height(spaceHeight))
                        if (loadState.refresh is LoadState.Loading) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        } else if (friends.isEmpty()) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("NO friends found!")
                            }
                        } else {
                            LazyColumn(
                                state = listState,
                                modifier = Modifier.padding(paddingValues).fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                itemsIndexed(friends) { index, user ->
                                    UserSearchItem(modifier = Modifier.fillMaxWidth().height(62.dp).clickable {
                                        onItemClick(user)
                                    }, user = user)
                                    if (index != friends.size - 1) {
                                        HorizontalDivider()
                                    }
                                }
                                if (loadState.append is LoadState.Loading) {
                                    item {
                                        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                            CircularProgressIndicator()
                                        }
                                    }
                                }
                            }
                        }

                    }
                    SearchBar(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = searchBarPadding.dp).offset {
                            IntOffset(
                                0, scrollBehavior.state.heightOffset.toInt()
                            )
                        },
                        inputField = inputField,
                        expanded = isSearchBarExpanded,
                        onExpandedChange = { isSearchBarExpanded = !isSearchBarExpanded }) {

                    }
                }
            }
        }
    }
}


@Composable
fun MessageCompose(
    modifier: Modifier = Modifier,
    shouldRTL: Boolean = false,
    isSynced: Boolean = false,
    urlPreviewEnabled: Boolean = false,
    onGetUrlMetadata: (url: String) -> Flow<UrlMetadata?>,
    message: Message,
    audioPlayerStateManager: AudioPlayerStateManager?,
    onDownloadAttachment: () -> Unit
) {
    Logger.i("loadig message $message")
    Column(modifier, horizontalAlignment = if (shouldRTL) Alignment.End else Alignment.Start) {
        if (message.replyTo != null) {
            MessageReply(message = message.replyTo, modifier = Modifier.wrapContentWidth().height(IntrinsicSize.Min))
            Spacer(Modifier.height(4.dp))
        }
        Row() {
            if (!isSynced) {
                CircularProgressIndicator(
                    Modifier.size(10.dp).align(Alignment.Bottom).padding(end = 2.dp), strokeWidth = 1.dp
                )
            }
            if (message.attachment != null) {
                val attachmentShape = if (message.text.isNotBlank()) {
                    roundedShape(top = 16.dp, bottom = 2.dp)
                } else {
                    MaterialTheme.shapes.medium
                }
                Column(verticalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.width(IntrinsicSize.Min)) {
                    when (message.attachment.type) {
                        AttachmentType.AUDIO -> {
                            val audioPlayerState =
                                audioPlayerStateManager?.state?.map { if (it?.id == message.messageId) it else null }
                                    ?.collectAsState(null)?.value

                            Row(
                                Modifier.clip(MaterialTheme.shapes.medium)
                                    .background(MaterialTheme.colorScheme.inversePrimary).padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                AudioPlayer(
                                    state = audioPlayerState, duration = message.attachment.duration ?: 0, onSeek = {
                                        if (audioPlayerState is AudioPlayerState.Ready && audioPlayerState.id == message.messageId) audioPlayerStateManager.onUpdateState(
                                            audioPlayerState.copy(
                                                playerProgress = it
                                            ), null
                                        )
                                    }) {
                                    audioPlayerStateManager?.onUpdateState((audioPlayerState.takeIf { it?.id == message.messageId } as AudioPlayerState.Ready?)?.copy(
                                        isPlaying = !(audioPlayerState as AudioPlayerState.Ready).isPlaying
                                    ) ?: AudioPlayerState.Ready(//new audio player request
                                        id = message.messageId,
                                        isPlaying = true,
                                        duration = message.attachment.duration ?: 0,
                                        playerProgress = 0f
                                    ), message.attachment)
                                }
                            }
                        }

                        AttachmentType.IMAGE -> {
                            ImageMessage(
                                modifier = Modifier.sizeIn(minWidth = 100.dp, minHeight = 100.dp).clip(attachmentShape),
                                attachment = message.attachment
                            )
                        }

                        AttachmentType.VIDEO -> {

                        }

                        AttachmentType.DOCUMENT -> {
                            DocumentAttachment(
                                modifier = Modifier.fillMaxWidth(),
                                shape = attachmentShape,
                                attachment = message.attachment,
                                onDownloadClick = onDownloadAttachment
                            )
                        }
                    }
                    if (message.text.isNotBlank()) {
                        Column {
                            val urlResult = getUrlFromString(message.text)
                            if (urlResult.urls.isNotEmpty()) {
                                val annotatedString = getUrlAnnotatedString(
                                    urlResult.originalString,
                                    urlResult.startIndex,
                                    urlResult.endIndex
                                )
                                TextMessage(
                                    modifier = Modifier.fillMaxWidth(),
                                    text = annotatedString,
                                    shape = roundedShape(top = 2.dp, bottom = 12.dp)
                                )
                                return@Column
                            }
                            TextMessage(
                                modifier = Modifier.fillMaxWidth(),
                                text = message.text,
                                shape = roundedShape(top = 2.dp, bottom = 12.dp)
                            )
                        }
                    }
                }

            } else {
                Column {
                    val urlResult = getUrlFromString(message.text)
                    if (urlResult.urls.isNotEmpty()) {
                        if (urlPreviewEnabled) {
                            val urlMetadata by onGetUrlMetadata(urlResult.urls.first()).collectAsState(null)
                            urlMetadata?.apply {
                                Card {
                                    UrlPreview(Modifier.heightIn(max = 350.dp), title, subTitle, image, true)
                                }
                            }
                        }
                        val annotatedString = getUrlAnnotatedString(
                            urlResult.originalString, urlResult.startIndex, urlResult.endIndex
                        )
                        TextMessage(text = annotatedString)
                        return@Column
                    }
                    TextMessage(text = message.text)
                }
            }
        }
    }
}

@Composable
fun TextMessage(
    modifier: Modifier = Modifier.widthIn(min = 32.dp), text: String, shape: Shape = MaterialTheme.shapes.medium
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors().copy(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = shape,
    ) {
        Column(
            Modifier.padding(vertical = 6.dp, horizontal = 8.dp).widthIn(min = 70.dp),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text,
                modifier = Modifier.align(Alignment.Start),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
//                        TimeFooter(date)
        }
    }
}

@Composable
fun TextMessage(
    modifier: Modifier = Modifier.widthIn(min = 32.dp),
    text: AnnotatedString,
    shape: Shape = MaterialTheme.shapes.medium
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors().copy(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = shape,
    ) {
        Column(
            Modifier.padding(vertical = 6.dp, horizontal = 8.dp).widthIn(min = 70.dp),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text,
                modifier = Modifier.align(Alignment.Start),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
//                        TimeFooter(date)
        }
    }
}


@Composable
fun DocumentAttachment(
    modifier: Modifier = Modifier.fillMaxWidth(),
    shape: Shape = CardDefaults.outlinedShape,
    attachment: MessageAttachment,
    onDownloadClick: () -> Unit
) {
    OutlinedCard(modifier, shape = shape) {
        Row(
            Modifier.padding(8.dp).height(IntrinsicSize.Min)
        ) {
            Image(
                painterResource(Res.drawable.document),
                modifier = Modifier.height(50.dp).padding(4.dp).aspectRatio(1f),
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)),
                contentDescription = null
            )
            Spacer(Modifier.width(8.dp))
            Column(Modifier.fillMaxHeight().weight(1f), verticalArrangement = Arrangement.Center) {
                Text(
                    text = attachment.name,
                    maxLines = 1,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    overflow = TextOverflow.Ellipsis,
                    color = LocalContentColor.current
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "${getExtentionFromName(attachment.name).run { takeLast(length - 1) }} • ${
                        CommonFileUtils.formatFileSize(
                            attachment.size
                        )
                    }",
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.labelSmall
                )
            }
            Image(
                painterResource(Res.drawable.download),
                contentDescription = null,
                modifier = Modifier.fillMaxHeight().aspectRatio(1f).padding(4.dp).clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onSurface).clickable(onClick = onDownloadClick).padding(8.dp),
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSecondary)
            )
        }
    }
}


@OptIn(ExperimentalResourceApi::class)
@Composable
fun ChatItem(
    modifier: Modifier = Modifier, chatItem: ChatItem, onProfileClick: (profileId: String) -> Unit
) {
    val lastMessage = chatItem.lastMessage
    val sender = chatItem.profile
    val laseMessageLabel = getLabelFromMessage(lastMessage)
    val avatar = sender.avatarUrl ?: Res.drawable.person
    Row(modifier = modifier) {
        AsyncImage(
            modifier = Modifier.fillMaxHeight().padding(4.dp).aspectRatio(1f).clip(CircleShape)
                .clickable(onClick = { onProfileClick(sender.id) }), model = avatar, contentDescription = null
        )
        Column(modifier = Modifier.padding(horizontal = 8.dp)) {
            Row {
                Text(
                    text = sender.fullName ?: "Unknown User",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = getRelativeTime(lastMessage.date),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )

            }
            Row {
                Text(
                    text = laseMessageLabel,
                    modifier = Modifier.weight(1f),
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    softWrap = false,
                    color = if (lastMessage.seen || chatItem.unseenCount == 0) Color.Gray else LocalContentColor.current
                )
                if (chatItem.unseenCount > 0) {
                    Card(
                        shape = CircleShape,
                        colors = CardDefaults.cardColors()
                            .copy(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Text(
                            text = chatItem.unseenCount.toString(),
                            modifier = Modifier.padding(horizontal = 8.dp),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AttachmentDisplayer(
    modifier: Modifier = Modifier, attachments: List<MessageAttachment>, onRemoveItem: (index: Int) -> Unit
) {
    LazyRow(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        itemsIndexed(attachments) { index, attachment ->
            AttachedFileDisplayerItem(
                Modifier.heightIn(max = 100.dp).widthIn(max = 160.dp), attachment = attachment
            ) {
                onRemoveItem(index)
            }
        }
    }
}


@Composable
fun AttachedFileDisplayerItem(modifier: Modifier = Modifier, attachment: MessageAttachment, onClearClick: () -> Unit) {
    Box(
        modifier
    ) {
        Box(
            Modifier.padding(top = 8.dp, end = 8.dp).clip(
                RoundedCornerShape(12.dp)
            ).background(MaterialTheme.colorScheme.surfaceContainerHigh)
        ) {
            when (attachment.type) {
                AttachmentType.VIDEO -> TODO()
                AttachmentType.IMAGE -> {
                    Row {
                        AsyncImage(
                            model = attachment.fileUri,
                            modifier = Modifier.fillMaxHeight().wrapContentWidth(),
                            contentDescription = null
                        )
                    }
                }

                AttachmentType.DOCUMENT, AttachmentType.AUDIO -> {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(8.dp)) {
                        Image(
                            painter = painterResource(Res.drawable.document),
                            modifier = Modifier.size(24.dp),
                            colorFilter = ColorFilter.tint(
                                LocalContentColor.current
                            ),
                            contentDescription = null
                        )
                        Spacer(Modifier.width(4.dp))
                        Column {
                            Text(
                                attachment.name,
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                CommonFileUtils.formatFileSize(attachment.size),
                                maxLines = 1,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }
        }
        OutlinedCard(
            onClick = onClearClick,
            shape = CircleShape,
            colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.inversePrimary),
            modifier = Modifier.size(24.dp).align(Alignment.TopEnd)
        ) {
            Icon(Icons.Rounded.Close, contentDescription = null, modifier = Modifier.padding(2.dp))
        }
    }
}


@Composable
fun AddAttachment(
    modifier: Modifier = Modifier,
    expandedSize: Dp = 110.dp,
    isSecondaryBtnVisible: Boolean = true,
    onToggleVisibility: () -> Unit,
    onCameraClick: () -> Unit,
    onFileClick: () -> Unit,
    onImageClick: () -> Unit
) {
    val secondaryBtnAnimValue by animateFloatAsState(
        if (isSecondaryBtnVisible) 1f else 0f,
        animationSpec = tween(200, easing = if (isSecondaryBtnVisible) EaseOutQuad else EaseInQuad)
    )
    val secondaryScale by derivedStateOf { 0.5f + (secondaryBtnAnimValue / 2f) }
    val secondaryRotation by derivedStateOf { -90 * (1f - secondaryBtnAnimValue) }
    Box(modifier = modifier) {
        AnimatedVisibility(
            visible = isSecondaryBtnVisible,
            modifier = Modifier.size(expandedSize),
            exit = fadeOut(tween(200, easing = if (isSecondaryBtnVisible) EaseOutQuad else EaseInQuad))
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                ElevatedCard(
                    shape = CircleShape,
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceTint),
                    modifier = Modifier.size(48.dp).scale(secondaryScale)
                        .offset(y = (-66 * (secondaryBtnAnimValue - 1)).dp).align(Alignment.TopStart)
                        .rotate(secondaryRotation),
                    onClick = onCameraClick
                ) {
                    Icon(
                        painterResource(Res.drawable.camera),
                        modifier = Modifier.fillMaxSize().padding(12.dp),
                        contentDescription = null,
                    )
                }
                ElevatedCard(
                    shape = CircleShape,
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceTint),
                    modifier = Modifier.size(48.dp).scale(secondaryScale)
                        .offset(x = (66 * (secondaryBtnAnimValue - 1)).dp).align(Alignment.BottomEnd)
                        .rotate(secondaryRotation),
                    onClick = onImageClick
                ) {
                    Icon(
                        painterResource(Res.drawable.image),
                        modifier = Modifier.fillMaxSize().padding(12.dp),
                        contentDescription = null,
                    )
                }
                ElevatedCard(
                    shape = CircleShape,
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceTint),
                    modifier = Modifier.padding(end = 8.dp, top = 8.dp).size(48.dp).scale(secondaryScale)
                        .offset(x = (54 * (secondaryBtnAnimValue - 1)).dp, (-54 * (secondaryBtnAnimValue - 1)).dp)
                        .align(Alignment.TopEnd).rotate(secondaryRotation),
                    onClick = onFileClick
                ) {
                    Icon(
                        painterResource(Res.drawable.document),
                        modifier = Modifier.fillMaxSize().padding(12.dp),
                        contentDescription = null,
                    )
                }
            }
        }
        IconButton(//primary btn
            modifier = Modifier.scale(1f - (secondaryBtnAnimValue / 10f)).clip(CircleShape)
                .rotate(45 * secondaryBtnAnimValue).background(MaterialTheme.colorScheme.secondary)
                .align(Alignment.BottomStart), onClick = onToggleVisibility
        ) {
            Icon(
                Icons.Rounded.Add,
                tint = MaterialTheme.colorScheme.onSecondary,
                contentDescription = null,
            )
        }
    }
}

fun getLabelFromMessage(message: Message): String {
    if (message.attachment == null) return if (message.replyTo != null) {
        "replied to a message: ${message.text}"
    } else {
        message.text
    }
    return when (message.attachment.type) {
        AttachmentType.AUDIO -> "Sent an Audio file."
        AttachmentType.VIDEO -> "Sent a Video."
        AttachmentType.IMAGE -> "Sent an Image."
        AttachmentType.DOCUMENT -> "Sent a Document."
    }
}

@Composable
fun TimeFooter(date: Instant) {
    Text(
        text = formatTimeHeader(date),
        modifier = Modifier.padding(vertical = 4.dp),
        style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
    )
}


fun Modifier.enabled(enabled: Boolean = true): Modifier = this.then(
    Modifier.pointerInput(enabled) {
        if (!enabled) {
            detectTapGestures {}
        }
    }.alpha(if (enabled) 1f else 0.6f)
)


@OptIn(ExperimentalTime::class)
@Composable
fun MessageReplyBottomBarAttachment(modifier: Modifier = Modifier, message: Message, onClear: () -> Unit) {
    Row(modifier.then(Modifier.height(IntrinsicSize.Min)), verticalAlignment = Alignment.CenterVertically) {
        VerticalDivider(
            thickness = 3.dp,
            modifier = Modifier.padding(vertical = 4.dp).clip(MaterialTheme.shapes.medium),
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.width(8.dp))
        MessageCompose(
            message = message,
            isSynced = true,
            modifier = Modifier.heightIn(max = 100.dp).clip(MaterialTheme.shapes.large).enabled(false),
            audioPlayerStateManager = null,
            onDownloadAttachment = { },
            urlPreviewEnabled = false,
            onGetUrlMetadata = { flowOf(null) })
        Spacer(Modifier.weight(1f))
        IconButton(onClick = onClear) {
            Icon(
                Icons.Rounded.Close, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun MessageReply(modifier: Modifier = Modifier, message: Message) {
    Row(modifier) {
        VerticalDivider(
            thickness = 3.dp,
            modifier = Modifier.fillMaxHeight().padding(vertical = 4.dp).clip(MaterialTheme.shapes.medium),
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.width(8.dp))
        MessageCompose(
            message = message,
            isSynced = true,
            modifier = Modifier.heightIn(max = 100.dp).clip(MaterialTheme.shapes.large).enabled(false),
            audioPlayerStateManager = null,
            onDownloadAttachment = { },
            urlPreviewEnabled = false,
            onGetUrlMetadata = { flowOf(null) })
    }
}
