package de.rogallab.mobile.ui

import de.rogallab.mobile.ui.navigation.NavEvent
import de.rogallab.mobile.ui.navigation.NavState
import kotlinx.coroutines.flow.StateFlow

interface INavigationHandler {
   // current navigation state
   val navStateFlow: StateFlow<NavState>
   // do navigation event, i.e. forward, reverse od back navigation
   fun onNavigate(navEvent: NavEvent)
   // navigation event handled
   fun onNavEventHandled()
}