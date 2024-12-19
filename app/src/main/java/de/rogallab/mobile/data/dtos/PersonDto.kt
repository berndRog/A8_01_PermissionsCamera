package de.rogallab.mobile.data.dtos

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import de.rogallab.mobile.domain.utilities.newUuid

@Entity(
   tableName="Person"
)
data class PersonDto (
   @SerializedName("firstname")
   val firstName: String = "",
   @SerializedName("lastname")
   val lastName: String = "",
   val email: String? = null,
   val phone: String? = null,
   @SerializedName("localimage")
   val localImage: String? = null,
   @SerializedName("remoteimage")
   val remoteImage: String? = null,
   @PrimaryKey
   val id: String = newUuid()  // Uuid
)