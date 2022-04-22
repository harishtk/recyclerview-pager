package com.example.trainingwheel01.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.trainingwheel01.data.Result
import com.example.trainingwheel01.data.entity.UserData
import com.example.trainingwheel01.data.paging.NETWORK_PAGE_SIZE
import com.example.trainingwheel01.data.paging.PagingUserSource
import com.example.trainingwheel01.data.paging.UserRemoteMediator
import com.example.trainingwheel01.data.source.local.AppDatabase
import com.example.trainingwheel01.data.source.remote.ApiService
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.lang.IllegalArgumentException
import javax.inject.Inject

@ViewModelScoped
class DefaultRepository @Inject constructor(
    private val apiService: ApiService,
    private val database: AppDatabase
) {
    /*fun getUsers(): Flow<PagingData<UserData>> {
        Timber.d("Fetching..")
        return Pager(
            config = PagingConfig(
                pageSize = NETWORK_PAGE_SIZE,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { PagingUserSource(apiService) }
        ).flow
    }*/

    @OptIn(ExperimentalPagingApi::class)
    fun getUsers(filter: String): Flow<PagingData<UserData>> {
        Timber.d("Filter: $filter")
        val pagingSourceFactory = {
            database.usersDao().getUsers()
            /*if (filter.trim().isNotEmpty()) {
                database.usersDao().getUsersByName(filter)
            } else {

            }*/
        }
        return Pager(
            config = PagingConfig(
                pageSize = NETWORK_PAGE_SIZE,
                enablePlaceholders = false
            ),
            remoteMediator = UserRemoteMediator(
                apiService, database
            ),
            pagingSourceFactory = pagingSourceFactory
        ).flow
    }

    fun getUserById(userId: String): Flow<Result<UserData>> = flow {
        emit(Result.Loading)
        val dbResult = database.usersDao().getUserByUuid(userId)
        dbResult.map {
            if (it.isNotEmpty()) emit(Result.Success(it.first()))
            else emit(Result.Error(IllegalArgumentException("No user found $userId")))
        }
    }
}