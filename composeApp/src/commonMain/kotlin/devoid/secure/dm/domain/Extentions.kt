package devoid.secure.dm.domain

import androidx.compose.ui.graphics.Shape
import devoid.secure.dm.ui.compose.DisplaySize
import devoid.secure.dm.ui.viewmodel.UserNameFormatException
import io.github.jan.supabase.exceptions.HttpRequestException
import io.github.jan.supabase.postgrest.exception.PostgrestRestException
import io.ktor.client.plugins.*
import kotlinx.datetime.*
import kotlinx.datetime.format.DateTimeFormat
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import kotlinx.datetime.format.byUnicodePattern

expect fun LocalDateTime.format(pattern:String):String

fun Int.toDisplaySize(): DisplaySize {
    return when (this) {
        in 0..425 -> DisplaySize.SMALL
        in 426..720 -> DisplaySize.MEDIUM
        else -> DisplaySize.LARGE
    }
}

fun Throwable.toSimplifiedError(): SimplifiedError {
    return when (this.cause) {
        is PostgrestRestException -> SimplifiedError("Server error!")
        is HttpRequestTimeoutException -> SimplifiedError("Requested timed out!")
        is HttpRequestException-> SimplifiedError("Network error!")
        is UserNameFormatException-> SimplifiedError(this.message?:"Username format error!")
        else-> SimplifiedError("Unknown error occurred!")
    }
}

data class SimplifiedError(val message: String = ""){
    override fun toString(): String {
        return message
    }
}



fun getRelativeTime(instant: Instant): String {
    val now = Clock.System.now()
    val duration = now - instant
    return when {
        duration.inWholeMinutes < 1 -> "just now"
        duration.inWholeHours < 1 -> "${duration.inWholeMinutes} minute${if (duration.inWholeMinutes != 1L) "s" else ""} ago"
        duration.inWholeDays < 1 -> "${duration.inWholeHours} hour${if (duration.inWholeHours != 1L) "s" else ""} ago"
        duration.inWholeDays < 7 -> "${duration.inWholeDays} day${if (duration.inWholeDays != 1L) "s" else ""} ago"
        else -> {
            val weeks = duration.inWholeDays / 7
            "$weeks week${if (weeks != 1L) "s" else ""} ago"
        }
    }
}

fun getRelativeTime(instantString:String): String = getRelativeTime(Instant.parse(instantString))

fun Instant.isToday():Boolean{
    return this.daysUntil(Clock.System.now(), TimeZone.UTC)==0
}
fun Instant.isYesterday():Boolean{
    return this.daysUntil(Clock.System.now(), TimeZone.UTC)==1
}


fun formatDateHeader(date: Instant): String {
    return when {
        date.isToday() -> "Today"
        date.isYesterday() -> "Yesterday"
        else -> {
           val localDate =  date.toLocalDateTime(TimeZone.UTC)
            "${localDate.month.name} ${localDate.dayOfMonth}"
        }

    }
}
@OptIn(FormatStringsInDatetimeFormats::class)
fun formatTimeHeader(date: Instant):String{
    val localDate =  date.toLocalDateTime(TimeZone.UTC)
    return localDate.format("hh:mm a")
}
