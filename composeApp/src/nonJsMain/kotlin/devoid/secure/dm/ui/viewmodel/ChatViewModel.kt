package devoid.secure.dm.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.network.parseGetRequest
import com.fleeksoft.ksoup.nodes.Document
import devoid.secure.dm.data.local.LocalChatItemRepository
import devoid.secure.dm.data.local.LocalMessageRepository
import devoid.secure.dm.domain.BackgroundTaskManager
import devoid.secure.dm.domain.MessageUploadTask
import devoid.secure.dm.domain.TaskState
import devoid.secure.dm.domain.UrlMetadata
import devoid.secure.dm.domain.audio.AudioPLayerCallback
import devoid.secure.dm.domain.audio.AudioPlayer
import devoid.secure.dm.domain.audio.AudioRecorder
import devoid.secure.dm.domain.files.*
import devoid.secure.dm.domain.model.*
import devoid.secure.dm.domain.toProfileEntity
import devoid.secure.dm.domain.toUrlMetaData
import devoid.secure.dm.ui.state.AudioPlayerState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import uuidV7

class ChatViewModel(
    private val localMessageRepository: LocalMessageRepository,
    userRepository: UserRepository,
    private val chatRepository: ChatRepository,
    private val localChatItemRepository: LocalChatItemRepository,
    private val backgroundTaskManager: BackgroundTaskManager
) : ViewModel() {
    private val TAG = "ChatViewModel"
    private val audioPlayer = AudioPlayer()
    private val _audioPlayerState = MutableStateFlow<AudioPlayerState?>(null)
    val audioPlayerState = _audioPlayerState.asStateFlow()
    private val audioRecorder = AudioRecorder()
    private val _audioRecordState = MutableStateFlow<AudioRecordState>(AudioRecordState.NotRecording)
    val audioRecordState = _audioRecordState.asStateFlow()

    private val _pickedAttachments = MutableStateFlow<List<MessageAttachment>>(listOf())
    val pickedAttachments = _pickedAttachments.asStateFlow()
    private val _replyToMessage = MutableStateFlow<Message?>(null)
    val replyToMessage = _replyToMessage.asStateFlow()
    private val _urlMetaData = MutableStateFlow<UrlMetadata?>(null)
    val urlMetadataPreview = _urlMetaData.asStateFlow()
    init {
        viewModelScope.launch {
            backgroundTaskManager.completedTaskFlow.mapNotNull { it }.collect {
                if (it.taskState is TaskState.FAILURE) {
                    Logger.e(it.taskState.throwable) { "failed to send message" }
                } else if (it.taskState == TaskState.SUCCESS) {
                    localMessageRepository.markAsSynced(messageId = it.id)
                    println("message sent id: ${it.id}")
                }
            }
        }
    }

    val currentUser = userRepository.currentUser

    fun sendMessages(
        chatId: String,
        sender: String,
        text: String,
        replyTo: Message? = null,
        attachments: List<MessageAttachment>
    ) {
        _urlMetaData.value = null
        val msgText = text.trim('\n', ' ')
        val messages = mutableListOf<Message>()
        val messageId = uuidV7().toString()
        if (attachments.isEmpty()) {
            messages.add(
                Message.createMessage(
                    chatId = chatId,
                    senderId = sender,
                    messageId = messageId,
                    text = msgText,
                    replyTo=replyTo,
                    attachment = null
                )
            )
        } else {
            val msgAttachments = attachments.map { it.copy(messageId = messageId) }
            msgAttachments.forEachIndexed { index, item ->
                messages.add(
                    Message.createMessage(
                        chatId = chatId,
                        senderId = sender,
                        messageId = messageId,
                        text = if (index == 0) msgText else "",//text will be attached to first message only
                        replyTo = if (index == 0) replyTo else null,//reply to will be attached to first message only
                        attachment = item
                    )
                )
            }
        }
        viewModelScope.launch {
            messages.forEach {
                localMessageRepository.addMessage(it)
                val msgTask = MessageUploadTask(it.messageId)
                backgroundTaskManager.runTask(task = msgTask, it)
            }
        }
        _replyToMessage.value = null
    }

    fun getUrlMetadata(url: String?){
        if (url == null){
            _urlMetaData.value = null
            return
        }
        viewModelScope.launch {
            val doc: Document = Ksoup.parseGetRequest(url = url)
            val metadata = Ksoup.parseMetaData(element = doc)
            _urlMetaData.value = metadata.toUrlMetaData()
//            Logger.i("metedata fetched: ${urlMetadataPreview.value}")
        }
    }
    fun getUrlMetaDataFlow(url: String)=flow<UrlMetadata> {
        val doc: Document = Ksoup.parseGetRequest(url = url)
        val metadata = Ksoup.parseMetaData(element = doc)
        Logger.i("metedata fetched: ${urlMetadataPreview.value}")
        emit(metadata.toUrlMetaData())
    }

    fun getMessagesFlow(chatId: String?) = chatId?.let { localMessageRepository.getMessages(it).flow } ?: flowOf()

    fun getProfileByChatId(chatId: String) = flow<UserClass.RemoteUser> {
        val localProfile = localChatItemRepository.getProfileByChatId(chatId)
        localProfile?.let {
            emit(it)
        }
        chatRepository.getProfileByChatID(chatId).onSuccess {
            emit(it)
            localChatItemRepository.addProfile(it.toProfileEntity())
        }
    }

    fun onDocumentPicked(list: List<SharedFile>) {
        Logger.i("files picked: ${list.map { it.getFileName() }}")
        _pickedAttachments.update {
            it.plus(list.map { sharedFile ->
                MessageAttachment(
                    "",
                    "",
                    sharedFile.uri,
                    sharedFile.getFileName() ?: "unknown.txt",
                    sharedFile.getFileSize(),
                    type =
                        if (getExtentionFromName(
                                sharedFile.getFileName() ?: ""
                            ).removePrefix(".").lowercase() in ImageExtensions
                        ) AttachmentType.IMAGE else AttachmentType.DOCUMENT
                )
            })
        }
    }

    fun onImagePicked(file: SharedFile?) {
        file?.uri?.let { uri ->
            _pickedAttachments.update {
                it.plus(
                    MessageAttachment(
                        "",
                        "",
                        uri,
                        file.getFileName() ?: "unknown.jpg",
                        size = file.getFileSize(),
                        type = AttachmentType.IMAGE
                    )
                )
            }
        }
    }

    fun replyToMessage(message: Message?) {
        _replyToMessage.value = message
    }

    fun clearPickedAttachments() {
        _pickedAttachments.update { listOf() }
    }

    fun removePickedAttachment(index: Int) {
        _pickedAttachments.update {
            it.toMutableList().apply {
                removeAt(index)
            }
        }
    }

    fun startRecordingAudio() {
        viewModelScope.launch {
            val startTime = Clock.System.now().toEpochMilliseconds()
            var duration = 0L
            _audioRecordState.update { AudioRecordState.Recording(duration) }
            while (_audioRecordState.value is AudioRecordState.Recording) {
                _audioRecordState.update {
                    duration = Clock.System.now().toEpochMilliseconds() - startTime
                    AudioRecordState.Recording(duration)
                }
                delay(1_000)
            }
        }
        _audioRecordState.update { AudioRecordState.Recording(0) }
        try {
            audioRecorder.record()
        } catch (e: Exception) {
            Logger.e(e, tag = TAG) {
                "failed to record audio"
            }
            _audioRecordState.update { AudioRecordState.NotRecording }
        }
    }

    fun stopRecordingAudio() {
        viewModelScope.launch {
            audioRecorder.stop()?.let { outputFile ->
                _audioRecordState.update {
                    AudioRecordState.Recorded(outputFile)
                }
                Logger.i("audio recording saved path: ${outputFile.uri}")
            } ?: _audioRecordState.update {
                Logger.e(tag = TAG, IllegalStateException("audio player is not recording")) {
                    "failed to stop recording audio"
                }
                AudioRecordState.NotRecording
            }
        }
    }

    fun deleteAudioRecording() {
        audioRecorder.deleteRecording()
        _audioRecordState.update { AudioRecordState.NotRecording }
    }

    fun sendAudioMessage(chatId: String, duration: Int) {
        if (_audioRecordState.value is AudioRecordState.Recorded) {
            val file = (_audioRecordState.value as AudioRecordState.Recorded).file
            currentUser.value?.let { sender ->
                val attachments = listOf(
                    MessageAttachment(
                        "",
                        "",
                        fileUri = file.uri,
                        name = file.getFileName() ?: "audio_recording${getExtentionFromName(file.uri)}",
                        size = file.getFileSize(),
                        duration = duration,
                        type = AttachmentType.AUDIO
                    )
                )
                sendMessages(chatId, sender.id, "", null, attachments)
            }
        }
        _audioRecordState.update { AudioRecordState.NotRecording }
    }

    fun deleteMessage(messageId: String) {
        viewModelScope.launch {
            chatRepository.deleteMessage(messageId).onSuccess {
                localMessageRepository.deleteByMessageId(messageId)
            }.onFailure {
                Logger.e(tag = TAG, it) {
                    "failed to delete message id: $messageId"
                }
            }
        }
    }


    private fun initAudioPlayer(attachment: MessageAttachment, autoPlay: Boolean = true) {
        if (attachment.type != AttachmentType.AUDIO) {
            Logger.e(IllegalArgumentException("the attachment is not a type of AttachmentType.AUDIO")) {
                "failed to play audio"
            }
            return
        }
        viewModelScope.launch {
            if (_audioPlayerState.value?.id != attachment.messageId) {
                audioPlayer.apply {
                    setCallback(createAudioPlayerCallback(attachment.messageId))
                    this.autoPlay = autoPlay
                    init(attachment.fileUri)
                }
            }
            audioPlayer.seekTo(0)
        }
    }

    fun initAudioPlayer(file: SharedFile, autoPlay: Boolean) {
        viewModelScope.launch {
            audioPlayer.apply {
                setCallback(createAudioPlayerCallback(null))
                this.autoPlay = autoPlay
                init(file.uri)
                seekTo(0)
            }
        }
    }

    fun playAudio() {
        audioPlayer.play()
    }

    private fun createAudioPlayerCallback(messageId: String?): AudioPLayerCallback {
        return object : AudioPLayerCallback {
            override fun onLoading() {
                _audioPlayerState.update {
                    AudioPlayerState.Loading(messageId)
                }
            }

            override fun onReady() {
                _audioPlayerState.update {
                    AudioPlayerState.Ready(messageId, false, audioPlayer.duration, 0f)
                }
            }

            override fun onPlay() {
                _audioPlayerState.update {
                    it?.runCatching {
                        (it as AudioPlayerState.Ready?)?.copy(isPlaying = true)
                    }?.getOrNull()
                }
            }

            override fun onPause() {
                _audioPlayerState.update {
                    it?.runCatching {
                        (it as AudioPlayerState.Ready?)?.copy(isPlaying = false)
                    }?.getOrNull()
                }
            }

            override fun onStop() {
                _audioPlayerState.update {
                    it?.runCatching {
                        (it as AudioPlayerState.Ready?)?.copy(isPlaying = false, playerProgress = 0f)
                    }?.getOrNull()
                }
            }

            override fun onProgress(fraction: Float) {
                _audioPlayerState.update {
                    (it as AudioPlayerState.Ready?)?.copy(playerProgress = fraction)
                }
            }

        }
    }

    fun pauseAudio() {
        audioPlayer.pause()
    }

    fun releaseAudioPlayer() {
        audioPlayer.release()
        audioPlayer.setCallback(null)
    }

    fun updateAudioState(state: AudioPlayerState.Ready, attachment: MessageAttachment?) {
        try {
            if (audioPlayerState.value?.id != state.id) {//play request for different audio
                attachment?.run { initAudioPlayer(this, state.isPlaying) }
            }
            audioPlayer.seekTo((state.duration * state.playerProgress).toInt())
            if (audioPlayerState.value is AudioPlayerState.Ready) {
                if (state.isPlaying) {
                    if (state.playerProgress == 1f)
                        audioPlayer.seekTo(0)
                    playAudio()
                } else {
                    pauseAudio()
                }
            }
        } catch (e: Exception) {
            Logger.e(e) {
                "failed to update audio player state"
            }
        }
    }

    fun downloadAttachment(attachment: MessageAttachment, onComplete: (Exception?) -> Unit) {
        viewModelScope.launch {
            AttachmentDownloader.downloadFile(attachment).collect { status ->
                when (status) {
                    DownloadStatus.Completed -> {
                        Logger.i(tag = TAG, messageString = "Attachment download successfully")
                        onComplete(null)
                    }

                    is DownloadStatus.Failure -> {
                        Logger.e(tag = TAG, status.e) {
                            "Attachment download failed"
                        }
                        onComplete(status.e)
                    }

                    is DownloadStatus.Progress -> {
                        Logger.i(tag = TAG, messageString = "download progress: ${status.progress * 100}")
                    }

                    DownloadStatus.Started -> {
                        Logger.i(tag = TAG, messageString = "Attachment download started")
                    }
                }
            }
        }
    }
}

sealed class AudioRecordState() {
    data object NotRecording : AudioRecordState()
    data class Recording(val millis: Long) : AudioRecordState()
    data class Recorded(val file: SharedFile) : AudioRecordState()
}