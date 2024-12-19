package de.rogallab.mobile.ui.permissions

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import de.rogallab.mobile.domain.utilities.logDebug
import de.rogallab.mobile.domain.utilities.logError
import de.rogallab.mobile.domain.utilities.logVerbose
import de.rogallab.mobile.ui.openAppSettings

@Composable
fun RequestMultiplePermissions(
   onAllPermissionsGranted: () -> Unit,
   onPermissionDenied: (String) -> Unit,
   onPermissionPermanentlyDenied: (String) -> Unit
) {
   val tag = "<-RequestPermissions"

   val context = LocalContext.current
   val permissions = remember { getPermissionsFromManifest(context, 1) }
   val permissionStates = remember { mutableStateOf(permissions.associateWith { false }) }
   val showRationaleStates = remember { mutableStateOf(permissions.associateWith { false }) }

   // Setup multiple permission request launcher (ActivityCompat.requestPermissions)
   val launcher = rememberLauncherForActivityResult(
      contract = ActivityResultContracts.RequestMultiplePermissions(),
      // Callback for the result of the permission request
      // the result is a Map with key=permission and value=isGranted
      onResult = { grantResults: Map<String, Boolean> ->
         grantResults.forEach { (permission, isGranted) ->
            permissionStates.value = permissionStates.value + (permission to isGranted)
            logDebug(tag, "$permission = $isGranted")
            if (!isGranted) {
               if (ActivityCompat.shouldShowRequestPermissionRationale(
                     context as Activity,
                     permission
                  )
               ) {
                  logDebug(tag, "showRationaleStates: $permission")
                  showRationaleStates.value = showRationaleStates.value + (permission to true)
               } else {
                  logDebug(tag, "Permission permanently denied: $permission")
                  onPermissionPermanentlyDenied(permission)
               }
            }
         }
         if (permissionStates.value.all { it.value }) {
            onAllPermissionsGranted()
         }
      }
   )

   LaunchedEffect(Unit) {
      permissions.filterNot { permission ->
         ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED
      }.forEach{ permission ->
         logDebug(tag, "Permission already granted: $permission") }

      // Check if permissions are already granted
      val permissionsToRequest = permissions.filter { permission ->
         ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED
      }
      permissionsToRequest.forEach{ permission ->
         logDebug(tag, "Permission to request: $permission") }

      if (permissionsToRequest.isNotEmpty()) {
         // Request permissions
         launcher.launch(permissionsToRequest.toTypedArray())
      } else {
         onAllPermissionsGranted()
      }
   }

   permissions.forEach { permission ->
      if (showRationaleStates.value[permission] == true) {
         AlertDialog(
            onDismissRequest = {
               showRationaleStates.value = showRationaleStates.value + (permission to false)
               onPermissionDenied(permission)
            },
            title = { Text("Permission Required") },
            text = { Text("This app needs access to $permission to provide its functionality.") },
            confirmButton = {
               Button(onClick = {
                  showRationaleStates.value = showRationaleStates.value + (permission to false)
                  launcher.launch(arrayOf(permission))
               }) {
                  Text("Grant Permission")
               }
            },
            dismissButton = {
               Button(onClick = {
                  showRationaleStates.value = showRationaleStates.value + (permission to false)
                  onPermissionDenied(permission)
               }) {
                  Text("Deny")
               }
            }
         )
      }
   }
}

@Composable
fun RequestForegroundLocationPermission(
   onPermissionGranted: () -> Unit,
//   onPermissionDenied: () -> Unit,
   onPermissionPermanentlyDenied: () -> Unit
) {
   val tag ="<-RequestForeground"

   val context = LocalContext.current
   val permissions = remember { getPermissionsFromManifest(context, 2) }
   var showRationale by remember { mutableStateOf(false) }
   var permissionRequested by remember { mutableStateOf(false) } // Add this line

    val launcher = rememberLauncherForActivityResult(
      contract = ActivityResultContracts.RequestPermission()
   ) { isGranted: Boolean ->
      if (isGranted) {
         onPermissionGranted()
      } else {
         // Permission denied, handle accordingly (e.g., show a message or disable functionality)
         if (!permissionRequested) { // Check if permission was already requested
            showRationale = true
         } else {
            logError(tag,"Permission denied after rationale, handle permanent denial")
            onPermissionPermanentlyDenied()
         }
      }
   }

   LaunchedEffect(Unit) {
      // Check if permission is already granted
      if (ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.FOREGROUND_SERVICE_LOCATION
         ) == PackageManager.PERMISSION_GRANTED
      ) {
         onPermissionGranted()
      } else {
         // Permission not granted, check if rationale should be shown
         if (ActivityCompat.shouldShowRequestPermissionRationale(
               context as Activity,
               Manifest.permission.FOREGROUND_SERVICE_LOCATION
            )
         ) {
            showRationale = true
         } else {
            // Request the permission
            launcher.launch(Manifest.permission.FOREGROUND_SERVICE_LOCATION)
            permissionRequested = true // Set permissionRequested to true
         }
      }
   }

   if (showRationale) {
      AlertDialog(
         onDismissRequest = { showRationale = false },
         title = { Text("Location Permission Required") },
         text = { Text("This app needs access to your location in the foreground to provide location-based services.") },
         confirmButton = {
            Button(onClick = {
               showRationale = false
               launcher.launch(Manifest.permission.FOREGROUND_SERVICE_LOCATION)
               permissionRequested = true // Set permissionRequested to true
            }) {
               Text("Grant Permission")
            }
         },
         dismissButton = {
            Button(onClick = {
               showRationale = false
            }) {
               Text("Deny")
            }
         }
      )
   }
}

private fun getPermissionsFromManifest(context: Context, filterType: Int = 0): List<String> {
   val packageInfo = context.packageManager.getPackageInfo(
      context.packageName,
      PackageManager.GET_PERMISSIONS
   )
   val permissions = packageInfo.requestedPermissions?.toList()?: emptyList()
//   permissions.forEach { permission ->
//      logVerbose("<-RequestPermissions","Permission form manifest: $permission")
//   }

   var filteredPermissions = emptyList<String>()

   if( filterType == 0) {
      return permissions
   }
   // cancel FOREGROUND_SERVICE_LOCATION permission
   else if(filterType == 1) {
      filteredPermissions = permissions.filterNot { it ->
         it == "android.permission.FOREGROUND_SERVICE_LOCATION" }
   }
   else if(filterType == 2) {
      filteredPermissions = permissions.filter { it ->
         it == "android.permission.FOREGROUND_SERVICE_LOCATION" }
   }
   else {
      filteredPermissions = emptyList()
   }

   filteredPermissions.forEach { permission ->
      logVerbose("<-RequestPermissions","Filtered permissions: $permission")
   }
   return filteredPermissions
}