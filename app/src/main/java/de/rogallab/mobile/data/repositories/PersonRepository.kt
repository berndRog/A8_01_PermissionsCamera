package de.rogallab.mobile.data.repositories

import de.rogallab.mobile.data.dtos.PersonDto
import de.rogallab.mobile.data.local.IPersonDao
import de.rogallab.mobile.data.mapping.toPerson
import de.rogallab.mobile.data.mapping.toPersonDto
import de.rogallab.mobile.data.remote.IPersonWebservice
import de.rogallab.mobile.domain.IPersonRepository
import de.rogallab.mobile.domain.ResultData
import de.rogallab.mobile.domain.entities.Person
import webServiceRequestByFlow
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class PersonRepository(
   private val _personDao: IPersonDao,
   private val _webservice: IPersonWebservice,
   private val _dispatcher: CoroutineDispatcher,
   private val _exceptionHandler: CoroutineExceptionHandler
): IPersonRepository {

   // region L O C A L   D A T A B A S E
   override fun selectAll(): Flow<ResultData<List<Person>>> = flow {
      try {
         _personDao.selectAll().collect { personDtos: List<PersonDto> ->
            val people: List<Person> = personDtos.map { it.toPerson() }
            //throw RuntimeException("getAll() failed")
            emit(ResultData.Success(people))
         }
      } catch (t: Throwable) {
         emit(ResultData.Error(t))
      }
   }.flowOn(_dispatcher + _exceptionHandler)

   override suspend fun findById(id: String): ResultData<Person?> =
      withContext(_dispatcher+_exceptionHandler) {
         return@withContext try {
            val personDto: PersonDto? = _personDao.findById(id)
            val person: Person? = personDto?.toPerson()
            ResultData.Success(person)
         } catch (t: Throwable) {
            ResultData.Error(t)
         }
      }

   override suspend fun count(): ResultData<Int> =
      withContext(_dispatcher+_exceptionHandler) {
         return@withContext try {
            ResultData.Success(_personDao.count())
         } catch (t: Throwable) {
            ResultData.Error(t)
         }
      }

   override suspend fun insert(person: Person): ResultData<Unit> =
      withContext(_dispatcher+_exceptionHandler) {
         return@withContext try {
            _personDao.insert( person.toPersonDto() )
            ResultData.Success(Unit)
         } catch (t: Throwable) {
            ResultData.Error(t)
         }
      }

   override suspend fun insert(people: List<Person>): ResultData<Unit> =
      withContext(_dispatcher+_exceptionHandler) {
         return@withContext try {
            _personDao.insert( people.map { it.toPersonDto() } )
            ResultData.Success(Unit)
         } catch (t: Throwable) {
            ResultData.Error(t)
         }
      }

   override suspend fun update(person: Person): ResultData<Unit> =
      withContext(_dispatcher+_exceptionHandler) {
         return@withContext try {
            _personDao.update( person.toPersonDto() )
            ResultData.Success(Unit)
         } catch (t: Throwable) {
            ResultData.Error(t)
         }
      }

   override suspend fun remove(person: Person): ResultData<Unit> =
      withContext(_dispatcher+_exceptionHandler) {
         return@withContext try {
            _personDao.remove( person.toPersonDto() )
            ResultData.Success(Unit)
         } catch (t: Throwable) {
            ResultData.Error(t)
         }
      }
   //endregion

   // region W E B S E R V I C E
   override fun getAll(): Flow<ResultData<List<Person>>> =
      webServiceRequestByFlow<PersonDto, Person>(
         tag = TAG,
         dispatcher = _dispatcher,
         exceptionHandler = _exceptionHandler,
         toEntity = { it.toPerson() },
         apiCall = { _webservice.getAll() }
      ).catch {
         emit(ResultData.Error(it))
      }.flowOn(_dispatcher)

   override suspend fun getById(id: String): ResultData<Person?> =
      webServiceRequestById<PersonDto, Person>(
         tag = TAG,
         id = id,
         dispatcher = _dispatcher,
         exceptionHandler = _exceptionHandler,
         toEntity = { it.toPerson() },
         apiCall = { _webservice.getById(id) }
      )

   override suspend fun post(person: Person): ResultData<String> =
      webServiceCommand(_dispatcher, _exceptionHandler) {
         _webservice.post(person.toPersonDto())
      }

   override suspend fun put(person: Person): ResultData<String> =
      webServiceCommand(_dispatcher, _exceptionHandler) {
         _webservice.put(person.id, person.toPersonDto())
      }

   override suspend fun delete(person: Person): ResultData<String> =
      webServiceCommand(_dispatcher, _exceptionHandler) {
         _webservice.delete(person.id)
      }
   // endregion


   companion object {
      private const val TAG = "<-PersonRepository"
   }
}