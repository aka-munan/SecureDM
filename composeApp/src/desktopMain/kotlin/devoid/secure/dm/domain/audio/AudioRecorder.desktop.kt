package devoid.secure.dm.domain.audio
import devoid.secure.dm.domain.files.SharedFile
import devoid.secure.dm.domain.files.SharedFileImpl
import kotlinx.atomicfu.AtomicBoolean
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.io.IOException
import java.io.File
import javax.sound.sampled.*
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

actual class AudioRecorder actual constructor() {
    private val audioFormat: AudioFormat = AudioFormat(
        16000f,   // Sample Rate
        16,       // Sample Size in bits
        1,        // Channels (Stereo)
        true,     // Signed
        false     // Little Endian
    )
    private var targetDataLine: TargetDataLine? = null
    private var isRecording = false
    private val scope = CoroutineScope(Dispatchers.IO)
    private var outputFile : File? = null

    @OptIn(ExperimentalUuidApi::class)
    actual fun record() {
        if (isRecording)
            return
        val info = DataLine.Info(TargetDataLine::class.java, audioFormat)
        if (!AudioSystem.isLineSupported(info)) {
            throw LineUnavailableException("Microphone not supported with this audio format.")
        }
        targetDataLine = AudioSystem.getLine(info) as TargetDataLine
        targetDataLine?.apply {
            open(audioFormat)
            start()
            isRecording = true
            scope.launch {
                outputFile = File.createTempFile("secure-dm/recording"+ Uuid.random().toString(), ".wav")
                writeAudioData()
            }
        }
    }
    private fun writeAudioData() {
        try {
            AudioSystem.write(AudioInputStream(targetDataLine), AudioFileFormat.Type.WAVE, outputFile)
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
    }


    actual fun stop(): SharedFile? {
        if (!isRecording) return null
        isRecording = false
        targetDataLine?.apply {
            stop()
            close()
        }
        return outputFile?.let { SharedFileImpl(it.toURI().toString()) }
    }

    actual fun deleteRecording(): Boolean {
        return outputFile?.delete()==true
    }
}