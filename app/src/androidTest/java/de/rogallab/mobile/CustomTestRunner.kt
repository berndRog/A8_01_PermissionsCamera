package de.rogallab.mobile

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner

class CustomTestRunner : AndroidJUnitRunner() {
   override fun newApplication(
      cl: ClassLoader?,
      className: String?,
      context: Context?
   ): Application {
      // Return an instance of your test application
      return super.newApplication(cl, TestStart::class.java.name, context)
   }
}