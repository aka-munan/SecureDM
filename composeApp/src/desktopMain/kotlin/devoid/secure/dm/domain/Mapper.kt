package devoid.secure.dm.domain

import devoid.secure.dm.domain.model.AttachmentType
import devoid.secure.dm.domain.model.Message
import devoid.secure.dm.domain.model.MessageAttachment
import org.quartz.JobDataMap

fun JobDataMap.toMessage(): Message {
    return Message(
        messageId = get("messageid") as String,
        senderId = get("sender_id") as String,
        chatId = get("chat_id")!! as String,
        text = get("text") as String,
        seen = get(("seen")) as Boolean,
        date = get("date") as String,
        replyTo = (get("reply_to") as Message?),
        attachment = null
    )
}

fun Map<String, Any?>.toAttachment(): MessageAttachment {
    return MessageAttachment(
        id = get("attachment_id") as String,
        messageId = get("messageid") as String,
        name = get("name") as String,
        fileUri = get("file_uri") as String,
        size = get("size") as Long,
        duration = get("duration") as Int?,
        type = AttachmentType.valueOf(get("type") as String),
    )
}

fun Message.toJobDataMap(): JobDataMap {
    return JobDataMap(
        mapOf(
            "messageid" to messageId,
            "sender_id" to senderId,
            "chat_id" to chatId,
            "text" to text,
            "seen" to seen,
            "date" to date,
            "reply_to" to replyTo?.copy(replyTo=null)//make sure to remove this field otherwise will trigger recursion
        ).plus(attachment?.run {
            mapOf(
                "attachment_id" to id,
                "file_uri" to fileUri,
                "name" to name,
                "size" to size,
                "type" to type.name,
                "duration" to duration,
            )
        } ?: mapOf()
        )
    )
}

fun MessageAttachment.toJobDataMap(chatId: String): JobDataMap {
    return JobDataMap(
        mapOf(
            "attachment_id" to id,
            "messageid" to messageId,
            "file_uri" to fileUri,
            "name" to name,
            "size" to size,
            "type" to type.name,
            "duration" to duration,
            "chat_id" to chatId
        )
    )
}