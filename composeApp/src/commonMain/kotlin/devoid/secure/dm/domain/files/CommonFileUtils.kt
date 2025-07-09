package devoid.secure.dm.domain.files

import kotlin.math.min

object CommonFileUtils {
    fun formatFileSize(bytes: Long,decimals:Int = 1): String {
        if (bytes <= 0) {
            return "0 B"
        }

        val kByte = 1024.0
        val mByte = kByte * 1024
        val gByte = mByte * 1024
        val tByte = gByte * 1024
        val unit = when {
            bytes < kByte -> "B"
            bytes < mByte -> "KB"
            bytes < gByte -> "MB"
            bytes < tByte -> "GB"
            else -> "TB"
        }
        return buildString {
            var value = bytes.toDouble()
            do {
                value /= 1024.0
            } while (value > 1024)
            val valueString = "$value".run {
                val index = indexOfFirst { it == '.' }
                substring(startIndex = 0, endIndex = min(length,index+decimals+1))
            }
            append(valueString)
            append(" $unit")
        }
    }
}