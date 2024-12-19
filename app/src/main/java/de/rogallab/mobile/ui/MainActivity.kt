package de.rogallab.mobile.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.rogallab.mobile.domain.utilities.logError
import de.rogallab.mobile.domain.utilities.logInfo
import de.rogallab.mobile.domain.utilities.logVerbose
import de.rogallab.mobile.ui.base.BaseActivity
import de.rogallab.mobile.ui.features.home.HomeViewModel
import de.rogallab.mobile.ui.navigation.composables.AppNavHost
import de.rogallab.mobile.ui.permissions.RequestForegroundLocationPermission
import de.rogallab.mobile.ui.permissions.RequestMultiplePermissions
import de.rogallab.mobile.ui.permissions.RequestPermissions
import de.rogallab.mobile.ui.theme.AppTheme
import kotlinx.coroutines.CompletableDeferred
import org.koin.android.ext.android.inject
import org.koin.compose.KoinContext

class MainActivity : BaseActivity(TAG) {


   private val _homeViewModel: HomeViewModel by inject()

   override fun onCreate(savedInstanceState: Bundle?) {
      super.onCreate(savedInstanceState)
      setContent {
         KoinContext{
            AppTheme {
               AppNavHost()
            }
         }
      }
   }

   companion object {
      private const val TAG = "<-MainActivity"
   }
}

// static extension function for Activity
fun Context.openAppSettings() {
   Intent(
      Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
      Uri.fromParts("package", packageName, null)
   ).also(::startActivity)
}
