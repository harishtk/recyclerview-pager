package com.example.trainingwheel01.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.trainingwheel01.data.Result
import com.example.trainingwheel01.data.entity.UserData
import com.example.trainingwheel01.data.repository.DefaultRepository
import com.example.trainingwheel01.data.repository.WeatherRepository
import com.example.trainingwheel01.data.source.remote.model.WeatherResponse
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import org.json.JSONObject
import timber.log.Timber
import java.lang.IllegalStateException

@OptIn(ExperimentalCoroutinesApi::class)
class UserDetailViewModel @AssistedInject constructor(
    @Assisted private val userId: String,
    private val repository: DefaultRepository,
    private val weatherRepository: WeatherRepository
) : ViewModel() {

    val state: StateFlow<UiState>

    init {
        val userDataFlow = repository.getUserById(userId)
        val weatherData: Flow<Result<WeatherResponse>> = userDataFlow.transformLatest { result ->
            when (result) {
                is Result.Loading -> emit(Result.Loading)
                is Result.Error -> emit(Result.Error(IllegalStateException("Failed to get weather data")))
                is Result.Success -> {
                    emitAll(weatherRepository.getWeatherData(result.data.latitude, result.data.longitude))
                }
            }
        }
        /*state = userDataFlow
            .map { UiState(userData = it) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
                initialValue = UiState(Result.Loading)
            )*/
        state = userDataFlow.combine(weatherData) { i, j ->
            UiState(i, j)
        }
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
    val userData: Result<UserData>,
    val userWeatherDataFlow: Result<WeatherResponse> = Result.Loading
)