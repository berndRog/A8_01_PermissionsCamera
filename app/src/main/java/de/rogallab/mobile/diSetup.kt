package de.rogallab.mobile

import androidx.room.Room
import coil.ImageLoader
import de.rogallab.mobile.data.local.IPersonDao
import de.rogallab.mobile.data.local.database.AppDatabase
import de.rogallab.mobile.data.local.database.SeedDatabase
import de.rogallab.mobile.data.local.seed.Seed
import de.rogallab.mobile.data.mediastore.MediaStoreRepository
import de.rogallab.mobile.data.remote.IPersonWebservice
import de.rogallab.mobile.data.remote.ImageWebservice
import de.rogallab.mobile.data.remote.network.ApiKey
import de.rogallab.mobile.data.remote.network.BearerToken
import de.rogallab.mobile.data.remote.network.NetworkConnection
import de.rogallab.mobile.data.remote.network.NetworkConnectivity
import de.rogallab.mobile.data.remote.network.createOkHttpClient
import de.rogallab.mobile.data.remote.network.createRetrofit
import de.rogallab.mobile.data.remote.network.createWebservice
import de.rogallab.mobile.data.repositories.ImageRepositoryImpl
import de.rogallab.mobile.data.io.LocalStorageRepository
import de.rogallab.mobile.data.remote.SeedWebservice
import de.rogallab.mobile.data.repositories.PersonRepository
import de.rogallab.mobile.domain.ILocalStorageRepository
import de.rogallab.mobile.domain.IMediaStoreRepository
import de.rogallab.mobile.domain.IPersonRepository
import de.rogallab.mobile.domain.ImageRepository
import de.rogallab.mobile.domain.utilities.logError
import de.rogallab.mobile.domain.utilities.logInfo
import de.rogallab.mobile.ui.IErrorHandler
import de.rogallab.mobile.ui.INavigationHandler
import de.rogallab.mobile.ui.errors.ErrorHandler
import de.rogallab.mobile.ui.features.home.HomeViewModel
import de.rogallab.mobile.ui.navigation.NavigationHandler
import de.rogallab.mobile.ui.features.people.PersonViewModel
import de.rogallab.mobile.ui.features.people.PersonValidator
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

typealias CoroutineDispatcherMain = CoroutineDispatcher
typealias CoroutineDispatcherIo = CoroutineDispatcher
typealias CoroutineScopeMain = CoroutineScope
typealias CoroutineScopeIo = CoroutineScope

val domainModules: Module = module {
   val tag = "<-domainModules"


   logInfo(tag, "factory   -> CoroutineExceptionHandler")
   factory<CoroutineExceptionHandler> {
      CoroutineExceptionHandler { _, exception ->
         logError(tag, "Coroutine exception: ${exception.localizedMessage}")
      }
   }
   logInfo( tag, "factory  -> CoroutineDispatcherMain")
   factory<CoroutineDispatcherMain> { Dispatchers.Main }

   logInfo(tag, "factory   -> CoroutineDispatcherIo)")
   factory<CoroutineDispatcherIo>{ Dispatchers.IO }


   logInfo(tag, "factory   -> CoroutineScopeMain")
   factory<CoroutineScopeMain> {
      CoroutineScope(
         SupervisorJob() +
            get<CoroutineDispatcherIo>()
      )
   }

   logInfo(tag, "factory   -> CoroutineScopeIo")
   factory<CoroutineScopeIo> {
      CoroutineScope(
         SupervisorJob() +
            get<CoroutineDispatcherIo>()
      )
   }
}

