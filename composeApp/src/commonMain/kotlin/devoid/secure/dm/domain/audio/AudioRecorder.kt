package devoid.secure.dm.domain.audio

import devoid.secure.dm.domain.files.SharedFile

expect class AudioRecorder() {
    fun record()
    fun stop():SharedFile?
    fun deleteRecording():Boolean
}