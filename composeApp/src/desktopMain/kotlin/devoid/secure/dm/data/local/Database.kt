package devoid.secure.dm.data.local

import androidx.room.Room
import androidx.room.RoomDatabase
import java.io.File


fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    val dbFile = File(System.getProperty("java.io.tmpdir"), "secure_dm.db")
    return Room.databaseBuilder<AppDatabase>(
        name = dbFile.absolutePath,
    )
}
