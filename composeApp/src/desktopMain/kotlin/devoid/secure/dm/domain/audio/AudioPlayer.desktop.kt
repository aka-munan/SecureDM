package devoid.secure.dm.domain.audio


import co.touchlab.kermit.Logger
import devoid.secure.dm.domain.files.getExtentionFromName
import javafx.application.Platform
import javafx.scene.media.Media
import javafx.scene.media.MediaPlayer
import javafx.util.Duration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

actual class AudioPlayer actual constructor() {

    private val TAG = "AudioPlayer"
    private var callback: AudioPLayerCallback? = null
    private var mediaPlayer: MediaPlayer? = null
    private var isPlaying = false
    private val scope= CoroutineScope(Dispatchers.IO)

    actual val duration: Int
        get() = mediaPlayer?.media?.duration?.toMillis()?.toInt() ?: 0
    actual val progress: Int = mediaPlayer?.currentTime?.toMillis()?.toInt() ?: 0
    actual var autoPlay: Boolean = true

    actual fun setCallback(callback: AudioPLayerCallback?) {
        this.callback = callback
    }
    init {
        try {
            Platform.startup {}
        }catch (e: Exception){
            Logger.e(tag = TAG, throwable = e, messageString = "failed to start javafx runtime")
        }
    }
    actual suspend fun init(uri: String) {
        callback?.onLoading()
        mediaPlayer?.dispose()
        scope.launch {
            try {
                val media = Media(uri)
                mediaPlayer = MediaPlayer(media).apply {
                    setOnReady {
                        Logger.i(tag = TAG, messageString = "media player ready")
                        callback?.onReady()
                        this.isAutoPlay = autoPlay
                    }
                    setOnMarker { Logger.i(tag = TAG, messageString = "mediaEvent" + it) }
                    currentTimeProperty().addListener { _, _, newValue ->
                        val position = newValue.toMillis().div(totalDuration.toMillis()).toFloat()
                        callback?.onProgress(position)
                    }
                    setOnEndOfMedia {
                        isPlaying = false
                        callback?.onPause()
                    }
                    setOnPaused {
                        isPlaying = false
                        callback?.onPause()
                    }
                    setOnError {
                        Logger.e(throwable = mediaPlayer?.media?.error, tag = TAG, messageString = "failed to play audio")
                        release()
                    }
                    setOnPlaying {
                        isPlaying = true
                        callback?.onPlay()
                    }
                }
            }catch (e:Exception){
                Logger.e(tag = TAG,e){
                    "failed to initialise audio player"
                }
                isPlaying = false
                release()
            }
        }

    }

    actual fun isPlaying() = isPlaying

    actual fun play() {
        if (mediaPlayer!=null && mediaPlayer?.currentTime == mediaPlayer?.media?.duration){
            mediaPlayer?.seek(Duration.ZERO)
            callback?.onPlay()
            isPlaying = true
        }
        mediaPlayer?.play()
    }

    actual fun pause() {
        mediaPlayer?.pause()
    }

    actual fun seekTo(milli: Int) {
        mediaPlayer?.seek(Duration.millis(milli.toDouble()))
    }

    actual fun release() {
        isPlaying = false
        mediaPlayer?.dispose()
        mediaPlayer = null
        callback?.onPause()
        callback?.onStop()
    }

}

fun isRemoteFile(source: String): Boolean {
    return (source.startsWith("http://") || source.startsWith("https://"))
}

fun fetchMediaWithHeaders(url: String, headers: Map<String, String>): String {
    val client = HttpClient.newHttpClient()
    val requestBuilder = HttpRequest.newBuilder(URI.create(url))

    headers.forEach { (key, value) ->
        requestBuilder.header(key, value)
    }

    val request = requestBuilder.build()
    val response = client.send(request, HttpResponse.BodyHandlers.ofInputStream())
    val tempFile = File.createTempFile("media", getExtentionFromName(url))
    tempFile.deleteOnExit()
    response.body().use { input ->
        tempFile.outputStream().use { output ->
            input.copyTo(output)
        }
    }
    return tempFile.toURI().toString()
}
