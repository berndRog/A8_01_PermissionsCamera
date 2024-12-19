package de.rogallab.mobile.data.remote.network

import de.rogallab.mobile.domain.utilities.logDebug
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

fun createOkHttpClient(
   bearerToken: BearerToken,
   apiKey: ApiKey,
   networkConnectivity: NetworkConnectivity,
   loggingInterceptor: HttpLoggingInterceptor
) : OkHttpClient {
   logDebug("<-OkHttpClient", "create()")
   return OkHttpClient.Builder()
      .connectTimeout(300, TimeUnit.SECONDS)
      .readTimeout(300, TimeUnit.SECONDS)
      .writeTimeout(300, TimeUnit.SECONDS)
      .addInterceptor(bearerToken)
      .addInterceptor(apiKey)
      .addInterceptor(networkConnectivity)
      .addInterceptor(loggingInterceptor)
      .build()
}
