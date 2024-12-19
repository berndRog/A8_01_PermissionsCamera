package de.rogallab.mobile.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import de.rogallab.mobile.data.dtos.PersonDto
import kotlinx.coroutines.flow.Flow

@Dao
interface IPersonDao {
   // QUERIES ---------------------------------------------
   @Query("SELECT * FROM Person")
   fun selectAll(): Flow<List<PersonDto>>

   @Query("SELECT * FROM Person WHERE id = :personId")
   suspend fun findById(personId: String): PersonDto?

   @Query("SELECT COUNT(*) FROM Person")
   suspend fun count(): Int

   // COMMANDS --------------------------------------------
   @Insert(onConflict = OnConflictStrategy.ABORT)
   suspend fun insert(personDto: PersonDto)
   @Insert(onConflict = OnConflictStrategy.ABORT)
   suspend fun insert(personDtos: List<PersonDto>)

   @Update
   suspend fun update(personDto: PersonDto)
   @Delete
   suspend fun remove(personDto: PersonDto)
}