package de.rogallab.mobile.domain

import android.graphics.Bitmap
import android.net.Uri
import java.io.InputStream

interface ILocalStorageRepository {
   suspend fun readImage(uri: Uri): ResultData<Bitmap?>
   suspend fun writeImage(bitmap: Bitmap): ResultData<String?>
   suspend fun writeInputStream(inputStream: InputStream, fileName: String): ResultData<String?>
   suspend fun writeDownloadedFile(fileContent: ByteArray, fileName: String): ResultData<String?>
   suspend fun deleteFile(fileName: String): ResultData<Boolean>
   suspend fun copyFileOnStorage(filePath: String): ResultData<String?>
}
