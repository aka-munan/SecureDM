package devoid.secure.dm.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profiles")
data class ProfileEntity (
    @PrimaryKey
    val id: String,
    val uName:String,
    val fullName: String?=null,
    val bio: String?=null,
    val avatarUrl: String?=null,
)