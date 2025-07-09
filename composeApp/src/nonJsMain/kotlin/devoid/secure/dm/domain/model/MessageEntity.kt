package devoid.secure.dm.domain.model

import androidx.compose.runtime.Stable
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "message_entity")
@Stable
data class MessageEntity(
    @PrimaryKey
    val messageId: String = "",
    val senderId:String,
    val chatId: String,
    val text: String,
    val replyTo: String?=null,
    val synced:Boolean = false,
    val seen: Boolean = false,
    val date: Long,
    val attachmentId :String?=null
)