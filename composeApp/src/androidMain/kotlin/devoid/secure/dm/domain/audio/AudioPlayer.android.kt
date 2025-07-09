package devoid.secure.dm.domain.audio

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import co.touchlab.kermit.Logger
import kotlinx.coroutines.*
import org.koin.core.context.GlobalContext

actual class AudioPlayer actual constructor() {
    val context = GlobalContext.get().get<Context>()
    private var player: MediaPlayer? = null
    private var callback: AudioPLayerCallback? = null
    private var progressUpdateJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)
    private var isPlaying = false
    actual fun isPlaying(): Boolean = isPlaying
    actual var autoPlay: Boolean = false
    actual fun play() {
        player?.apply {
            start()
            progressUpdateJob?.cancel()
            progressUpdateJob = scope.launch {
                while (this@AudioPlayer.isPlaying && isActive) {
                    val progress = currentPosition / (duration * 1f)
                    callback?.onProgress(progress)
                    if (progress == 1f)
                        callback?.onPause()
                    delay(200)
                }
            }
            this@AudioPlayer.isPlaying = true
            callback?.onPlay()
        }
    }

    actual fun pause() {
        player?.apply {
            this@AudioPlayer.isPlaying = false
            pause()
            callback?.onPause()
        }
    }

    actual suspend fun init(uri: String) {
        try {
            progressUpdateJob?.cancel()
            progressUpdateJob = null
            isPlaying = false
            withContext(Dispatchers.IO) {
                callback?.onLoading()
                player?.release()
                player = MediaPlayer.create(context, Uri.parse(uri))
                player?.setOnCompletionListener {
                    callback?.onPause()
                    seekTo(0)
                }
                player?.setOnPreparedListener {
                    callback?.onReady()
                    if (autoPlay) {
                        play()
                    }
                }
                player?.setOnErrorListener { mp, what, extra ->
                    Logger.e("Media player error")
                    callback?.onStop()
                    true
                }
            }
        }catch (e:Exception){
            Logger.e(e){
                "failed to init audio player"
            }
            callback?.onStop()
        }

    }

    actual fun release() {
        player?.release()
        player = null
        isPlaying = false
        callback?.onStop()
    }

    actual fun seekTo(milli: Int) {
        player?.seekTo(milli)
    }

    actual val duration: Int
        get() = player?.duration ?: throw IllegalStateException("AudioPlayer not initialised!")
    actual val progress: Int
        get() = player?.currentPosition ?: throw IllegalStateException("AudioPlayer not initialised!")

    actual fun setCallback(callback: AudioPLayerCallback?) {
        this.callback = callback
    }
}