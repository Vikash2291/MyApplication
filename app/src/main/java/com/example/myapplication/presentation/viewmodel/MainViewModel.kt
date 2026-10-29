package com.example.myapplication.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.preferences.AppSettings
import com.example.myapplication.data.local.preferences.PreferencesRepository
import com.example.myapplication.domain.usecase.ObserveCurrentUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

sealed interface MainUiState {
    data object Loading : MainUiState
    data class Ready(val isLoggedIn: Boolean, val settings: AppSettings) : MainUiState
}

/** Activity-level state: whether a session exists and the current theme settings. */
@HiltViewModel
class MainViewModel @Inject constructor(
    observeCurrentUser: ObserveCurrentUserUseCase,
    preferencesRepository: PreferencesRepository
) : ViewModel() {

    val uiState: StateFlow<MainUiState> = combine(
        observeCurrentUser(),
        preferencesRepository.settings
    ) { user, settings ->
        MainUiState.Ready(isLoggedIn = user != null, settings = settings)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MainUiState.Loading)
}