val dataModules = module {
   val tag = "<-dataModules"

   logInfo(tag, "single    -> Seed")
   single<Seed> {
      Seed(
         _resources = androidContext().resources,
         _localStorageRepository = get<ILocalStorageRepository>(),
         _coroutineScope = get<CoroutineScopeIo>(),
         _dispatcher = get<CoroutineDispatcherIo>()
      )
   }
   logInfo(tag, "single    -> SeedDatabase")
   single<SeedDatabase> {
      SeedDatabase(
         _database = get<AppDatabase>(),
         _personDao = get<IPersonDao>(),
         _seed = get<Seed>(),
         _dispatcher = get<CoroutineDispatcherIo>(),
      )
   }

   logInfo(tag, "single    -> AppDatabase")
   single {
      Room.databaseBuilder(
         context = androidContext(),
         klass = AppDatabase::class.java,
         name = AppStart.DATABASE_NAME
      ).build()
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

   logInfo(tag, "single    -> SeedWebservice")
   single<SeedWebservice> {
      SeedWebservice(
         _personRepository = get<IPersonRepository>(),
         _imageRepository = get<ImageRepository>(),
         _localStorageRepository = get<ILocalStorageRepository>(),
         _webservice = get<IPersonWebservice>(),
         _seed = get<Seed>(),
         _dispatcher = get<CoroutineDispatcherIo>(),
         _exceptionHandler = get<CoroutineExceptionHandler>()
      )
   }

   logInfo(tag, "single    -> LocalStorageRepository: ILocalStorageRepository")
   single<ILocalStorageRepository> {
      LocalStorageRepository(
         _context =  androidContext(),
         _dispatcher = get<CoroutineDispatcherIo>(),
         _exceptionHandler = get<CoroutineExceptionHandler>()
      )
   }

   logInfo(tag, "single    -> PersonRepository: IPersonRepository")
   single<IPersonRepository> {
      PersonRepository(
         _personDao =  get<IPersonDao>(),
         _webservice = get<IPersonWebservice>(),
         _dispatcher = get<CoroutineDispatcherIo>(),
         _exceptionHandler = get<CoroutineExceptionHandler>()
      )
   }

   logInfo(tag, "single    -> ImageRepositoryImpl: ImageRepository")
   single<ImageRepository> {
      ImageRepositoryImpl(
         _context = androidContext(),
         _webService = get<ImageWebservice>(),
         _localStorageRepository = get<ILocalStorageRepository>(),
         _dispatcher = get<CoroutineDispatcherIo>(),
         _exceptionHandler = get<CoroutineExceptionHandler>()
      )
   }

   logInfo(tag, "single    -> LocalStorageRepository: ILocalStorageRepository")
   single<ILocalStorageRepository> {
      LocalStorageRepository(
         _context = androidContext(),
         _dispatcher = get<CoroutineDispatcherIo>(),
         _exceptionHandler = get<CoroutineExceptionHandler>()
      )
   }

   logInfo(tag, "single    -> MediaStoreRepository: IMediaStoreRepository")
   single<IMediaStoreRepository> {
      MediaStoreRepository(
         _context = androidContext(),
         _dispatcher = get<CoroutineDispatcherIo>(),
         _exceptionHandler = get<CoroutineExceptionHandler>()
      )
   }
}


val uiModules: Module = module {
   val tag = "<-uiModules"

   logInfo(tag, "single    -> createImageLoader")
   single<ImageLoader> { createImageLoader(androidContext()) }

   logInfo(tag, "factory   -> PersonValidator")
   factory<PersonValidator> { PersonValidator(androidContext()) }

   logInfo(tag, "factory   -> IErrorHandler")
   factory<IErrorHandler> {
      ErrorHandler(
         _coroutineScopeMain = get<CoroutineScopeMain>(),
         _exceptionHandler = get<CoroutineExceptionHandler>()
      )
   }

   logInfo(tag, "factory   -> INavigationHandler")
   factory<INavigationHandler> {
      NavigationHandler(
         _coroutineScopeMain = get<CoroutineScopeMain>(),
         _exceptionHandler = get<CoroutineExceptionHandler>()
      )
   }

   logInfo(tag, "viewModel -> HomeViewModel")
   viewModel<HomeViewModel> {
      HomeViewModel(
         _errorHandler = get<IErrorHandler>(),
         _navHandler = get<INavigationHandler>(),
      )
   }

   logInfo(tag, "viewModel -> PersonViewModel")
   viewModel<PersonViewModel> {
      PersonViewModel(
         _personRepository = get<IPersonRepository>(),
         _imageRepository = get<ImageRepository>(),
         _localStorageRepository = get<ILocalStorageRepository>(),
         _mediaStoreRepository = get<IMediaStoreRepository>(),
         _validator = get<PersonValidator>(),
         _errorHandler = get<IErrorHandler>(),
         _navigationHandler = get<INavigationHandler>(),
         _imageLoader = get<ImageLoader>(),
         _exceptionHandler = get<CoroutineExceptionHandler>()
      )
   }
}