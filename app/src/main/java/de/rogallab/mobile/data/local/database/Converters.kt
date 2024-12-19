package de.rogallab.mobile.data.local.database

import androidx.room.TypeConverter
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class Converters {


   @OptIn(ExperimentalUuidApi::class)
   class UuidConverter {
      @TypeConverter
      fun fromUUID(uuid: Uuid?): String? {
         return uuid?.toString() ?: null
      }
      @TypeConverter
      fun uuidFromString(uuidString: String?): Uuid? {
         return uuidString?.let { Uuid.parse(it) }
      }
   }

   class LocalDateTimeUTCConverter {
      private val utcTimeZone = TimeZone.UTC
      @TypeConverter
      fun fromLocalDateTime(dateTime: LocalDateTime?): String? {
         return dateTime
            ?.toInstant(utcTimeZone)
            ?.toString() // Convert Instant to ISO-8601 String
      }
      @TypeConverter
      fun toLocalDateTime(isoString: String?): LocalDateTime? {
         return isoString
            ?.let { Instant.parse(it) } // Parse String to Instant
            ?.toLocalDateTime(utcTimeZone) // Convert Instant to LocalDateTime in UTC
      }
   }


//   epoch
//class LocalDateTimeUTCConverter {
//
//   private val utcTimeZone = TimeZone.UTC
//
//   @TypeConverter
//   fun fromLocalDateTime(dateTime: LocalDateTime?): Long? {
//      return dateTime?.toInstant(utcTimeZone)?.toEpochMilliseconds()
//   }
//
//   @TypeConverter
//   fun toLocalDateTime(epochMillis: Long?): LocalDateTime? {
//      return epochMillis?.let {
//         Instant.fromEpochMilliseconds(it).toLocalDateTime(utcTimeZone)
//      }
//   }
//}

//   class LocalDateTimeConverter {
//
//      @TypeConverter
//      fun fromLocalDateTime(dateTime: LocalDateTime?): String? {
//         return dateTime?.toString() // ISO-8601 format
//      }
//
//      @TypeConverter
//      fun toLocalDateTime(dateTimeString: String?): LocalDateTime? {
//         return dateTimeString?.let { LocalDateTime.parse(it) }
//      }
//   }
}