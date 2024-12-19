package de.rogallab.mobile.ui.permissions

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import de.rogallab.mobile.R
import de.rogallab.mobile.domain.utilities.logDebug
import de.rogallab.mobile.domain.utilities.logInfo
import de.rogallab.mobile.ui.openAppSettings
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun RequestPermissions(
   permissionsDeferred: CompletableDeferred<Boolean>,
   handleErrorEvent: (Throwable) -> Unit
) {
   val tag = "<-RequestPermissions"
   val context = LocalContext.current

   val permissions: Array<String> =
      remember { getPermissionsFromManifest(context) }
   val permissionStates: MutableState<Map<String, Boolean>> =
      remember { mutableStateOf(permissions.associateWith { false }) }
   val showRationaleStates = remember { mutableStateOf(permissions.associateWith { false }) }

   val coroutineScope = rememberCoroutineScope()

   // Local state for permission queue
   //val permissionQueue: SnapshotStateList<String> = remember { mutableStateListOf<String>() }
   val permissionsToRequest: MutableList<String> = remember { mutableListOf() }

   // Setup multiple permission request launcher (ActivityCompat.requestPermissions)
   val requestLauncher = rememberLauncherForActivityResult(
      // RequestMultiplePermissions() is a built-in ActivityResultContract
      contract = ActivityResultContracts.RequestMultiplePermissions(),
      // Callback for the result of the permission request
      // the result is a Map<String, Boolean> with key=permission value=isGranted
      onResult = { grantResults: Map<String, Boolean> ->
         grantResults.forEach { (permission, isGranted) ->
            logDebug(tag, "$permission = $isGranted")
            // gemini
            if (!isGranted) {
               if (ActivityCompat.shouldShowRequestPermissionRationale(
                     context as Activity,
                     permission
                  )
               ) {
                  showRationaleStates.value += (permission to true)
               } else {
                  // permission last time permanently denied(permission)
                  val permissionText = getPermissionText(context, permission)
                  val text = permissionText?.getDescription(true)

                  Toast.makeText(context, text, Toast.LENGTH_LONG).show()
//                handleErrorEvent(Exception(text))

                  coroutineScope.launch {
                     delay(2500)
                     context.openAppSettings()
                     context.finish()
                  }
               }
            }
         }
         if (grantResults.all { it.value }) {
            logInfo(tag, "All permissions already granted")
            permissionsDeferred.complete(true)
         }
      }
   )

   // launch permission requests that are not already granted
   LaunchedEffect(Unit) {
      // Filter permissions from manifest that are not granted yet
      filterPermissions(context, permissions) { permission ->
         permissionsToRequest.add(permission)
      }
      // launch permission requests that are not already granted
      if (permissionsToRequest.isNotEmpty()) {
         requestLauncher.launch(permissionsToRequest.toTypedArray())
      } else {
         logInfo(tag, "no more permissions to request")
         permissionsDeferred.complete(true)
      }
   }

   // Handle permission rationale and app settings
   permissions.forEach { permission ->
      logDebug(tag, "permission: $permission ratinale:${showRationaleStates.value[permission]}")

      if (showRationaleStates.value[permission] == true) {
         val showRationale =
            (context as Activity).shouldShowRequestPermissionRationale(permission)
         val permissionText = getPermissionText(context, permission)

         logDebug(tag, "AlertDialog showRationaleStates: $permission ${showRationaleStates.value[permission]}")
         AlertDialog(
            onDismissRequest = {
               showRationaleStates.value += (permission to false)
            },
            confirmButton = {
               Button(
                  onClick = {
                     showRationaleStates.value += (permission to false)
                     requestLauncher.launch(arrayOf(permission))
                  }
               ) {
                  Text(text = stringResource(R.string.agree))
               }
            },
            dismissButton = {
               Button(
                  onClick = {
                     showRationaleStates.value += (permission to false)
                     // Show rationale
                     if (!showRationale) {
                        requestLauncher.launch(arrayOf(permission))
                     }
                     // Permission is permanently denied, open app settings
                     else {
                        val text = permissionText?.getDescription(showRationale)
                        Toast.makeText(context, text, Toast.LENGTH_LONG).show()
//                        coroutineScope.launch {
//                           delay(2500)
//                           context.openAppSettings()
//                           context.finish()
//                        }
                     }
                  }
               ) {
                  Text(text = stringResource(R.string.refuse))
               }
            },
            icon = {},
            title = { Text(text = stringResource(R.string.permissionRequired)) },
            text = {
               Text(text = permissionText?.getDescription(!showRationale) ?: "")
            }
         )
      } // end if (showRationaleStates.value[permission] == true)
   } // end permissions.forEach

   /*
permissions.forEach { permission ->
   if (showRationaleStates.value[permission] == true) {

      val isPermanentlyDeclined =
         !(context as Activity).shouldShowRequestPermissionRationale(permission)
      val permissionText = getPermissionText(context, permission)
         ?.getDescription(isPermanentlyDeclined)
         ?: "no text availible"

      AlertDialog(
         onDismissRequest = {
            showRationaleStates.value += (permission to false)
            logDebug(tag, "onDismissRequest showRationaleStates: $permission ${showRationaleStates.value[permission]}")
         },
         title = { Text(stringResource(R.string.permissionRequired)) },
         //text = { Text("This app needs access to $permission to provide its functionality.") },
         text = { Text(permissionText) },
         confirmButton = {
            Button(onClick = {
               showRationaleStates.value += (permission to false)
               logDebug(tag, "confirm showRationaleStates: $permission ${showRationaleStates.value[permission]}")
               requestLauncher.launch(arrayOf(permission))
            }) {
               Text(text = stringResource(R.string.agree))
            }
         },
         dismissButton = {
            Button(onClick = {
               showRationaleStates.value += (permission to false)
               logDebug(tag, "dismiss showRationaleStates: $permission ${showRationaleStates.value[permission]}")

               if (isPermanentlyDeclined) {
                  context.openAppSettings()
               }
            }) {
               Text(text = stringResource(R.string.refuse))
//                  Text("Deny")
            }
         }
      )
   }
}
*/


}

