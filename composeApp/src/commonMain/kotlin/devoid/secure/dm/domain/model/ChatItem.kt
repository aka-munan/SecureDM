package devoid.secure.dm.domain.model

import androidx.compose.runtime.Stable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Stable
@Serializable
data class ChatItem(
    @SerialName("chat_id")
    val chatId: String,
    @SerialName("profile")
    val profile: UserClass.RemoteUser,
    @SerialName("message")
    val lastMessage: Message,
    @SerialName("unseen_count")
    val unseenCount:Int
)
