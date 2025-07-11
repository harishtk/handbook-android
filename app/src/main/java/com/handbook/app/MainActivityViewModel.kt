package com.handbook.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import com.handbook.app.core.domain.repository.UserDataRepository
import com.handbook.app.core.domain.model.UserData
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    userDataRepository: UserDataRepository,
) : ViewModel() {

    private val userData = userDataRepository.userData
        .distinctUntilChanged { old, new ->
            old.userId == new.userId
        }

    val uiState: StateFlow<MainActivityUiState> = userData
        .map {
            if (it.serverUnderMaintenance) {
                MainActivityUiState.Maintenance
            } else {
                MainActivityUiState.Success(it)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = MainActivityUiState.Loading
        )

}

sealed interface MainActivityUiState {
    data object Loading : MainActivityUiState
    @Deprecated("Not yet implemented")
    data object Maintenance : MainActivityUiState
    data class Login(val data: UserData) : MainActivityUiState
    data class Success(val data: UserData) : MainActivityUiState
}