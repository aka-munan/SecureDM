package devoid.secure.dm.domain

import devoid.secure.dm.domain.model.*
import kotlinx.datetime.*

fun Message.toMessageEntity(synced: Boolean = false): MessageEntity {
    val localDate = Instant.parse(date)
    return MessageEntity(
        messageId = messageId,
        senderId = senderId,
        chatId = chatId,
        text = text,
        replyTo = replyTo?.messageId,
        synced = synced,
        seen = seen,
        date = localDate.toEpochMilliseconds(),
        attachmentId = attachment?.id
    )
}

fun MessageEntity.toMessage(): Message {
    return Message(
        messageId,
        senderId,
        chatId,
        text,
        seen = seen,
        date = Instant.fromEpochMilliseconds(date).toString(),
        attachment = null
    )
}

fun UserClass.RemoteUser.toProfileEntity(): ProfileEntity {
    return ProfileEntity(id, uName, fullName, bio, avatarUrl)
}

fun ProfileEntity.toRemoteUser(): UserClass.RemoteUser {
    return UserClass.RemoteUser(id, uName, fullName, bio, avatarUrl)
}

fun ChatItem.toChatItemEntity(): ChatItemEntity {
    return ChatItemEntity(
        chatId = lastMessage.chatId,
        profileId = profile.id,
        unseenCount = unseenCount
    )
}

fun LocalChatItem.toChatItem(): ChatItem {
    return ChatItem(
        chatId = chatId,
        profile = profileEntity.toRemoteUser(),
        lastMessage = lastMessage.toMessage(),
        unseenCount = unseenCount
    )
}

fun MessageAttachment.toAttachmentsEntity(): AttachmentEntity {
    return AttachmentEntity(id=id, messageId = messageId, fileUri = fileUri, name =  name,size=size,duration = duration,type= type)
}

fun AttachmentEntity.toAttachment(): MessageAttachment {
    return MessageAttachment(id=id, messageId = messageId, fileUri =  fileUri,name= name,size=size, duration = duration, type= type)
}

fun LocalMessage.toMessage(): Message {
    return Message(
        messageId,
        senderId,
        chatId,
        text,
        replyToMessageEntities?.getOrNull(0)?.toMessage(),
        seen,
        Instant.fromEpochMilliseconds(date).toString(),
        attachment?.getOrNull(0)?.toAttachment()
    )
}