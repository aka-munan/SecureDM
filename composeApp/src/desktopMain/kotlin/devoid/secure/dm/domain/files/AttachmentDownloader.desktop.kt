package devoid.secure.dm.domain.files

import devoid.secure.dm.domain.model.MessageAttachment
import kotlinx.coroutines.flow.Flow


actual object AttachmentDownloader {
    actual fun downloadFile(
        attachment: MessageAttachment,
        overrideDuplicate: Boolean
    ): Flow<DownloadStatus> {
        TODO("Not yet implemented")
    }
}