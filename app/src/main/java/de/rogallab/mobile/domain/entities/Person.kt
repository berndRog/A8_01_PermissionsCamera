package de.rogallab.mobile.domain.entities

data class Person (
   val firstName: String = "",
   val lastName: String = "",
   val email: String? = "",
   val phone: String? = "",
   val localImage: String? = "",
   val remoteImage: String? = "",
   val id: String,  // Uuid as String

   // Relations to other domainModel classes
)