package de.rogallab.mobile.data.local.database

import androidx.room.RoomDatabase
import de.rogallab.mobile.data.local.IPersonDao
import de.rogallab.mobile.data.local.seed.Seed
import de.rogallab.mobile.domain.utilities.logDebug
import de.rogallab.mobile.domain.utilities.logError
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent

class SeedDatabase(
   private val _database: RoomDatabase,
   private val _personDao: IPersonDao,
   private val _seed: Seed,
   private val _dispatcher: CoroutineDispatcher,
) : KoinComponent {

   suspend fun seedPerson(): Boolean =
      withContext(_dispatcher) {
         try {
            _personDao.count().let { count ->
               if (count > 0) {
                  logDebug("<-SeedDatabase", "seed: Database already seeded")
                  return@withContext false
               }
            }
            _seed.createPerson(true)
            _database.clearAllTables()
            _personDao.insert(_seed.personDtos)
            return@withContext true
         } catch (e: Exception) {
            logError("<-SeedDatabase", "seed: ${e.message}")
         }
         return@withContext false
      }
}
