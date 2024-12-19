package de.rogallab.mobile.ui.features.people.composables

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import de.rogallab.mobile.ui.permissions.checkCameraPermission
import de.rogallab.mobile.ui.permissions.checkImagesOnMediaStorePermissions

@Composable
fun CameraCheckPermission(
   handleErrorEvent: (Throwable) -> Unit,
   onPermissionGranted: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val checkedPermissions = remember {
        checkCameraPermission(context, handleErrorEvent) &&
        checkImagesOnMediaStorePermissions(context, handleErrorEvent)
    }
    if (checkedPermissions) onPermissionGranted()
}