package devoid.secure.dm.domain.model

data class Connection(
    val fullName: String,
    val uName: String,
    val avatarUrl: String?,
    val lastMessage: Message?,
    val newMessagesCount: Int
)
