package de.rogallab.mobile.data.repositories

import de.rogallab.mobile.data.remote.network.httpStatusMessage
import retrofit2.Response
import kotlinx.coroutines.withContext
import de.rogallab.mobile.domain.ResultData
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler

suspend fun <Dto, T> webServiceRequestById(
   tag: String,
   id: String,
   dispatcher: CoroutineDispatcher,
   exceptionHandler: CoroutineExceptionHandler,
   toEntity: (Dto) -> T,
   apiCall: suspend (String) -> Response<Dto?>,
): ResultData<T?> =
   withContext(dispatcher + exceptionHandler) {
      try {
         // do the api call
         val response = apiCall(id)
         logResponse(tag, response)

         // check if the response is successful
         if (response.isSuccessful) {
            // dto is not null, so we return the entity
            response.body()?.let { dto: Dto ->
               ResultData.Success( toEntity(dto) )
            }
            // dto can be null, so we return null
            ?: ResultData.Success(null)
         } else {
            val statusCode = response.code()
            val statusMessage = httpStatusMessage(statusCode)
            ResultData.Error(RuntimeException("Error: $statusCode $statusMessage"))
         }
      } catch (t: Throwable) {
         ResultData.Error(t)
      }
   }