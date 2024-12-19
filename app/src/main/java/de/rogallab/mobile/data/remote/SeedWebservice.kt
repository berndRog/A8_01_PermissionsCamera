package de.rogallab.mobile.data.remote

import de.rogallab.mobile.data.local.seed.Seed
import de.rogallab.mobile.data.mapping.toPerson
import de.rogallab.mobile.domain.ILocalStorageRepository
import de.rogallab.mobile.domain.IPersonRepository
import de.rogallab.mobile.domain.ImageRepository
import de.rogallab.mobile.domain.ResultData
import de.rogallab.mobile.domain.entities.Person
import de.rogallab.mobile.domain.utilities.logDebug
import de.rogallab.mobile.domain.utilities.logError
import de.rogallab.mobile.domain.utilities.logVerbose
import de.rogallab.mobile.domain.utilities.splitUrl
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent

class SeedWebservice(
   private val _personRepository: IPersonRepository,
   private val _imageRepository: ImageRepository,
   private val _localStorageRepository: ILocalStorageRepository,
   private val _webservice: IPersonWebservice,
   private val _seed: Seed,
   private val _dispatcher: CoroutineDispatcher,
   private val _exceptionHandler: CoroutineExceptionHandler
) : KoinComponent {

   suspend fun seedPerson(): Boolean =
      withContext(_dispatcher) {
         try {

            var count = 0
            _webservice.getAll().let { response ->
               if (response.isSuccessful) {
                  response.body()?.let { peopleDto ->
                     count = peopleDto.size
                  }
               }
            }
            if (count > 0) {
               logDebug("<-SeedDatabase", "seed: Database already seeded")
               return@withContext false
            }
            _seed.createPerson(true)

            val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
            _seed.personDtos.forEach { personDto ->
               create(personDto.toPerson(), scope)
               logDebug("<-SeedDatabase", "seed: $personDto")
            }

            return@withContext true
         } catch (e: Exception) {
            logError("<-SeedDatabase", "seed: ${e.message}")
         }
         return@withContext false
      }

   private suspend fun create(
      person: Person,
      scope: CoroutineScope
   ) {
      // handle new localImage
      seed(
         person = person,
         deleteLocalImage = _localStorageRepository::deleteFile,
         deleteRemoteImage = _imageRepository::delete,
         postImage =  _imageRepository::post,
         postPerson = _personRepository::post,
         handleErrorEvent = { logError("<-SeedWebservice", "Error: ${it.message}") },
         scope = scope,
         exceptionHandler = _exceptionHandler
      )
   }

   // post local image to remote webserver
// then delete local image
   suspend fun seed(
      person: Person,
      deleteLocalImage: suspend (String) -> ResultData<Boolean>,
      deleteRemoteImage : suspend (String) -> ResultData<Boolean>,
      postImage: suspend (String) -> ResultData<String>,
      postPerson: suspend (Person) -> Unit,
      handleErrorEvent: (Throwable) -> Unit,
      scope: CoroutineScope,
      exceptionHandler: CoroutineExceptionHandler,
   ) {

      val tag = "<-seedPersonWithImage"

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

      val p = person.copy(localImage = localImage, remoteImage = remoteImage)
      logVerbose("<-seedPersonWithImage", "person: $p")
      postPerson( p )
      delay(300)

   } // end function
}
