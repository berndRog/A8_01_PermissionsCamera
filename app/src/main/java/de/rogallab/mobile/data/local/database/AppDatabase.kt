package de.rogallab.mobile.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import de.rogallab.mobile.AppStart
import de.rogallab.mobile.data.local.IPersonDao
import de.rogallab.mobile.data.dtos.PersonDto

@Database(
   entities = [
      PersonDto::class
   ],
   version = AppStart.DATABASE_VERSION,
   exportSchema = false
)
@TypeConverters(Converters.UuidConverter::class, Converters.LocalDateTimeUTCConverter::class)
abstract class AppDatabase : RoomDatabase() {
   abstract fun createPersonDao(): IPersonDao
}