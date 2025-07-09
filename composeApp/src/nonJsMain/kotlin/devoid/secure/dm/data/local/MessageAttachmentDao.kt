package devoid.secure.dm.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import devoid.secure.dm.domain.model.AttachmentEntity

@Dao
interface MessageAttachmentDao {

    @Upsert
    suspend fun upsertAll(attachments: List<AttachmentEntity>)

    @Upsert
    suspend fun upsert(attachment: AttachmentEntity)

    @Query("DELETE FROM attachments")
    suspend fun clearAll()

    @Query("SELECT * FROM attachments " +
            "WHERE id = :id")
    suspend fun getById(id: String): AttachmentEntity?


    @Query("DELETE FROM attachments " +
            "WHERE id = :id" )
    suspend fun delete(id:String)
}