package com.example.trainingwheel01.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.trainingwheel01.data.Result
import com.example.trainingwheel01.data.entity.UserData
import com.example.trainingwheel01.data.repository.DefaultRepository
import com.example.trainingwheel01.data.repository.WeatherRepository
import com.example.trainingwheel01.data.source.remote.model.WeatherResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val weatherRepository: WeatherRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    val state: StateFlow<UiState>

    val accept: (UiAction) -> Unit

    init {
        val actionStateFlow = MutableSharedFlow<UiAction>()

        val getWeather = actionStateFlow
            .filterIsInstance<UiAction.GetWeather>()
            .distinctUntilChanged()
            .onStart { emit(UiAction.GetWeather(lat = "", lng = "")) }


        val weatherDataFlow: Flow<Result<WeatherResponse>> = getWeather
            .flatMapLatest {
                Timber.tag("Location.Msg").d("Updating weather $it")
                weatherRepository.getWeatherData(it.lat, it.lng)
            }

        state = weatherDataFlow
            .map {
                Timber.tag("Location.Msg").d("Mapping state: $it")
                UiState(it)
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
                initialValue = UiState()
            )

        accept = { action ->
            Timber.tag("Location.Msg").d("Action: $action")
            viewModelScope.launch { actionStateFlow.emit(action) }
        }
    }

    // Expose the repo fun
    fun getWeatherInfo(lat: String, lng: String) = weatherRepository.getWeatherData(lat, lng)
}

sealed class UiAction {
    data class GetWeather(val lat: String, val lng: String): UiAction()
}

data class UiState(
    val weatherData: Result<WeatherResponse> = Result.Loading
)