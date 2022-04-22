package com.example.trainingwheel01.data.source.remote

import com.example.trainingwheel01.data.source.remote.model.UserResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {

    @GET("api/")
    suspend fun getUsers(
        @Query("results") limit: Int,
        @Query("page") page: Int,
        @Query("seed") seed: String
    ): UserResponse

    companion object {
        const val BASE_URL = "https://randomuser.me/"
    }
}