package de.rogallab.mobile.ui.features.home

import androidx.compose.runtime.Immutable
import de.rogallab.mobile.domain.entities.Person
import de.rogallab.mobile.domain.utilities.newUuid

@Immutable
data class HomeUiState(
   val home: String = "",
)