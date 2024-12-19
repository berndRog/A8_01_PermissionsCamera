import de.rogallab.mobile.data.remote.network.httpStatusMessage
import de.rogallab.mobile.data.repositories.logResponse
import de.rogallab.mobile.domain.ResultData
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import retrofit2.Response

fun <Dto, T> webServiceRequestByFlow(
   tag: String,
   dispatcher: CoroutineDispatcher,
   exceptionHandler: CoroutineExceptionHandler,
   toEntity: (Dto) -> T,
   apiCall: suspend () -> Response<List<Dto>>
): Flow<ResultData<List<T>>> = flow {

   try {
      emit(ResultData.Loading)

      // make the api call
      val response: Response<List<Dto>> = apiCall()
      logResponse(tag, response)

      // if the response is successful, emit the body
      if (response.isSuccessful) {
         val body: List<Dto>? = response.body()
         body?.let { dtos: List<Dto> ->
            val items: List<T> = dtos.map { toEntity(it) }
            emit(ResultData.Success(items))
         } ?: run {
            val t = RuntimeException("response body() is null")
            emit(ResultData.Error(t))
         }
      } else {
         val statusMessage = httpStatusMessage(response.code())
         emit(ResultData.Error(RuntimeException(statusMessage)))
      }
   } catch (e: Exception) {
      emit(ResultData.Error(e))
   }
}.flowOn(dispatcher + exceptionHandler)
 .catch { t: Throwable -> emit(ResultData.Error(t)) }