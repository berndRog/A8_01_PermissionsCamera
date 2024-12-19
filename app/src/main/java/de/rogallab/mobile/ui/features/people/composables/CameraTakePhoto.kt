package de.rogallab.mobile.ui.features.people.composables

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddAPhoto
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.rogallab.mobile.R
import kotlinx.coroutines.launch

@Composable
fun CameraTakePhoto(
   writeToLocalStorage: (Bitmap) -> Unit, // Event ↑
   writeToMediaStore: (Bitmap) -> Unit,   // Event ↑
) {
   val context = LocalContext.current
   // callback camera
   val bitmapState = remember { mutableStateOf<Bitmap?>(value = null) }

   val coroutineScope = rememberCoroutineScope()

   val cameraLauncher = rememberLauncherForActivityResult(
      ActivityResultContracts.TakePicturePreview()
   ) { it: Bitmap? ->
      bitmapState.value = it
      // save bitmap to internal storage of the app
      bitmapState.value?.let { bitmap ->
         coroutineScope.launch {
            writeToLocalStorage(bitmap)
         }
      } // bitmapState.value?.let
   } // cameraLauncher

   bitmapState.value?.let { bitmap ->
      LaunchedEffect(bitmap) {
         writeToMediaStore(bitmap)
      }
   }

   Button(
      modifier = Modifier
         .padding(horizontal = 4.dp)
         .fillMaxWidth(),
      onClick = {
         cameraLauncher.launch()
      }
   ) {
      Row(
         verticalAlignment = Alignment.CenterVertically
      ) {
         Icon(imageVector = Icons.Outlined.AddAPhoto,
            contentDescription = stringResource(R.string.back))
         Text(
            modifier = Modifier.padding(start = 8.dp),
            text = stringResource(R.string.takePhotoWithCamera),
            style = MaterialTheme.typography.bodyMedium
         )
      }
   }
}