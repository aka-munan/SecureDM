package devoid.secure.dm.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "attachments")
data class AttachmentEntity(
    @PrimaryKey
    val id :String,
    val messageId: String,
    val fileUri :String,
    val name:String,
    val size:Long,
    val duration:Int?=null,
    val type: AttachmentType,
)
