package devoid.secure.dm.domain.model

import androidx.compose.runtime.Stable
import androidx.room.Embedded
import androidx.room.Relation

@Stable
data class LocalChatItem(
    val chatId:String,
    val lastMessageId: String,
    val profileId: String,
    val unseenCount: Int,
    @Relation(parentColumn = "lastMessageId", entityColumn = "messageId", entity = MessageEntity::class)
//    @Embedded(prefix = "sub_")
    val lastMessage: LocalMessage,
    @Relation(parentColumn = "profileId", entityColumn = "id", entity = ProfileEntity::class)
    val profileEntity: ProfileEntity
)
