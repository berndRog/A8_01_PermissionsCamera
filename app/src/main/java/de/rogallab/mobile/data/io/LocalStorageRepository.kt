package de.rogallab.mobile.data.io

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.net.toFile
import de.rogallab.mobile.domain.ILocalStorageRepository
import de.rogallab.mobile.domain.ResultData
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.UUID

class LocalStorageRepository(
   private val _context: Context,
   private val _dispatcher: CoroutineDispatcher,
   private val _exceptionHandler: CoroutineExceptionHandler,
): ILocalStorageRepository {

   override suspend fun readImage(
      uri: Uri
   ): ResultData<Bitmap?> =
      withContext(_dispatcher + _exceptionHandler) {
         return@withContext try {
            BitmapFactory.decodeFile(uri.toFile().absolutePath)?.let { bitmap ->
               ResultData.Success(bitmap)
            } ?: ResultData.Error(IOException("BitmapFactory.decodeFile() returned null"))
         } catch (e: Exception) {
            ResultData.Error(e)
         }
      }

   override suspend fun writeImage(
      bitmap: Bitmap
   ): ResultData<String?> =
      withContext(_dispatcher + _exceptionHandler) {
         return@withContext try {
            val file = File(_context.filesDir, "${UUID.randomUUID()}.jpg")
            // compress bitmap to file and return absolute path
            file.outputStream().use { out ->
               bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
               file.absolutePath // return absolute path
            }
            ResultData.Success(file.absolutePath)
         } catch (t: Throwable) {
            ResultData.Error(t)
         }
      }


   override suspend fun writeInputStream(
      inputStream: InputStream,
      fileName: String
   ): ResultData<String?> =
      withContext(_dispatcher + _exceptionHandler) {
         return@withContext try {
            val file = File(_context.filesDir, fileName)
            inputStream.use { input ->
               file.outputStream().use { output ->
                  input.copyTo(output)
               }
            }
            ResultData.Success(file.absolutePath)
         } catch (t: Throwable) {
            ResultData.Error(t)
         }
      }

   override suspend fun writeDownloadedFile(
      fileContent: ByteArray,
      fileName: String
   ): ResultData<String?> =
      withContext(_dispatcher + _exceptionHandler) {
         return@withContext try {
            val file = File(_context.filesDir, fileName)
            FileOutputStream(file).use { output ->
               output.write(fileContent)
            }
            ResultData.Success(file.absolutePath)
         } catch (t: Throwable) {
            ResultData.Error(t)
         }
      }

   override suspend fun deleteFile(
      fileName: String
   ): ResultData<Boolean> =
      withContext(_dispatcher + _exceptionHandler) {
         return@withContext try {
            File(fileName).apply {
               this.absoluteFile.delete()
            }
            ResultData.Success(true)
         } catch (t: Throwable) {
            ResultData.Error(t)
         }
      }

   override suspend fun copyFileOnStorage(
      filePath: String
   ): ResultData<String?> =
      withContext(_dispatcher + _exceptionHandler) {
         return@withContext try {
            val originalFile = File(filePath)
            val ext = filePath.substringAfterLast('.', "")
            val newFileName = "${UUID.randomUUID()}.$ext"
            val newFile = File(originalFile.parent, newFileName)
            FileInputStream(originalFile).use { input ->
               FileOutputStream(newFile).use { output ->
                  input.copyTo(output)
               }
            }
            ResultData.Success(newFile.absolutePath)
         } catch (t: Throwable) {
            ResultData.Error(t)
         }
      }
}