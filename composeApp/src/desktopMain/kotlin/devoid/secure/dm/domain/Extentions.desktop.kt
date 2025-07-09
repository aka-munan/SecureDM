package devoid.secure.dm.domain

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toJavaLocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

actual fun LocalDateTime.format(pattern:String):String{
    val formater = DateTimeFormatter.ofPattern(pattern, Locale.getDefault())
    return formater.format(this.toJavaLocalDateTime())
}