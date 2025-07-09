package devoid.secure.dm.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FriendRequest(
    @SerialName("chat_id")
    val chatID:String,
    @SerialName("request_status")
    val status: FriendRequestStatus,
    @SerialName("profile")
    val user: UserClass.RemoteUser
)
