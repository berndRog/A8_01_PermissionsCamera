package de.rogallab.mobile.ui.features.people

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.ImageLoader
import de.rogallab.mobile.domain.ILocalStorageRepository
import de.rogallab.mobile.domain.IMediaStoreRepository
import de.rogallab.mobile.domain.IPersonRepository
import de.rogallab.mobile.domain.ImageRepository
import de.rogallab.mobile.domain.ResultData
import de.rogallab.mobile.domain.entities.Person
import de.rogallab.mobile.domain.utilities.logDebug
import de.rogallab.mobile.domain.utilities.logError
import de.rogallab.mobile.domain.utilities.logInfo
import de.rogallab.mobile.domain.utilities.newUuid
import de.rogallab.mobile.ui.IErrorHandler
import de.rogallab.mobile.ui.INavigationHandler
import de.rogallab.mobile.ui.errors.ErrorParams
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class PersonViewModel(
   private val _personRepository: IPersonRepository,
   private val _imageRepository: ImageRepository,
   private val _localStorageRepository: ILocalStorageRepository,
   private val _mediaStoreRepository: IMediaStoreRepository,
   private val _validator: PersonValidator,
   private val _errorHandler: IErrorHandler,
   private val _navigationHandler: INavigationHandler,
   private val _imageLoader: ImageLoader,
   private val _exceptionHandler: CoroutineExceptionHandler
) : ViewModel(),
   IErrorHandler by _errorHandler,
   INavigationHandler by _navigationHandler
{

   // region P E O P L E   L I S T   S C R E E N
   private val _peopleUiStateFlow = MutableStateFlow(PeopleUiState())
   //val peopleUiStateFlow = _peopleUiStateFlow.asStateFlow()
   // transform intent into an action
   fun onProcessPeopleIntent(intent: PeopleIntent) {
      logInfo(TAG, "onProcessIntent: $intent")
      when (intent) {
         is PeopleIntent.Fetch -> {} //fetch()
      }
   }

   // Refreshable Scenario
   private val reloadTrigger = MutableSharedFlow<Unit>(replay = 1)
   init {
      triggerFetch()
   }

   val peopleUiStateFlow: StateFlow<PeopleUiState> = reloadTrigger.flatMapLatest {
      _personRepository.getAll().map { resultData ->
         when (resultData) {
            is ResultData.Success -> _peopleUiStateFlow.update { it: PeopleUiState ->
               it.copy(isLoading = false, people = resultData.data.toList())
            }
            is ResultData.Error -> handleErrorEvent(resultData.throwable)
            is ResultData.Loading -> _peopleUiStateFlow.update { it: PeopleUiState ->
               it.copy(isLoading = true)
            }
         }
         return@map _peopleUiStateFlow.value
      }
   }.stateIn(
      viewModelScope,
      SharingStarted.WhileSubscribed(),
      PeopleUiState()
   )
   fun triggerFetch() {
      viewModelScope.launch {
         reloadTrigger.emit(Unit)
      }
   }
// endregion

   // region P E R S O N   S C R E E N
   private val _personUiStateFlow = MutableStateFlow(PersonUiState())
   val personUiStateFlow = _personUiStateFlow.asStateFlow()

   // transform intent into an action
   fun onProcessPersonIntent(intent: PersonIntent) {
      //logInfo(TAG, "onProcessIntent: $intent")
      when (intent) {
         is PersonIntent.FirstNameChange -> onFirstNameChange(intent.firstName)
         is PersonIntent.LastNameChange -> onLastNameChange(intent.lastName)
         is PersonIntent.EmailChange -> onEmailChange(intent.email)
         is PersonIntent.PhoneChange -> onPhoneChange(intent.phone)

         is PersonIntent.Clear -> clearState()
         is PersonIntent.FetchById -> fetchById(intent.id)
         is PersonIntent.Create -> create(_personUiStateFlow.value.person)
         is PersonIntent.Update -> update()
         is PersonIntent.Remove -> remove(intent.person)
         is PersonIntent.UndoRemove -> undoRemove()

         is PersonIntent.WriteToLocalStorage -> writeToLocalStorage(intent.bitmap)
         is PersonIntent.WriteToMediaStore -> writeToMediaStore(intent.bitmap)

      }
   }

   // region onPersonIntents
   private fun onFirstNameChange(firstName: String) {
      if (firstName == _personUiStateFlow.value.person.firstName) return
      _personUiStateFlow.update { it: PersonUiState ->
         it.copy(person = it.person.copy(firstName = firstName))
      }
   }
   private fun onLastNameChange(lastName: String) {
      if (lastName == _personUiStateFlow.value.person.lastName) return
      _personUiStateFlow.update { it: PersonUiState ->
         it.copy(person = it.person.copy(lastName = lastName))
      }
   }
   private fun onEmailChange(email: String?) {
      if (email == null || email == _personUiStateFlow.value.person.email) return
      _personUiStateFlow.update { it: PersonUiState ->
         it.copy(person = it.person.copy(email = email))
      }
   }
   private fun onPhoneChange(phone: String?) {
      if (phone == null || phone == _personUiStateFlow.value.person.phone) return
      _personUiStateFlow.update { it: PersonUiState ->
         it.copy(person = it.person.copy(phone = phone))
      }
   }
// endregion

   // region clearState, fetchByid, create, update
   private fun clearState() {
      _personUiStateFlow.update { it.copy(person = Person(id = newUuid())) }
   }

   private fun fetchById(personId: String) {
      logDebug(TAG, "fetchPersonById: $personId")
      viewModelScope.launch(_exceptionHandler) {
         when (val resultData = _personRepository.getById(personId)) {
            is ResultData.Success -> _personUiStateFlow.update { it: PersonUiState ->
               it.copy(person = resultData.data ?: Person(id = newUuid()))  // new UiState
            }
            is ResultData.Error -> handleErrorEvent(resultData.throwable)
            else -> Unit
         }
      }
   }

   private fun create(pPerson: Person) {
      logDebug(TAG, "createPerson()")
      viewModelScope.launch(_exceptionHandler) {
         var person = pPerson
         // handle new localImage
         val (localImage, remoteImage) = handleLocalImage(
            person = person,
            deleteLocalImage = _localStorageRepository::deleteFile,
            deleteRemoteImage = _imageRepository::delete,
            postImage =  _imageRepository::post,
            handleErrorEvent = ::handleErrorEvent,
            scope = viewModelScope,
            exceptionHandler = _exceptionHandler
         )
         person = person.copy(localImage = localImage, remoteImage = remoteImage)
         when (val resultData = _personRepository.post(person)) {
            is ResultData.Success -> triggerFetch()
            is ResultData.Error -> handleErrorEvent(resultData.throwable)
            else -> Unit
         }
      }
   }

   private fun update() {
      logDebug(TAG, "updatePerson()")
      viewModelScope.launch(_exceptionHandler) {
         var person = _personUiStateFlow.value.person
         // handle new localImage
         val (localImage, remoteImage) = handleLocalImage(
            person = person,
            deleteLocalImage = _localStorageRepository::deleteFile,
            deleteRemoteImage =  _imageRepository::delete,
            postImage = _imageRepository::post,
            handleErrorEvent = ::handleErrorEvent,
            scope = viewModelScope,
            exceptionHandler = _exceptionHandler
         )
         person = person.copy(localImage = localImage, remoteImage = remoteImage)
         when (val resultData = _personRepository.put(person)) {
            is ResultData.Success -> triggerFetch()
            is ResultData.Error -> handleErrorEvent(resultData.throwable)
            else -> Unit
         }
      }
   }
   // endregion

   // region remove, undoRemove
   private var removedPerson: Person? = null
   private fun remove(person: Person) {
      logDebug(TAG, "removePerson()")
      viewModelScope.launch(_exceptionHandler) {
         // save remote image path on local storage
         val remoteAsLocalImage = viewModelScope.async(_exceptionHandler) {
            removeRemoteImage(
               person = person,
               getImage = _imageRepository::get,
               deleteLocalImage = _localStorageRepository::deleteFile,
               deleteRemoteImage = _imageRepository::delete,
               handleErrorEvent = ::handleErrorEvent,
               scope = viewModelScope,
               exceptionHandler = _exceptionHandler
            )
         }.await()
         when (val resultData = _personRepository.delete(person)) {
            is ResultData.Success -> {
               removedPerson = person.copy(localImage = remoteAsLocalImage, remoteImage = null )
               triggerFetch()
            }
            is ResultData.Error -> handleErrorEvent(resultData.throwable)
            else -> Unit
         }
      }
   }

   private fun undoRemove() {
      removedPerson?.let { person ->
         create(person)
      }
   }
   // endregion

   // region writeToLocalStorage, WriteToMediaTore
   private fun writeToLocalStorage(bitmap: Bitmap) {
      logDebug(TAG, "writeToLocalStorage()")
      viewModelScope.launch(_exceptionHandler) {
         when (val resultData = _localStorageRepository.writeImage(bitmap)) {
            is ResultData.Success -> _personUiStateFlow.update { it: PersonUiState ->
               it.copy(person = it.person.copy(localImage = resultData.data))
            }
            is ResultData.Error -> handleErrorEvent(resultData.throwable)
            else -> Unit
         }
      }
   }
   private fun writeToMediaStore(bitmap: Bitmap) {
      logDebug(TAG, "writeToMediaStore()")
      viewModelScope.launch(_exceptionHandler) {
         when (val resultData = _mediaStoreRepository.saveImage(bitmap)) {
            is ResultData.Error -> handleErrorEvent(resultData.throwable)
            else -> Unit
         }
      }
   }
   // endregion

// endregion

   // region Validate input fields
   // =========================================
   // V A L I D A T E   I N P U T   F I E L D S
   // =========================================
   // validate all input fields after user finished input into the form
   fun validate(isInput: Boolean): Boolean {
      val person = _personUiStateFlow.value.person

      if (validateAndLogError(_validator.validateFirstName(person.firstName)) &&
         validateAndLogError(_validator.validateLastName(person.lastName)) &&
         validateAndLogError(_validator.validateEmail(person.email)) &&
         validateAndLogError(_validator.validatePhone(person.phone))
      ) {
         // write data to repository
         if (isInput) this.create(person)
         else this.update()
         return true
      } else {
         return false
      }
   }

   private fun validateAndLogError(validationResult: Pair<Boolean, String>): Boolean {
      val (error, message) = validationResult
      if (error) {
         onErrorEvent(ErrorParams(message = message, navEvent = null))
         logError(TAG, message)
         return false
      }
      return true
   }
// endregion


   @OptIn(coil.annotation.ExperimentalCoilApi::class)
   override fun onCleared() {
      logInfo(TAG, "onCleared(): clear caches")
      _imageLoader.memoryCache?.clear()
      _imageLoader.diskCache?.clear()

      super.onCleared()
   }

   companion object {
      private const val TAG = "<-PersonViewModel"
   }
}