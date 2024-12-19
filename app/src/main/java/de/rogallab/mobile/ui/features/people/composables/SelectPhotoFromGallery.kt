package de.rogallab.mobile.ui.features.people.composables

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Face
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.rogallab.mobile.R
import de.rogallab.mobile.domain.utilities.logDebug
import kotlinx.coroutines.launch

@Composable
fun SelectPhotoFromGallery(
   writeToLocalStorage: (Bitmap) -> Unit  // Event ↑
) {

   var bitmap:Bitmap? = null
   val context = LocalContext.current

   val coroutineScope = rememberCoroutineScope()

   // callback for result from photo gallery
   val launcher = rememberLauncherForActivityResult(
      contract = ActivityResultContracts.GetContent()
   ) { uri: Uri? ->
      // get bitmap from content resolver (photo gallery)
      uri?.let { imageUri:Uri ->
         logDebug("[SelectPhotoFromGalery]","Get Bitmap ${imageUri.path}")
         if (Build.VERSION.SDK_INT < 28) {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(context.contentResolver, imageUri)?.let { bitmapFromGallery:Bitmap ->
               bitmap = bitmapFromGallery
            }
         } else {
            val source = ImageDecoder.createSource(context.contentResolver, imageUri)
            bitmap = ImageDecoder.decodeBitmap(source)
         }
         // save bitmap to internal storage of the app
         bitmap?.let { bitmap ->
            coroutineScope.launch {
               writeToLocalStorage(bitmap)
            }
         }
      }
   }

   Button(
      modifier = Modifier.padding(horizontal = 4.dp).fillMaxWidth(),
      onClick = {
         logDebug("[SelectPhotoFromGalery]", "Click")
         launcher.launch("image/*")
      }
   ) {
      Row(
         verticalAlignment = Alignment.CenterVertically
      ) {
         Icon(imageVector = Icons.Outlined.Face,
            contentDescription = stringResource(R.string.back))
         Text(
            modifier = Modifier.padding(start = 8.dp),
            text = stringResource(R.string.selectPhotoFromGallery),
            style = MaterialTheme.typography.bodyMedium
         )
      }
   }

}