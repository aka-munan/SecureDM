package devoid.secure.dm.data.remote.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.allowHardware
import coil3.toBitmap
import okio.IOException

object BitmapUtils {
    suspend fun getBitmapFromUrl(url: String, size: Int, context: Context): Result<Bitmap?> {
        val loader = ImageLoader(context)
        val imageRequest = ImageRequest.Builder(context)
            .data(url)
            .allowHardware(false)
            .build()
        val result = loader.execute(imageRequest)
        return if (result is SuccessResult) {
            Result.success(result.image.toBitmap(result.image.width,result.image.height))
        } else
            Result.failure(IOException("failed to get bitmap from url"))
    }
}