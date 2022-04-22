package com.example.trainingwheel01.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.trainingwheel01.data.Result
import com.example.trainingwheel01.data.entity.UserData
import com.example.trainingwheel01.data.repository.DefaultRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

class UserDetailViewModel @AssistedInject constructor(
    @Assisted private val userId: String,
    private val repository: DefaultRepository
) : ViewModel() {

    val state: StateFlow<UiState>

    init {
        state = repository.getUserById(userId)
            .map { UiState(it) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
                initialValue = UiState(Result.Loading)
            )
    }

    @AssistedFactory
    interface Factory {
        fun create(userId: String): UserDetailViewModel
    }

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun provideFactory(
            assistedFactory: Factory,
            userId: String
        ) : ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return assistedFactory.create(userId) as T
            }
        }
    }
}

data class UiState(
    val userData: Result<UserData>
)