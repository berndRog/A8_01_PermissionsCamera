package de.rogallab.mobile.ui.permissions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

fun checkCameraPermission(
   context: Context,
   handleErrorEvent: (Throwable) -> Unit
): Boolean {
   // Check if the camera permission is already granted
   if(ContextCompat.checkSelfPermission(
         context,
         Manifest.permission.CAMERA
      ) == PackageManager.PERMISSION_GRANTED) {
      return true
   } else {
      handleErrorEvent(
         Throwable("CAMERA permission is not granted")
      )
      return false
   }
}

fun checkImagesOnMediaStorePermissions(
   context: Context,
   handleErrorEvent: (Throwable) -> Unit
): Boolean {

   when {
      Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
         // Android 13+:
         // If you want to read images, use READ_MEDIA_IMAGES
         if (ContextCompat.checkSelfPermission(
               context,
               Manifest.permission.READ_MEDIA_IMAGES
            ) != PackageManager.PERMISSION_GRANTED
         ) {
            handleErrorEvent(
               Throwable("READ_MEDIA_IMAGES permission is not granted")
            )
            return false
         }
         // Generally, WRITE_EXTERNAL_STORAGE is not needed on Android 10+ for MediaStore inserts.
      }

      Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
         // Android 10 to 12:
         // Scoped storage in effect. Usually you only need READ_EXTERNAL_STORAGE
         // if reading images not created by your app.
         if (ContextCompat.checkSelfPermission(
               context,
               Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
         ) {
            handleErrorEvent(
               Throwable("READ_EXTERNAL_STORAGE permission is not granted")
            )
            return false
         }
      }

      else -> { return false }
   }
   return true
}