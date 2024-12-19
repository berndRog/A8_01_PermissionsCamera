package de.rogallab.mobile.data.remote.network

import okhttp3.Interceptor
import okhttp3.Response

class BearerToken(
   private val _bearerToken: String? = null

): Interceptor {
   override fun intercept(chain: Interceptor.Chain): Response {
      var request = chain.request()
      if(_bearerToken.isNullOrEmpty()) return chain.proceed(request)

      if (request.header("No-Authentication") == null) {
         val finalToken = "Bearer " + _bearerToken
         request = request.newBuilder()
            .addHeader("Authorization", finalToken)
            .build()
      }
      return chain.proceed(request)
   }
}
