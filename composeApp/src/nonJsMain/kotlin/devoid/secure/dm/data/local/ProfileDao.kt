package devoid.secure.dm.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import devoid.secure.dm.domain.model.ProfileEntity

@Dao
interface ProfileDao {
    @Upsert
    suspend fun upsertAll(users: List<ProfileEntity>)

    @Upsert
    suspend fun upsert(user: ProfileEntity)

    @Query("DELETE FROM profiles")
    suspend fun clearAll()

    @Query("SELECT * FROM profiles WHERE id = :profileId")
    suspend fun getSingle(profileId:String): ProfileEntity

    @Query("SELECT COUNT(*) FROM profiles WHERE id = :profileId")
    suspend fun count(profileId:String):Int
}