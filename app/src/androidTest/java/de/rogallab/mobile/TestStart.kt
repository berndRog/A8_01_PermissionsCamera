package de.rogallab.mobile

import android.app.Application
import de.rogallab.mobile.domain.utilities.logDebug

// to run tests without KOIN initialization
class TestStart: Application() {

   override fun onCreate() {
      super.onCreate()
      // Do not initialize Koin here
      logDebug("<-TestStart", "onCreate()")
   }
}