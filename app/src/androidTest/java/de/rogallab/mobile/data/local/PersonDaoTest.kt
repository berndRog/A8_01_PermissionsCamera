package de.rogallab.mobile.data.local

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import de.rogallab.mobile.data.local.database.AppDatabase
import de.rogallab.mobile.data.local.database.SeedDatabase
import de.rogallab.mobile.data.local.seed.Seed
import de.rogallab.mobile.dataTestModules
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
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
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class PersonDaoTest: KoinTest {

   private lateinit var _database: AppDatabase
   private lateinit var _personDao: IPersonDao
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
      _seed = get<Seed>()
      _seedDatabase = get()
   }

   @After
   fun teardown() {
      _database.clearAllTables()
      _database.close()
      // Delete the database file
      // ApplicationProvider.getApplicationContext<Context>()
      //   .deleteDatabase(AppStart.DATABASENAME + "_Test")

      Dispatchers.resetMain()
   }

   @Test
   fun testSelectById() = runTest {
      // Arrange
      _seedDatabase.seedPerson()
      val personDto = _seed.personDtos[0]
      // Act
      val actual = _personDao.findById(personDto.id)
      // Assert
      assertEquals(actual, personDto)
   }
   @Test
   fun testSelectAll() = runTest {
      // Arrange
      _seedDatabase.seedPerson()
      // Act
      val actual = _personDao.selectAll().first()
      // Arrange
      assertEquals(26, actual.size)
      assertEquals(_seed.personDtos, actual)

   }
   @Test
   fun testInsert() = runTest {
      // Arrange
      val personDto = _seed.personDtos[0]
      // Act
      _personDao.insert(personDto)
      // Assert
      val actual = _personDao.findById(personDto.id)
      assertEquals(actual, personDto)
   }
   @Test
   fun testUpdate() = runTest {
      // Arrange
      val personDto = _seed.personDtos[0]
      _personDao.insert(personDto)
      // Act
      val updatedPersonDto = personDto.copy(firstName = "Arne updated", lastName = "Arndt updated")
      _personDao.update(updatedPersonDto)
      // Assert
      val actual = _personDao.findById(personDto.id)
      assertEquals(actual, updatedPersonDto)
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