package devoid.secure.dm.domain.model

import androidx.compose.runtime.Stable
import kotlinx.datetime.Clock
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

@Stable
@Serializable
data class Message(
    @SerialName("messageid")
    val messageId: String,
    @SerialName("sender_id")
    val senderId: String,
    @SerialName("chat_id")
    val chatId: String,
    val text: String,
    @SerialName("reply_to")
    val replyTo: Message? = null,
    val seen: Boolean = false,
    val date: String,
    val attachment: MessageAttachment? = null
) {
    companion object{
        val EMPTY = Message("","","","", date =Clock.System.now().toString())
        fun createMessage(chatId: String, senderId: String, messageId: String, text: String,replyTo: Message?=null,attachment: MessageAttachment?=null): Message{
            return Message(
                messageId = messageId,
                text = text,
                senderId = senderId,
                chatId = chatId,
                replyTo = replyTo,
                date = Clock.System.now().toString(),
                attachment = attachment
            )
        }
    }
    fun toJsonObject(): JsonObject =
        buildJsonObject {
            put("p_message_id",messageId)
            put("p_chat_id", chatId)
            put("p_text", text)
            put("p_reply_to", replyTo?.messageId)
            if (attachment!=null){
                put("p_attach_url",attachment.fileUri)
                put("p_attach_type",attachment.type.name)
                put("p_attach_name",attachment.name)
                put("p_attach_duration",attachment.duration)
                put("p_attach_size",attachment.size)
            }
        }
}
@Stable
@Serializable
data class MessageAttachment(
    val id :String,
    @SerialName("message_id")
    val messageId: String,
    @SerialName("file_url")
    val fileUri :String,
    val name:String,
    @SerialName("file_size")
    val size: Long,
    val duration:Int?=null,
    val type: AttachmentType,
)

@Stable
@Serializable
enum class AttachmentType {
    AUDIO, VIDEO, IMAGE, DOCUMENT
}