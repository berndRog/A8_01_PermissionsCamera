package de.rogallab.mobile.data.remote.network

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

class ApiKey(
   private val _apiKey: String? = null
) : Interceptor {

   override fun intercept(chain: Interceptor.Chain): Response {
      var request: Request = chain.request()
      if(_apiKey.isNullOrEmpty()) return chain.proceed(request)

      request = request.newBuilder()
         .header("X-API-Key",_apiKey)
         //          .header("X-Session", getServerSession())
         .method(request.method, request.body)
         .build()
      return chain.proceed(request)
   }
}