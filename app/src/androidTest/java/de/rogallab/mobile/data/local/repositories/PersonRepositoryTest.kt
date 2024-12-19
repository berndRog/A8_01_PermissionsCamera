package de.rogallab.mobile.data.local.repositories

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import de.rogallab.mobile.data.local.IPersonDao
import de.rogallab.mobile.data.local.database.AppDatabase
import de.rogallab.mobile.data.local.database.SeedDatabase
import de.rogallab.mobile.data.local.seed.Seed
import de.rogallab.mobile.data.mapping.toPerson
import de.rogallab.mobile.dataTestModules
import de.rogallab.mobile.domain.IPersonRepository
import de.rogallab.mobile.domain.ResultData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.component.get
import org.koin.core.logger.Level
import org.koin.test.KoinTest
import org.koin.test.KoinTestRule
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.fail

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class PersonRepositoryTest: KoinTest {

   private lateinit var _database: AppDatabase
   private lateinit var _personDao: IPersonDao
   private lateinit var _personRepository: IPersonRepository
   private lateinit var _seed: Seed
   private lateinit var _seedDatabase: SeedDatabase

   @get:Rule
   val koinTestRule = KoinTestRule.create {
      androidLogger(Level.ERROR) // Optional
      androidContext(ApplicationProvider.getApplicationContext())
      modules(dataTestModules)
   }

   @Before
   fun setUp() {
      //
      val standardTestDispatcher = get<TestDispatcher>()
      Dispatchers.setMain(standardTestDispatcher)

      _database = get<AppDatabase>()
      _database.clearAllTables()

      _personDao = get<IPersonDao>()
      _personRepository = get<IPersonRepository>()
      _seed = get<Seed>()
      _seedDatabase = get()
   }

   @After
   fun teardown() {
      _database.clearAllTables()
      _database.close()
      // Delete the database file
      //ApplicationProvider.getApplicationContext<Context>()
      //   .deleteDatabase(AppStart.DATABASENAME + "_Test")

      Dispatchers.resetMain()
   }

   @Test
   fun testGetById() = runTest {
      // Arrange
      _seedDatabase.seedPerson()
      val person = _seed.personDtos[0].toPerson()
      // Act
      val resultData = _personRepository.getById(person.id)
      // Assert
      when(resultData) {
         is ResultData.Success -> {
            val actualPerson = resultData.data
            assertEquals(actualPerson, person)
         }
         is ResultData.Error -> {
            fail(resultData.throwable.message ?: "Exception thrown in GetById()")
         }
         else -> Unit
      }
   }
   @Test
   fun testGetAll() = runTest {
      // Arrange
      _seedDatabase.seedPerson()
      // Act
      val resultData = _personRepository.getAll().first()
      // Arrange
      when(resultData) {
         is ResultData.Success -> {
            // actual
            val actualPeople = resultData.data
            // expected
            val people = _seed.personDtos.map { it.toPerson() }
            assertEquals(26, actualPeople.size)
            assertEquals(actualPeople, people)
         }
         is ResultData.Error -> {
            fail(resultData.throwable.message ?: "Exception thrown in GetAll()")
         }
         else -> Unit
      }
   }
   @Test
   fun testCreate() = runTest {
      // Arrange
      val person = _seed.personDtos[0].toPerson()
      // Act
      assertIs<ResultData.Success<*>>( _personRepository.insert(person))
      // Assert
      val resultData = _personRepository.getById(person.id)
      when(resultData) {
         is ResultData.Success -> {
            // actual
            val actualPerson = resultData.data
            assertEquals(actualPerson, person)
         }
         is ResultData.Error -> {
            fail(resultData.throwable.message ?: "Exception thrown in Create()")
         }
         else -> Unit
      }
   }
   @Test
   fun testUpdate() = runTest {
      // Arrange
      val person = _seed.personDtos[0].toPerson()
      _personRepository.insert(person)
      // Act
      val updatedPerson = person.copy(firstName = "Arne updated", lastName = "Arndt updated")
      assertIs<ResultData.Success<*>>(_personRepository.update(updatedPerson))
      // Assert
      val resultData = _personRepository.getById(person.id)
      when(resultData) {
         is ResultData.Success -> {
            // actual
            val actualPerson = resultData.data
            assertEquals(actualPerson, updatedPerson)
         }
         is ResultData.Error -> {
            fail(resultData.throwable.message ?: "Exception thrown in Update()")
         }
         else -> Unit
      }
   }
   @Test
   fun testDelete() = runTest {
      // Arrange
      _seedDatabase.seedPerson()
      val personDto = _seed.personDtos[0]
      // Act
      _personDao.remove(personDto)
      // Assert
      val actual = _personDao.findById(personDto.id)
      assertNull(actual)
   }


   companion object {
      private const val TAG = "<-PersonDaoTest"
   }


}