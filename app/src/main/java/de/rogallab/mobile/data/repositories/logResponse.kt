package de.rogallab.mobile.data.repositories

import de.rogallab.mobile.domain.utilities.logVerbose
import de.rogallab.mobile.domain.utilities.maxChar
import retrofit2.Response

// helper function to log the response
fun <T> logResponse(
   tag: String,
   response: Response<T>
) {
   logVerbose(tag, "Request ${response.raw().request.method} ${response.raw().request.url}")
   logVerbose(tag, "Request Headers")
   response.raw().request.headers.forEach {
      val text = "   %-15s %s".format(it.first, it.second )
      logVerbose(tag, text)
   }

   val ms = response.raw().receivedResponseAtMillis - response.raw().sentRequestAtMillis
   logVerbose(tag, "took $ms ms")
   logVerbose(tag, "Response isSuccessful ${response.isSuccessful()}")

   logVerbose(tag, "Response Headers")
   response.raw().headers.forEach {
      val text = "   %-15s %s".format(it.first, it.second)
      logVerbose(tag, text)
   }

   logVerbose(tag, "   Status Code ${response.code().toString().maxChar(100)}")
   logVerbose(tag, "   Status Message ${response.message().toString().maxChar(100)}")
}