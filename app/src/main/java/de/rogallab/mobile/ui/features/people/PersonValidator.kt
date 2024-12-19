package de.rogallab.mobile.ui.features.people

import android.content.Context
import android.util.Patterns
import de.rogallab.mobile.R

class PersonValidator(
   private val _context: Context
) {

   private val _charMin: Int by lazy {
      _context.getString(R.string.errorCharMin).toInt()
   }

   private val _charMax: Int by lazy {
      _context.getString(R.string.errorCharMax).toInt()
   }

   private val _firstNameTooShort: String by lazy {
      _context.getString(R.string.errorFirstNameTooShort)
   }
   private val _firstNameTooLong: String by lazy {
      _context.getString(R.string.errorFirstNameTooLong)
   }
   private val _lastNameTooShort: String by lazy {
      _context.getString(R.string.errorLastNameTooShort)
   }
   private val _lastNameTooLong: String by lazy {
      _context.getString(R.string.errorLastNameTooLong)
   }
   private val _emailInValid: String by lazy {
      _context.getString(R.string.errorEmail)
   }
   private val _phoneInValid: String by lazy {
      _context.getString(R.string.errorPhone)
   }

   // Validation is unrelated to state management and simply returns a result
   // We can call the validation function directly in the Composables
   fun validateFirstName(firstName: String): Pair<Boolean, String> =
      if (firstName.isEmpty() || firstName.length < _charMin)
         Pair(true, _firstNameTooShort)
      else if (firstName.length > _charMax )
         Pair(true, _firstNameTooLong)
      else
         Pair(false, "")

   fun validateLastName(lastName: String): Pair<Boolean, String> =
      if (lastName.isEmpty() || lastName.length < _charMin)
         Pair(true, _lastNameTooShort)
      else if (lastName.length > _charMax )
         Pair(true, _lastNameTooLong)
      else
         Pair(false, "")

   fun validateEmail(email: String?): Pair<Boolean, String> {
      if(email.isNullOrEmpty()) return Pair(false, "")
      when (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
         true -> return Pair(false, "") // email ok
         false -> return Pair(true, _emailInValid)
      }
   }

   fun validatePhone(phone: String?): Pair<Boolean, String> {
      if(phone.isNullOrEmpty()) return Pair(false, "")
      when (Patterns.PHONE.matcher(phone).matches()) {
         true -> return Pair(false,"")   // phone ok
         false -> return Pair(true, _phoneInValid)
      }
   }
}