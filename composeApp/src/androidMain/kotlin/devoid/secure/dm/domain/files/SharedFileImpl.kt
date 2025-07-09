package devoid.secure.dm.domain.files

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import java.io.ByteArrayOutputStream

class SharedFileImpl(private val contentResolver: ContentResolver, override val uri: String) : SharedFile {

    override fun toByteArray(): ByteArray? {
        val baos = ByteArrayOutputStream()
        val buffer = ByteArray(size = 1024)
        contentResolver.openInputStream(Uri.parse(uri)).use {
            while (it?.read(buffer) != -1) {
                baos.write(buffer)
            }
        }
        return baos.toByteArray()
    }

    override fun getFileSize(): Long {
        contentResolver.query(Uri.parse(uri), null, null, null)?.run {
            val sizeIndex = getColumnIndex(OpenableColumns.SIZE)
            moveToFirst()
            val size = getLong(sizeIndex)
            close()
            return size
        } ?: return -1
    }

    override fun getFileName(): String? {
        return contentResolver.query(Uri.parse(uri), null, null, null)?.run {
            val sizeIndex = getColumnIndex(OpenableColumns.DISPLAY_NAME)
            moveToFirst()
            val name = getString(sizeIndex)
            close()
            name
        }
    }
}