package de.rogallab.mobile

import androidx.room.Room
import de.rogallab.mobile.data.local.IPersonDao
import de.rogallab.mobile.data.local.database.AppDatabase
import de.rogallab.mobile.data.local.database.SeedDatabase
import de.rogallab.mobile.data.local.seed.Seed
import de.rogallab.mobile.data.remote.IPersonWebservice
import de.rogallab.mobile.data.remote.ImageWebservice
import de.rogallab.mobile.data.remote.network.ApiKey
import de.rogallab.mobile.data.remote.network.BearerToken
import de.rogallab.mobile.data.remote.network.NetworkConnection
import de.rogallab.mobile.data.remote.network.NetworkConnectivity
import de.rogallab.mobile.data.remote.network.createOkHttpClient
import de.rogallab.mobile.data.remote.network.createRetrofit
import de.rogallab.mobile.data.remote.network.createWebservice
import de.rogallab.mobile.data.repositories.PersonRepository
import de.rogallab.mobile.domain.ILocalStorageRepository
import de.rogallab.mobile.domain.IPersonRepository
import de.rogallab.mobile.domain.ImageRepository
import de.rogallab.mobile.domain.utilities.logError
import de.rogallab.mobile.domain.utilities.logInfo
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.TestDispatcher
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val dataTestModules = module {
   val tag = "<-dataTestModules"
   logInfo(tag, "single    -> CoroutineExceptionHandler")
   single<CoroutineExceptionHandler> {
      CoroutineExceptionHandler { _, exception ->
         logError(tag, "Coroutine exception: ${exception.localizedMessage}")
      }
   }

   single<TestCoroutineScheduler> {
      TestCoroutineScheduler().apply {  this.advanceUntilIdle() }
   }
   single<TestDispatcher> {
      StandardTestDispatcher( get<TestCoroutineScheduler>())
   }

   single<CoroutineScope> {
      CoroutineScope(SupervisorJob() + get<TestDispatcher>())
   }

   logInfo(tag, "single    -> Seed")
   single<Seed> {
      Seed(
         _resources = androidContext().resources,
         _localStorageRepository = get<ILocalStorageRepository>(),
         _coroutineScope = get<CoroutineScope>(),
         _dispatcher = get<TestDispatcher>()
      )
   }

   logInfo(tag, "single    -> SeedDatabase")
   single<SeedDatabase> {
      SeedDatabase(
         _database = get<AppDatabase>(),
         _personDao = get<IPersonDao>(),
         _seed = get<Seed>(),
         _dispatcher = get<TestDispatcher>(),
      )
   }

   logInfo(tag, "single    -> AppDatabase")
   single<AppDatabase> {
      Room.inMemoryDatabaseBuilder(
         androidContext(),
         AppDatabase::class.java
      ).build()
//    Room.databaseBuilder(
//       context = androidContext(),
//       klass = AppDatabase::class.java,
//       name = AppStart.DATABASENAME+"_Test"
//    ).build()
   }

   logInfo(tag, "single    -> IPersonDao")
   single<IPersonDao> { get<AppDatabase>().createPersonDao() }


   // remote OkHttp/Retrofit Webservice ---------------------------------------
   logInfo(tag, "single    -> NetworkConnection")
   single<NetworkConnection> {
      NetworkConnection(context = androidContext())
   }
   logInfo(tag, "single    -> NetworkConnectivity")
   single<NetworkConnectivity> { NetworkConnectivity(get<NetworkConnection>()) }

   logInfo(tag, "single    -> BearerToken")
   single<BearerToken> { BearerToken() }

   logInfo(tag, "single    -> ApiKey")
   single<ApiKey> { ApiKey(AppStart.API_KEY) }

   logInfo(tag, "single    -> HttpLoggingInterceptor")
   single<HttpLoggingInterceptor> {
      HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
   }
   logInfo(tag, "single    -> OkHttpClient")
   single<OkHttpClient> {
      createOkHttpClient(
         bearerToken = get<BearerToken>(),
         apiKey = get<ApiKey>(),
         networkConnectivity = get<NetworkConnectivity>(),
         loggingInterceptor = get<HttpLoggingInterceptor>()
      )
   }

   logInfo(tag, "single    -> GsonConverterFactory")
   single<GsonConverterFactory> { GsonConverterFactory.create() }

   logInfo(tag, "single    -> Retrofit")
   single<Retrofit> {
      createRetrofit(
         okHttpClient = get<OkHttpClient>(),
         gsonConverterFactory = get<GsonConverterFactory>()
      )
   }
   logInfo(tag, "single    -> IPersonWebservice")
   single<IPersonWebservice> {
      createWebservice<IPersonWebservice>(
         retrofit = get<Retrofit>(),
         webserviceName = "IPersonWebservice"
      )
   }
   logInfo(tag, "single    -> ImageWebservice")
   single<ImageWebservice> {
      createWebservice<ImageWebservice>(
         retrofit = get<Retrofit>(),
         webserviceName = "ImageWebservice"
      )
   }


   // Provide IPersonRepository
   logInfo(tag, "single    -> PersonRepository: IPersonRepository")
   single<IPersonRepository> {
      PersonRepository(
         _personDao = get<IPersonDao>(),
         _webservice = get(),
         _dispatcher = get<TestDispatcher>(),
         _exceptionHandler = get<CoroutineExceptionHandler>()
      )
   }
}