package de.rogallab.mobile.ui.navigation

import de.rogallab.mobile.domain.utilities.logVerbose
import de.rogallab.mobile.ui.INavigationHandler
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NavigationHandler(
   private val _coroutineScopeMain: CoroutineScope,
   private val _exceptionHandler: CoroutineExceptionHandler,
) : INavigationHandler {

   private val _tag: String = "<-NavigationHandler"

   private val _navStateFlow: MutableStateFlow<NavState> =
      MutableStateFlow(NavState())
   override val navStateFlow: StateFlow<NavState> =
      _navStateFlow.asStateFlow()

   // save the previous navigation event
   private var savedNavEvent: NavEvent? = null

   // navigate to the event
   override fun onNavigate(navEvent: NavEvent) {
      if (navEvent == savedNavEvent) return
      logVerbose(_tag, "onNavigate() event:${navEvent}")
      savedNavEvent = navEvent
      _navStateFlow.update { it: NavState ->
         it.copy(navEvent = navEvent)
      }
   }

   // delete the last navigation event when handled
   override fun onNavEventHandled() {
      _coroutineScopeMain.launch(_exceptionHandler) {
         delay(100) // Delay to ensure navigation has been processed
         logVerbose(_tag, "onNavEventHandled()")
         _navStateFlow.update { it: NavState ->
            it.copy(navEvent = null)
         }
         savedNavEvent = null
      }
   }
}