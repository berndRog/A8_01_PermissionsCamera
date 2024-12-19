package de.rogallab.mobile.domain.utilities

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import java.util.Locale

// java.time.LocalDateTime used for formatting
val zonedId = TimeZone.currentSystemDefault().id

// -----------------------------------------------------------------------------
// LocalDateTime  Kotlin
// -----------------------------------------------------------------------------
fun LocalDateTime.toUtcString(): String =
   this.toInstant(TimeZone.UTC).toString()

fun LocalDateTime.toIsoString(): String =
   this.toInstant(TimeZone.of(zonedId)).toString()

fun LocalDateTime.toEpochMillis(): Long =
   this.toInstant(TimeZone.UTC).toEpochMilliseconds()

fun LocalDateTime.toZonedDateTimeString(
   zoneId: String                  // IANA time zone id
): LocalDateTime {
   val timeZone = TimeZone.of(zoneId)
   val instant = this.toInstant(timeZone)
   return instant.toLocalDateTime(timeZone)
}

// to Date
fun LocalDateTime.toDateString(
   locale: Locale = Locale.getDefault()
): String {
   val dts: DateTimeString = this.formatted()
   return when (locale.language) {
      "de" -> "${dts.day}.${dts.month}.${dts.year}"
      "en" -> "${dts.month}/${dts.day}/${dts.year}"
      else -> "${dts.day}.${dts.month}.${dts.year}"
   }
}

// to Time
fun LocalDateTime.toTimeString(): String {
   val dts: DateTimeString = this.formatted()
   return "${dts.hour}:${dts.min}:${dts.sec}"
}

// To DateTime
fun LocalDateTime.toDateTimeString(
   locale: Locale = Locale.getDefault(),
): String {
   val dts: DateTimeString = this.formatted()
   return when (locale.language) {
      "de" -> "${dts.day}.${dts.month}.${dts.year} ${dts.hour}:${dts.min}:${dts.sec}"
      "en" -> "${dts.month}/${dts.day}/${dts.year} ${dts.hour}:${dts.min}:${dts.sec}"
      else -> "${dts.day}.${dts.month}.${dts.year} ${dts.hour}:${dts.min}:${dts.sec}"
   }
}

private fun LocalDateTime.formatted(): DateTimeString =
   DateTimeString(
      year = this.date.year.toString(),
      month = this.date.monthNumber.toString().padStart(2, '0'),
      day = this.date.dayOfMonth.toString().padStart(2, '0'),
      dayOfWeek = this.date.dayOfWeek.name,
      hour = this.time.hour.toString().padStart(2, '0'),
      min = this.time.minute.toString().padStart(2, '0'),
      sec = this.time.second.toString().padStart(2, '0'),
      mil = (this.time.nanosecond / 1_000_000).toString().padStart(3, '0')
   )

// -----------------------------------------------------------------------------
// epochMillis
// -----------------------------------------------------------------------------
// to LocalDateTime
fun epochToLocalDateTime(epochMillis: Long): LocalDateTime =
   Instant
      .fromEpochMilliseconds(epochMillis)
      .toLocalDateTime(TimeZone.currentSystemDefault())

// -----------------------------------------------------------------------------
// ISO 8601 String to LocalDateTime (Kotlinx)
// -----------------------------------------------------------------------------
fun isoStringToLocalDateTime(isoString: String): LocalDateTime {
   val instant = Instant.parse(isoString)
   return instant.toLocalDateTime(TimeZone.currentSystemDefault())
}


// static extension function
fun LocalDateTime.Companion.now(): LocalDateTime {
   val instantNow: Instant = Clock.System.now()
   // Convert the instant to a LocalDateTime in the system's default time zone
   val nowDateTime: LocalDateTime =
      instantNow.toLocalDateTime(TimeZone.currentSystemDefault())
   return nowDateTime
}

private data class DateTimeString(
   val year: String,
   val month: String,
   val day: String,
   val dayOfWeek: String,
   val hour: String,
   val min: String,
   val sec: String,
   val mil: String
)
