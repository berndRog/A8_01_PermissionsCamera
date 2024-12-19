package de.rogallab.mobile.ui.features.people

import android.graphics.Bitmap
import de.rogallab.mobile.domain.entities.Person

sealed class PersonIntent {
   data class  FirstNameChange(val firstName: String) : PersonIntent()
   data class  LastNameChange(val lastName: String) : PersonIntent()
   data class  EmailChange(val email: String?) : PersonIntent()
   data class  PhoneChange(val phone: String?) : PersonIntent()

   data object Clear : PersonIntent()
   data class  FetchById(val id: String) : PersonIntent()
   data object Create : PersonIntent()
   data object Update : PersonIntent()
   data class  Remove(val person: Person) : PersonIntent()
   data object UndoRemove : PersonIntent()

   data class  WriteToLocalStorage(val bitmap: Bitmap) : PersonIntent()
   data class  WriteToMediaStore(val bitmap: Bitmap) : PersonIntent()
}