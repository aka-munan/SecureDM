package devoid.secure.dm.domain.files

import devoid.secure.dm.domain.model.MessageAttachment
import kotlinx.coroutines.flow.Flow

expect object AttachmentDownloader {
    fun downloadFile(attachment: MessageAttachment,overrideDuplicate :Boolean= false):Flow<DownloadStatus>
}

sealed class DownloadStatus(){
    object Started:DownloadStatus()
    data class Progress(val progress:Float): DownloadStatus()
    data class Failure(val e:Exception): DownloadStatus()
    data object Completed:DownloadStatus()
}