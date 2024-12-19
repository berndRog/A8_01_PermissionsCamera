package de.rogallab.mobile.data.repositories

import de.rogallab.mobile.data.remote.network.httpStatusMessage
import de.rogallab.mobile.domain.ResultData
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.withContext
import retrofit2.Response

suspend fun webServiceCommand(
   dispatcher: CoroutineDispatcher,
   exceptionHandler: CoroutineExceptionHandler,
   apiCall: suspend () -> Response<Unit>
): ResultData<String> = withContext(dispatcher + exceptionHandler) {
   try {
      // do the webservice api call
      val response = apiCall()
      // get the response code
      val responseCode = httpStatusMessage(response.code())
      if (response.isSuccessful) {
         return@withContext ResultData.Success(responseCode)
      } else {
         return@withContext ResultData.Error(RuntimeException(responseCode))
      }
   } catch (t: Throwable) {
      return@withContext ResultData.Error(t)
   }
}