private fun filterPermissions(
   context: Context,
   permissionsFromManifest: Array<String>,
   onPermissionToRequest: (String) -> Unit
) {
   val tag = "<-FilterPermissions"

   permissionsFromManifest.forEach { permission ->
      // is permission already granted?
      if (ContextCompat.checkSelfPermission(context, permission) ==
         PackageManager.PERMISSION_GRANTED
      ) {
         logDebug(tag, "already granted:       $permission")
         return@forEach
      }

      // no permission check needed
      if (permission == Manifest.permission.WRITE_EXTERNAL_STORAGE && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
      ) {
         logDebug(tag, "not needed permission: $permission SDK_INT: ${Build.VERSION.SDK_INT} >= TIRAMISU ${Build.VERSION_CODES.TIRAMISU}")
         return@forEach
      }
      if (permission == Manifest.permission.READ_EXTERNAL_STORAGE &&  Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
      ) {
         logDebug(tag, "not needed permission: $permission SDK_INT: ${Build.VERSION.SDK_INT} >= TIRAMISU ${Build.VERSION_CODES.TIRAMISU}")
         return@forEach
      }

      if (permission == Manifest.permission.FOREGROUND_SERVICE_LOCATION
      ) {
         logDebug(tag, "implicit granted:      $permission")
         return@forEach
      }

      logDebug(tag, "Permission to request: $permission")
      onPermissionToRequest(permission)
   }
}

private fun getPermissionsFromManifest(context: Context): Array<String> {
   val packageInfo = context.packageManager
      .getPackageInfo(context.packageName, PackageManager.GET_PERMISSIONS)
   return packageInfo.requestedPermissions ?: emptyArray()
}

private fun getPermissionText(context: Context, permission: String): IPermissionText? {
   return when (permission) {
      // Permissions that have to be granted by the user
      Manifest.permission.CAMERA -> PermissionCamera(context, permission)
      Manifest.permission.RECORD_AUDIO -> PermissionRecordAudio(context, permission)
      Manifest.permission.READ_EXTERNAL_STORAGE -> PermissionExternalStorage(context, permission)
      Manifest.permission.WRITE_EXTERNAL_STORAGE -> PermissionExternalStorage(context, permission)
      Manifest.permission.ACCESS_COARSE_LOCATION -> PermissionCoarseLocation(context, permission)
      Manifest.permission.ACCESS_FINE_LOCATION -> PermissionFineLocation(context, permission)
      else -> PermissionDefault(context, permission)
   }
}