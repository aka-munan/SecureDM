package devoid.secure.dm.domain.files

import UUID
import android.content.Context
import android.os.Environment
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object FileUtils {
    fun createTempFile(context: Context,ext:String):File{
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "TMP_" + timestamp + "_"
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return  File.createTempFile(imageFileName,ext,storageDir)
    }
}