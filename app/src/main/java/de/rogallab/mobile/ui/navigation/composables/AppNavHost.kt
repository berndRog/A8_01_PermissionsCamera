package de.rogallab.mobile.ui.navigation.composables

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import de.rogallab.mobile.domain.utilities.logInfo
import de.rogallab.mobile.domain.utilities.logVerbose
import de.rogallab.mobile.ui.INavigationHandler
import de.rogallab.mobile.ui.features.home.HomeScreen
import de.rogallab.mobile.ui.features.home.HomeViewModel
import de.rogallab.mobile.ui.navigation.NavEvent
import de.rogallab.mobile.ui.navigation.NavScreen
import de.rogallab.mobile.ui.navigation.NavState
import de.rogallab.mobile.ui.features.people.PersonValidator
import de.rogallab.mobile.ui.features.people.PersonViewModel
import de.rogallab.mobile.ui.features.people.composables.PeopleListScreen
import de.rogallab.mobile.ui.features.people.composables.PersonScreen
import kotlinx.coroutines.flow.combine
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
fun AppNavHost(
   // create a NavHostController with a factory function
   navController: NavHostController = rememberNavController(),
   homeViewModel: HomeViewModel = koinViewModel<HomeViewModel>(),
   peopleViewModel: PersonViewModel = koinViewModel<PersonViewModel>()
) {
   val tag = "<-AppNavHost"
   val duration = 700  // in milliseconds

   //region N A V I G A T I O N    H O S T ---------------------------------------------------------
   NavHost(
      navController = navController,
      startDestination = NavScreen.Home.route,
      enterTransition = { enterTransition(duration) },
      exitTransition = { exitTransition(duration) },
      popEnterTransition = { popEnterTransition(duration) },
      popExitTransition = { popExitTransition(duration) }
   ) {
      composable(route = NavScreen.Home.route) {
         HomeScreen(
            viewModel = homeViewModel,
         )
      }

      composable(route = NavScreen.PeopleList.route) {
         PeopleListScreen(
            viewModel = peopleViewModel,
         )
      }
      composable(route = NavScreen.PersonInput.route) {
         PersonScreen(
            viewModel = peopleViewModel,
            validator = koinInject<PersonValidator>(),
            isInputScreen = true,
            id = null
         )
      }
      composable(
         route = NavScreen.PersonDetail.route + "/{personId}",
         arguments = listOf(navArgument("personId") { type = NavType.StringType }),
      ) { backStackEntry ->
         val id = backStackEntry.arguments?.getString("personId")
         PersonScreen(
            viewModel = peopleViewModel,
            validator = koinInject<PersonValidator>(),
            isInputScreen = false,
            id = id
         )
      }
   }
   //endregion
   val homeNavState by   homeViewModel.navStateFlow.collectAsStateWithLifecycle()
   logVerbose(tag, "homeNavState: $homeNavState")

   //region O N E   T I M E   E V E N T S   N A V I G A T I O N ------------------------------------
   // Observing the navigation state and handle navigation
   val combinedNavEvent: NavEvent? by combine(
      homeViewModel.navStateFlow,
      peopleViewModel.navStateFlow,
   ) { states: Array<NavState> ->
      // Access the elements by index
      val home = states[0]
      val people = states[1]
      // Combine the states as needed, here we just return the first non-null event
      home.navEvent ?: people.navEvent
   }.collectAsStateWithLifecycle(initialValue = null)

   combinedNavEvent?.let { navEvent: NavEvent ->
      logInfo(tag, "navEvent: $navEvent")
      // check which ViewModel has the navEvent
      val navigationHandler: INavigationHandler = when {
         homeViewModel.navStateFlow.value.navEvent == navEvent -> homeViewModel
         peopleViewModel.navStateFlow.value.navEvent == navEvent -> peopleViewModel
         else -> return@let
      }

      logVerbose(tag, "navEvent: $navEvent")
      when (navEvent) {
         is NavEvent.NavigateHome -> {
            navController.navigate(NavScreen.Home.route) {
               popUpTo(navController.graph.startDestinationRoute ?: NavScreen.Home.route) {
                  saveState = true
               }
               launchSingleTop = true
               restoreState = true
            }
            navigationHandler.onNavEventHandled()
         }

         is NavEvent.NavigateLateral -> {
            navController.navigate(navEvent.route) {
               popUpTo(navController.graph.findStartDestination().id) {
                  saveState = true
               }
               launchSingleTop = true
               restoreState = true
            }
            navigationHandler.onNavEventHandled()
         }

         is NavEvent.NavigateForward -> {
            // Each navigate() pushes the given destination
            // to the top of the stack.
            navController.navigate(navEvent.route)
            navigationHandler.onNavEventHandled()
         }
         is NavEvent.NavigateReverse -> {
            navController.navigate(navEvent.route) {
               popUpTo(navEvent.route) {  // clears the back stack up to the given route
                  inclusive = true        // ensures that any previous instances of
               }                          // that route are removed
            }
            navigationHandler.onNavEventHandled()
         }
         is NavEvent.NavigateBack -> {
            navController.popBackStack()
            navigationHandler.onNavEventHandled()
         }
      } // end of when (it) {
   } // end of navEvent?.let { it: NavEvent ->
   //endregion
}

//region A N I M A T I O N S -----------------------------------------------------------------------
private fun AnimatedContentTransitionScope<NavBackStackEntry>.enterTransition(
   duration: Int
) = fadeIn(
   animationSpec = tween(duration)
) + slideIntoContainer(
   animationSpec = tween(duration),
   towards = AnimatedContentTransitionScope.SlideDirection.Right
)

private fun AnimatedContentTransitionScope<NavBackStackEntry>.exitTransition(
   duration: Int
) = fadeOut(
   animationSpec = tween(duration)
) + slideOutOfContainer(
   animationSpec = tween(duration),
   towards = AnimatedContentTransitionScope.SlideDirection.Right
)

private fun popEnterTransition(
   duration: Int
) = scaleIn(
   initialScale = 0.1f,
   animationSpec = tween(duration)
) + fadeIn(animationSpec = tween(duration))

private fun popExitTransition(
   duration: Int
) = scaleOut(
   targetScale = 3.0f,
   animationSpec = tween(duration)
) + fadeOut(animationSpec = tween(duration))
//endregion