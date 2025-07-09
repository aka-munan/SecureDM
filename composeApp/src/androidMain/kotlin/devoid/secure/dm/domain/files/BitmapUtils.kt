package devoid.secure.dm.domain.files

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import co.touchlab.kermit.Logger

object BitmapUtils {
    fun getBitmapFromUri(uri: Uri, contentResolver: ContentResolver): Bitmap? {
        return try {
            contentResolver.openInputStream(uri).use {
                BitmapFactory.decodeStream(it)
            }
        } catch (e: Exception) {
            Logger.i(e) { "failed to open uri: $uri" }
            null
        }
    }
}