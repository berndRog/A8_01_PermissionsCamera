package de.rogallab.mobile.ui.errors

import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import de.rogallab.mobile.domain.utilities.logDebug
import de.rogallab.mobile.domain.utilities.logVerbose
import de.rogallab.mobile.ui.navigation.NavEvent

suspend fun showError(
   snackbarHostState: SnackbarHostState,  // State ↓
   params: ErrorParams,                   // State ↓
   onNavigate: (NavEvent) -> Unit,        // Event ↑
) {
   logDebug("<-showError", "params: $params")
   // Show Snackbar
   when (val snackbarResult = snackbarHostState.showSnackbar(
      message = params.throwable?.message ?: params.message,
      actionLabel = params.actionLabel,
      withDismissAction = params.withUndoAction,
      duration = params.duration
   )) {
       SnackbarResult.ActionPerformed -> {
            logVerbose("<-showError", "Snackbar action performed")
            params.onUndoAction.invoke()
       }
       SnackbarResult.Dismissed -> {
            logVerbose("<-showError", "Snackbar dismissed")
       }
       else  -> {
         logVerbose("<-showError", "Snackbar timeout")
      }
   }

   logDebug("<-showError", "delete event")

   // navigate to target
   params.navEvent?.let { navEvent ->
      onNavigate(navEvent)
   }
}