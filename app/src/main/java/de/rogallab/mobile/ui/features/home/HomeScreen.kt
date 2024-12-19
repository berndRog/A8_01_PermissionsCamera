package de.rogallab.mobile.ui.features.home

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.captionBar
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeGestures
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.rogallab.mobile.domain.utilities.logDebug
import de.rogallab.mobile.domain.utilities.logError
import de.rogallab.mobile.domain.utilities.logVerbose
import de.rogallab.mobile.ui.errors.ErrorParams
import de.rogallab.mobile.ui.errors.ErrorState
import de.rogallab.mobile.ui.errors.showError
import de.rogallab.mobile.ui.navigation.NavEvent
import de.rogallab.mobile.ui.navigation.NavScreen
import de.rogallab.mobile.ui.permissions.RequestPermissions
import kotlinx.coroutines.CompletableDeferred

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
   viewModel: HomeViewModel
) {

   val activity = LocalContext.current as Activity
   BackHandler{
      activity.finish()
   }

   val windowInsets = WindowInsets.systemBars
      .add(WindowInsets.captionBar)
      .add(WindowInsets.safeGestures)

   val snackbarHostState = remember { SnackbarHostState() }

   Scaffold(
      modifier = Modifier
         .fillMaxSize()
         .padding(windowInsets.asPaddingValues())
         .background(color = MaterialTheme.colorScheme.surface),
      snackbarHost = {
         SnackbarHost(snackbarHostState) { data ->
            Snackbar(
               snackbarData = data,
               actionOnNewLine = true
            )
         }
      }) { paddingValues: PaddingValues ->

      Column(
         modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp),
         verticalArrangement = Arrangement.Center,
         horizontalAlignment = Alignment.CenterHorizontally
      ) {

         val permissionsDeferred: CompletableDeferred<Boolean> =
            remember { CompletableDeferred() }
         // Wait for the permissions result, then continue
         var permissionsGranted: Boolean
            by remember { mutableStateOf<Boolean>(false) }

         RequestPermissions(
            permissionsDeferred,
            handleErrorEvent = { it ->
               logError("<-HomeScreen", "Error: ${it.message}")
               viewModel.handleErrorEvent(it) }
         )

         LaunchedEffect(Unit) {
            // wait until permissions are granted
            permissionsGranted = permissionsDeferred.await()
            if (!permissionsGranted)
               logError("<-HomeScreen", "Permissions not granted")
            else
               viewModel.onNavigate(NavEvent.NavigateLateral(NavScreen.PeopleList.route))

         }

      }
   }

   val errorState: ErrorState
      by viewModel.errorStateFlow.collectAsStateWithLifecycle()
   logVerbose("<-HomeScreen", "errorState: $errorState")
   LaunchedEffect(errorState.params) {
      errorState.params?.let { params: ErrorParams ->
         logDebug("<-HomeScreen", "ErrorState: ${errorState.params}")
         showError(snackbarHostState, params, viewModel::onNavigate)
         viewModel::onErrorEventHandled
      }
   }
}