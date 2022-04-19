package com.example.trainingwheel01.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.trainingwheel01.data.entity.UserData
import com.example.trainingwheel01.data.paging.NETWORK_PAGE_SIZE
import com.example.trainingwheel01.data.paging.PagingUserSource
import com.example.trainingwheel01.data.source.remote.ApiService
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.flow.Flow
import timber.log.Timber
import javax.inject.Inject

@ViewModelScoped
class DefaultRepository @Inject constructor(
    private val apiService: ApiService
) {
    fun getUsers(): Flow<PagingData<UserData>> {
        Timber.d("Fetching..")
        return Pager(
            config = PagingConfig(
                pageSize = NETWORK_PAGE_SIZE,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { PagingUserSource(apiService) }
        ).flow
    }
}