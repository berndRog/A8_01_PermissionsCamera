package de.rogallab.mobile.data.remote.network

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class NetworkConnectivity(
   private val _networkConnection: NetworkConnection
) : Interceptor {

   override fun intercept(chain: Interceptor.Chain): Response {
      // Check
      if (_networkConnection.isWiFiOnline() || _networkConnection.isCellularOnline()) {
         val original: Request = chain.request()
         return chain.proceed(original)
      } else {
         throw IOException("Cellular and Wifi are not connected")
      }
   }
}