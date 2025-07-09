package devoid.secure.dm.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import devoid.secure.dm.domain.model.AttachmentEntity
import devoid.secure.dm.domain.model.ChatItemEntity
import devoid.secure.dm.domain.model.MessageEntity
import devoid.secure.dm.domain.model.ProfileEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

@Database(
    entities = [MessageEntity::class, ChatItemEntity::class, ProfileEntity::class, AttachmentEntity::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract val messageDao: MessageDao
    abstract val chatItemDao: ChatItemDao
    abstract val profileDao: ProfileDao
    abstract val attachmentDao: MessageAttachmentDao
}

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}

fun getRoomDatabase(
    builder: RoomDatabase.Builder<AppDatabase>
): AppDatabase {
    return builder
        .fallbackToDestructiveMigrationOnDowngrade(true)
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
}
