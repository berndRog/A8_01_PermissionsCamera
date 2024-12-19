package de.rogallab.mobile.data.remote.network

import de.rogallab.mobile.AppStart
import de.rogallab.mobile.domain.utilities.logDebug
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

fun createRetrofit(
   okHttpClient: OkHttpClient,
   gsonConverterFactory: GsonConverterFactory
) : Retrofit {
   logDebug("<-Retrofit", "create()")
   return Retrofit.Builder()
      .baseUrl(AppStart.BASE_URL)
      .client(okHttpClient)
      .addConverterFactory(gsonConverterFactory)
      .build()
}