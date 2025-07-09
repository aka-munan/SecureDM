package devoid.secure.dm.domain.model

import androidx.compose.ui.platform.LocalUriHandler
import androidx.core.bundle.Bundle
import androidx.navigation.NavType
import com.eygraber.uri.UriCodec
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class User (
    val id: String,
    val email: String,
    @SerialName("full_name")
    val fullName: String? = null,
    @SerialName("username")
    val uName: String? = null,
    val bio: String? = null,
    @SerialName("avatar_url")
    val avatarUrl: String? = null,
)

enum class FriendRequestStatus{
    ACCEPTED,REJECTED,PENDING
}
@Serializable
sealed interface UserClass{
    @Serializable
    data class LocalUser(
        val id: String,
        val email: String,
        @SerialName("full_name")
        val fullName: String? = null,
        @SerialName("username")
        val uName: String? = null,
        val bio: String? = null,
        @SerialName("avatar_url")
        val avatarUrl: String? = null,
    )
    @Serializable
    data class RemoteUser(
        val id: String,
        @SerialName("username")
        val uName:String,
        @SerialName("full_name")
        val fullName: String?=null,
        val bio: String?=null,
        @SerialName("avatar_url")
        val avatarUrl: String?=null,
    )
}
class UserNavType : NavType<UserClass.RemoteUser>(isNullableAllowed = false) {
    override fun get(bundle: Bundle, key: String): UserClass.RemoteUser? {
        return  bundle.getString(key)?.let {
            return Json.decodeFromString(bundle.getString(key) ?: return null)
        }
    }

    override fun parseValue(value: String): UserClass.RemoteUser {
        return Json.decodeFromString(UriCodec.decode(value))
    }

    override fun serializeAsValue(value: UserClass.RemoteUser): String {
        return UriCodec.encode(Json.encodeToString(value))
    }
    override fun put(bundle: Bundle, key: String, value: UserClass.RemoteUser) {
        bundle.putString(key, Json.encodeToString(value))
    }
}
