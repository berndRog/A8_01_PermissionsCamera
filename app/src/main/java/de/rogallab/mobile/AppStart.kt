package de.rogallab.mobile

import android.app.Application
import android.net.Uri
import de.rogallab.mobile.data.local.database.SeedDatabase
import de.rogallab.mobile.data.remote.SeedWebservice
import de.rogallab.mobile.domain.ILocalStorageRepository
import de.rogallab.mobile.domain.utilities.logInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class AppStart : Application() {

   // Define a CoroutineScope with a SupervisorJob for long-running application-wide tasks
   private val _applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

   override fun onCreate() {
      super.onCreate()
      logInfo(TAG, "onCreate()")

      logInfo(TAG, "onCreate(): startKoin{...}")
      startKoin {
         // Log Koin into Android logger
         androidLogger(Level.DEBUG)
         // Reference Android context
         androidContext(this@AppStart)
         // Load modules
         modules(domainModules, dataModules, uiModules)
      }

      val seedDatabase: SeedDatabase by inject()
      val seedWebservice: SeedWebservice by inject()
      _applicationScope.launch {
         seedDatabase.seedPerson()
         seedWebservice.seedPerson()
      }
   }

   companion object {
      const val IS_INFO = true
      const val IS_DEBUG = true
      const val IS_VERBOSE = true
      const val DATABASE_NAME = "db_8_01_PermissionsCamera.db"
      const val DATABASE_VERSION = 1

//    const val BASE_URL: String = "http://10.0.2.2:5010/"        // localhost für AVD
      const val BASE_URL: String = "http://192.168.178.23:6100/"  // physical mobile device
      const val API_KEY:  String = ""
      const val BEARER_TOKEN:  String = ""

      private const val TAG = "<-AppStart"
   }
}