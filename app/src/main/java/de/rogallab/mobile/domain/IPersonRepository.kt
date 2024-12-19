package de.rogallab.mobile.domain

import de.rogallab.mobile.domain.entities.Person
import kotlinx.coroutines.flow.Flow

interface IPersonRepository {
    // local database
    fun selectAll(): Flow<ResultData<List<Person>>>
    suspend fun findById(id: String): ResultData<Person?>
    suspend fun count(): ResultData<Int>
    suspend fun insert(person: Person): ResultData<Unit>
    suspend fun insert(people: List<Person>): ResultData<Unit>
    suspend fun update(person: Person): ResultData<Unit>
    suspend fun remove(person: Person): ResultData<Unit>

    // remote webservice
    fun getAll(): Flow<ResultData<List<Person>>>
    suspend fun getById(id: String): ResultData<Person?>
    suspend fun post(person: Person): ResultData<String>
    suspend fun put(person: Person): ResultData<String>
    suspend fun delete(person: Person): ResultData<String>
}