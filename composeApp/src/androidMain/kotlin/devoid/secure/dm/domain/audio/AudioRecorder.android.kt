package devoid.secure.dm.domain.audio

import android.content.Context
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import androidx.core.net.toFile
import devoid.secure.dm.domain.files.SharedFile
import devoid.secure.dm.domain.files.SharedFileImpl
import org.koin.core.context.GlobalContext
import java.io.File
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

actual class AudioRecorder {
    private var tempFilePath: String? = null

    //    private val TAG = "AudioRecorder"
    val context = GlobalContext.get().get<Context>()
    var recorder: MediaRecorder? = null
    @OptIn(ExperimentalUuidApi::class)
    actual fun record() {
        if (recorder != null)
            throw IllegalStateException("recorder is already running")
        recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            MediaRecorder(context)
        else
            MediaRecorder()
        val outputFile = File.createTempFile("recording"+Uuid.random().toString(), ".m4a")
        tempFilePath = outputFile.toURI().toString()
        recorder?.apply {
            setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setOutputFile(outputFile)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            try {
                prepare()
            } catch (e: Exception) {
                recorder = null
                throw e
            }
            start()
        }
    }

    actual fun stop(): SharedFile? {
        recorder = recorder?.run {
            stop()
            release()
            null
        }
        return tempFilePath?.let {
            SharedFileImpl(context.contentResolver, it)
        }
    }

    actual fun deleteRecording(): Boolean {
        return tempFilePath?.let {
            Uri.parse(tempFilePath).toFile().delete()
            true
        } ?: false
    }
}