package de.rogallab.mobile.domain

import android.graphics.Bitmap

interface IMediaStoreRepository {
   // return the uri of the saved image
   suspend fun saveImage(bitmap: Bitmap): ResultData<String?>
} //
