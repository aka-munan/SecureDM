package devoid.secure.dm.domain.files

import co.touchlab.kermit.Logger
import devoid.secure.dm.domain.model.MessageAttachment
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.io.asSink
import java.io.File


actual object AttachmentDownloader {
    actual fun downloadFile(
        attachment: MessageAttachment,
        overrideDuplicate: Boolean
    ): Flow<DownloadStatus> = flow {
        val httpClient = HttpClient()
        try {
            val home = System.getProperty("user.home")
            val path = File("$home/Downloads")
            var file = File(path, attachment.name)
            if (!overrideDuplicate) {
                val ext = getExtentionFromName(attachment.name)
                val nameWithoutExt = attachment.name.replace(ext, "")
                var num = 1
                while (file.exists()) {
                    file = File(path, "$nameWithoutExt($num)$ext")
                    num += 1
                }
            }
            if (!path.exists()) path.mkdirs()
            file.createNewFile()
            httpClient.prepareGet(attachment.fileUri) {}.execute { response ->
                val fos = file.outputStream().asSink()
                val dataChannel = response.bodyAsChannel()
                var bytesRead = 0L
                emit(DownloadStatus.Started)
                fos.use {
                    while (!dataChannel.exhausted()) {
                        val chunk = dataChannel.readRemaining()
                        bytesRead += chunk.remaining
                        emit(DownloadStatus.Progress((attachment.size * 1f) / bytesRead))
                        chunk.transferTo(fos)
                    }
                }
                Logger.i("file downloaded path: ${file.absolutePath}")
                emit(DownloadStatus.Completed)
            }
        } catch (e: Exception) {
            emit(DownloadStatus.Failure(e))
        }
    }.flowOn(Dispatchers.IO)
}