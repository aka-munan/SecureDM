package devoid.secure.dm.domain.model

import androidx.compose.runtime.Stable
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Relation
import kotlinx.serialization.SerialName

@Stable
data class LocalMessage(
    val messageId: String,
    val senderId: String,
    val chatId: String,
    val text: String,
    val replyTo: String?=null,
    @Relation(entity = MessageEntity::class, parentColumn = "replyTo", entityColumn = "messageId")
    val replyToMessageEntities: List<MessageEntity>? = null,
    val synced: Boolean = false,
    val seen: Boolean = false,
    val date: Long,
    val attachmentId: String? = null,
    @Relation(entity = AttachmentEntity::class, parentColumn = "attachmentId", entityColumn = "id")
//    @Embedded(prefix = "at_")
    val attachment: List<AttachmentEntity>? = null
)
