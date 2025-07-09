package devoid.secure.dm.domain.model

import androidx.compose.runtime.Stable
import androidx.room.Entity

@Entity(tableName = "chats_entity", primaryKeys = ["chatId", "profileId"])
@Stable
data class ChatItemEntity(
    val chatId: String,
    val profileId: String,
    val unseenCount: Int
)
