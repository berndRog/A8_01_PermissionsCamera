package de.rogallab.mobile.ui.errors

import de.rogallab.mobile.domain.utilities.logDebug
import de.rogallab.mobile.domain.utilities.logError
import de.rogallab.mobile.ui.IErrorHandler
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

class ErrorHandler(
   private val _coroutineScopeMain: CoroutineScope,
   private val _exceptionHandler: CoroutineExceptionHandler,
) : IErrorHandler {

   private val _tag: String = "<-NavigationHandler"

   private val _errorStateFlow: MutableStateFlow<ErrorState> =
      MutableStateFlow(ErrorState())
   override val errorStateFlow: StateFlow<ErrorState> =
      _errorStateFlow.asStateFlow()

   override fun handleErrorEvent(t: Throwable) {
      onErrorEvent(ErrorParams(throwable = t, navEvent = null))
   }

   // save the previous navigation event
   private var savedParams: ErrorParams? = null
   override fun onErrorEvent(params: ErrorParams) {
      if (params == savedParams) return

      logDebug(_tag, "onErrorEvent()")
      savedParams = params

      params.throwable?.let {
         var error = "$it.localizedMessage"
         when (it) {
            is CancellationException -> error = "Cancellation error: $error"
            //   is RedirectResponseException -> error = "Redirect error: $error"
            //   is ClientRequestException -> error = "Client error: $error"
            //   is ServerResponseException -> error = "Server error: $error"
            //   is ConnectTimeoutException -> error = "Connection time out: $error"
            //   is SocketTimeoutException -> error = "Socket time out: $error"
            //   is UnknownHostException -> error = "no internet connection: $error"
         }
         logError(_tag, error)
         savedParams = params.copy(message = error)
      }
   }

   override fun onErrorEventHandled() {
      _coroutineScopeMain.launch(_exceptionHandler) {
         delay(100)
         logDebug(_tag, "onErrorEventHandled()")
         savedParams = null
         _errorStateFlow.update { it: ErrorState ->
            it.copy(params = savedParams)
         }
      }
   }
}