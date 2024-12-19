package de.rogallab.mobile.ui.features.people

sealed class PeopleIntent {
   data object Fetch : PeopleIntent()
}