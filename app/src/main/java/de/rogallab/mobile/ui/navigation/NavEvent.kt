package de.rogallab.mobile.ui.navigation

sealed class NavEvent {

   // lateral navigation
   data class NavigateLateral(val route: String) : NavEvent()
   data object NavigateHome : NavEvent()

   // vertical navigation
   data class NavigateForward(val route: String) : NavEvent()
   data class NavigateReverse(val route: String) : NavEvent()
   data class NavigateBack(val route: String) : NavEvent()
}
