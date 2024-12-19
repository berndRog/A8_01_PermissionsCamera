package de.rogallab.mobile.data.local.seed

import android.content.res.Resources
import android.graphics.BitmapFactory
import de.rogallab.mobile.R
import de.rogallab.mobile.data.dtos.PersonDto
import de.rogallab.mobile.domain.ILocalStorageRepository
import de.rogallab.mobile.domain.ResultData
import de.rogallab.mobile.domain.utilities.createUuid
import de.rogallab.mobile.domain.utilities.logDebug
import de.rogallab.mobile.domain.utilities.logVerbose
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

class Seed(
   private val _resources: Resources,
   private val _localStorageRepository: ILocalStorageRepository,
   private val _coroutineScope: CoroutineScope,
   private val _dispatcher: CoroutineDispatcher
) {
   private val _imagesPath = mutableListOf<String>()

   var personDtos: MutableList<PersonDto> = mutableListOf()

   //--- P E O P L E -----------------------------------------------------------------------
   suspend fun createPerson(withImages: Boolean): Seed {

      val firstNames = mutableListOf(
         "Arne", "Berta", "Cord", "Dagmar", "Ernst", "Frieda", "Günter", "Hanna",
         "Ingo", "Johanna", "Klaus", "Luise", "Martin", "Nadja", "Otto", "Patrizia",
         "Quirin", "Rebecca", "Stefan", "Tanja", "Uwe", "Veronika", "Walter", "Xaver",
         "Yvonne", "Zwantje")
      val lastNames = mutableListOf(
         "Arndt", "Bauer", "Conrad", "Diehl", "Engel", "Fischer", "Graf", "Hoffmann",
         "Imhoff", "Jung", "Klein", "Lang", "Meier", "Neumann", "Olbrich", "Peters",
         "Quart", "Richter", "Schmidt", "Thormann", "Ulrich", "Vogel", "Wagner", "Xander",
         "Yakov", "Zander")
      val emailProvider = mutableListOf("gmail.com", "icloud.com", "outlook.com", "yahoo.com",
         "t-online.de", "gmx.de", "freenet.de", "mailbox.org", "yahoo.com", "web.de")
      val random = Random(0)
      for (index in firstNames.indices) {
         val firstName = firstNames[index]
         val lastName = lastNames[index]
         val email =
            "${firstName.lowercase()}." +
               "${lastName.lowercase()}@" +
               "${emailProvider.random()}"
         val phone =
            "0${random.nextInt(1234, 9999)} " +
               "${random.nextInt(100, 999)}-" +
               "${random.nextInt(10, 9999)}"
         val personDto = PersonDto(
            firstName = firstName,
            lastName = lastName,
            email = email,
            phone = phone,
            localImage = null,
            remoteImage = null,
            id = createUuid(index + 1, 1)
         )
         logVerbose("<-Seed", "personDto: $personDto")
         personDtos.add(personDto)
      }

      //--- I M A G E S -----------------------------------------------------------------------
      if (!withImages) return this
      else {
         convertAndSaveDrawables()
         return this
      }
   } // createPerson

   private suspend fun convertAndSaveDrawables() {
      val drawables = listOf(
         R.drawable.man_1, R.drawable.man_2, R.drawable.man_3,
         R.drawable.man_4, R.drawable.man_5, R.drawable.man_6,
         R.drawable.woman_1, R.drawable.woman_2, R.drawable.woman_3,
         R.drawable.woman_4, R.drawable.woman_5
      )

      drawables.forEach { drawableId ->
         val bitmap = BitmapFactory.decodeResource(_resources, drawableId)
         bitmap?.let { bmp ->
            val resultData = withContext(_dispatcher) {
               _localStorageRepository.writeImage(bmp)
            }
            when (resultData) {
               is ResultData.Success -> {
                  resultData.data?.let { uriPath ->
                     uriPath?.let { _imagesPath.add(it) }
                  }
               }
               is ResultData.Error -> {
                  logDebug("<DrawableConverter>", "Error: ${resultData.throwable}")
               }
               else -> {}
            }
         }
      }

      if (_imagesPath.size == 11) {
         personDtos[0] = personDtos[0].copy(localImage = _imagesPath[0])
         personDtos[1] = personDtos[1].copy(localImage = _imagesPath[6])
         personDtos[2] = personDtos[2].copy(localImage = _imagesPath[1])
         personDtos[3] = personDtos[3].copy(localImage = _imagesPath[7])
         personDtos[4] = personDtos[4].copy(localImage = _imagesPath[2])
         personDtos[5] = personDtos[5].copy(localImage = _imagesPath[8])
         personDtos[6] = personDtos[6].copy(localImage = _imagesPath[3])
         personDtos[7] = personDtos[7].copy(localImage = _imagesPath[9])
         personDtos[8] = personDtos[8].copy(localImage = _imagesPath[4])
         personDtos[9] = personDtos[9].copy(localImage = _imagesPath[10])
         personDtos[10] = personDtos[10].copy(localImage = _imagesPath[5])
      }

   }

   fun disposeImages() {
      _imagesPath.forEach { imageUrl ->
         logDebug("<disposeImages>", "Url $imageUrl")
         _coroutineScope.launch {
            _localStorageRepository.deleteFile(imageUrl)
         }
      }
   }
}