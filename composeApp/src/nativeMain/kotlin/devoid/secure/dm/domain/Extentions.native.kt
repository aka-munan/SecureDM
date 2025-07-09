package devoid.secure.dm.domain

import kotlinx.datetime.LocalDateTime
import platform.Foundation.*


actual fun LocalDateTime.format(pattern: String): String {
    val formatter = NSDateFormatter()
    formatter.locale = NSLocale.currentLocale
    formatter.dateFormat = "MM/dd/yyyy - hh:mm a" // Example format
    return formatter.stringFromDate(toNsDate(this))
}
private fun toNsDate(localDateTime: LocalDateTime): NSDate {
    val calendar = NSCalendar.currentCalendar
    val components = NSDateComponents()
    components.year = localDateTime.year.toLong()
    components.month = localDateTime.monthNumber.toLong()
    components.day = localDateTime.dayOfMonth.toLong()
    components.hour = localDateTime.hour.toLong()
    components.minute = localDateTime.minute.toLong()
    components.second = localDateTime.second.toLong()
    return calendar.dateFromComponents(components)!!
}