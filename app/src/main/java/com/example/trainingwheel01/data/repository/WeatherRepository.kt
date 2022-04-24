package com.example.trainingwheel01.data.repository

import com.example.trainingwheel01.data.Result
import com.example.trainingwheel01.data.source.remote.model.WeatherResponse
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.CacheControl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import retrofit2.HttpException
import javax.inject.Inject

class WeatherRepository @Inject constructor(
    private val httpClient: OkHttpClient
) {

    val NO_CACHE = CacheControl.Builder().noCache().build()

    fun getWeatherData(
        lat: String,
        lng: String
    ) : Flow<Result<WeatherResponse>> = flow {
        emit(Result.Loading)

        val result = kotlin.runCatching {
            val url = "https://api.openweathermap.org/data/2.5/weather?lat=${lat}&lon=${lng}&appid=${WEATHER_API_KEY}"
            val request = Request.Builder().url(url).cacheControl(NO_CACHE).build()
            val response = httpClient.newCall(request).execute()
            if (response.isSuccessful && response.body != null) {
                val jsonString = response.body?.string()!!
                return@runCatching Gson().fromJson(jsonString, WeatherResponse::class.java)
            } else {
                throw Exception("Invalid response: ${response.code}")
            }
        }

        result.fold(
            onFailure = { emit(Result.Error(Exception(it))) },
            onSuccess = { emit(Result.Success(it)) }
        )
    }.flowOn(Dispatchers.IO)
}

private const val WEATHER_API_KEY: String = "4dff3623fe348bf46d200d2e83a89254"