package de.rogallab.mobile.ui.features.people

import android.content.Context
import de.rogallab.mobile.domain.ResultData
import de.rogallab.mobile.domain.entities.Person
import de.rogallab.mobile.domain.utilities.logDebug
import de.rogallab.mobile.domain.utilities.splitUrl
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async

fun whichImagePath(
   localImage: String?,
   remoteImage: String?
): String? {
//   logDebug("<-whichImage",
//      "local $localImage, remote $remoteImage")

   // localImage first
   var imagePath: String? = null
   // imagepath = localImage when localImage is not null
   if(!localImage.isNullOrEmpty()) imagePath = localImage
   // imagePath = remoteImage, when remote is not null and localImage is null
   if(imagePath.isNullOrEmpty() && !remoteImage.isNullOrEmpty()) imagePath = remoteImage

//   logDebug("<-whichImage",
//      "returned imagePath $imagePath")

   return imagePath
}


// post local image to remote webserver
// then delete local image
suspend fun handleLocalImage(
   person: Person,
   deleteLocalImage: suspend (String) -> ResultData<Boolean>,
   deleteRemoteImage : suspend (String) -> ResultData<Boolean>,
   postImage: suspend (String) -> ResultData<String>,
   handleErrorEvent: (Throwable) -> Unit,

   scope: CoroutineScope,
   exceptionHandler: CoroutineExceptionHandler,
): Pair<String?, String?> {

   val tag = "<-handleLocalImage"

   var localImage = person.localImage
   var remoteImage = person.remoteImage

   // is there a new local image?
   if (!localImage.isNullOrEmpty()) {
      // delete old remote image
      if (!remoteImage.isNullOrEmpty()) {
         when (val resultData = scope.async(exceptionHandler) {
            val (filename,ext) = splitUrl(remoteImage!!)
            deleteRemoteImage(filename)
         }.await()
         ) {
            is ResultData.Success -> logDebug(tag, "deleted remote image")
            is ResultData.Error -> handleErrorEvent(resultData.throwable)
            else -> Unit
         }
      }

      // post the new local image
      when (val resultData = scope.async(exceptionHandler) {
         postImage(localImage!!)
      }.await()
      ) {
         is ResultData.Success -> {
            logDebug(tag, "posted new remote image")
            // then delete the local image
            when(val r = deleteLocalImage(localImage)) {
               is ResultData.Error -> handleErrorEvent(r.throwable)
               else -> Unit
            }
            // set localImage to null
            localImage = null
            // save remoteImage path
            remoteImage = resultData.data
//          return Pair(localImage, remoteImage)
         }
         is ResultData.Error -> handleErrorEvent(resultData.throwable)
         else -> Unit
      }
   } // end post local image
   return Pair(localImage, remoteImage)
} // end function

// remove the image from remote webserver
// for undo: download remote image and save it as local image
suspend fun removeRemoteImage(
   person: Person,
   getImage: suspend (String) -> ResultData<String?>,
   deleteLocalImage: suspend (String) -> ResultData<Boolean>,
   deleteRemoteImage: suspend (String) -> ResultData<Boolean>,
   handleErrorEvent: (Throwable) -> Unit,
   scope: CoroutineScope,
   exceptionHandler: CoroutineExceptionHandler,
): String? {
   val tag = "<-handleLocalImage"

   // is there a new local image?
   if (!person.localImage.isNullOrEmpty()) {
      // then delete the local image
      when (val r = deleteLocalImage(person.localImage)) {
         is ResultData.Error -> handleErrorEvent(r.throwable)
         else -> Unit
      }
   }

   var remoteImage = person.remoteImage
   var remoteAsLocalImage: String? = null

   // is there a remote image
   if (!remoteImage.isNullOrEmpty()) {
      // download remote image and save it as local image
      val (filename, ext) = splitUrl(remoteImage)
      when (val resultData = scope.async(exceptionHandler) {
         // save remote image path on local storage
         getImage(filename)
      }.await()
      ) {
         is ResultData.Success -> {
            resultData.data?.let { imagePath ->
               remoteAsLocalImage = imagePath
               logDebug(tag, "remote image downloaded and saved as local image $imagePath")
            }
         }
         is ResultData.Error -> handleErrorEvent(resultData.throwable)
         else -> Unit
      }

      // delete remote image
      when (val resultData = scope.async(exceptionHandler) {
         val (filename, ext) = splitUrl(remoteImage)
         deleteRemoteImage(filename)
      }.await()
      ) {
         is ResultData.Success -> logDebug(tag, "remote image deleted")
         is ResultData.Error -> handleErrorEvent(resultData.throwable)
         else -> Unit
      }
   } // end delete remote image
   logDebug(tag, "return remoteAsLocalImage: $remoteAsLocalImage")
   return remoteAsLocalImage
} // end function