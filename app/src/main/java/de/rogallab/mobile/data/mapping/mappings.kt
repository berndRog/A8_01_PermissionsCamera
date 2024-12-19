package de.rogallab.mobile.data.mapping

import de.rogallab.mobile.data.dtos.PersonDto
import de.rogallab.mobile.domain.entities.Person

fun PersonDto.toPerson(): Person = Person(
   firstName = firstName,
   lastName = lastName,
   email = email,
   phone = phone,
   localImage = localImage,
   remoteImage = remoteImage,
   id = id
)
fun Person.toPersonDto(): PersonDto = PersonDto(
   firstName = firstName,
   lastName = lastName,
   email = email,
   phone = phone,
   localImage = localImage,
   remoteImage = remoteImage,
   id = id
)


