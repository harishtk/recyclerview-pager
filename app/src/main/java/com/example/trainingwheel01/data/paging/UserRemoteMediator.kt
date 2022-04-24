package com.example.trainingwheel01.data.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.example.trainingwheel01.data.entity.UserData
import com.example.trainingwheel01.data.entity.UserRemoteKeys
import com.example.trainingwheel01.data.source.local.AppDatabase
import com.example.trainingwheel01.data.source.remote.ApiService
import com.example.trainingwheel01.data.source.remote.model.Result
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject

@OptIn(ExperimentalPagingApi::class)
class UserRemoteMediator @Inject constructor(
    private val apiService: ApiService,
    private val database: AppDatabase
) : RemoteMediator<Int, UserData>() {

    private var seed = ""

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, UserData>
    ): MediatorResult {

        val page = when (loadType) {
            LoadType.REFRESH -> {
                /*val remoteKeys = getRemoteKeyClosestToCurrentPosition(state)
                remoteKeys?.nextKey?.minus(1) ?: STARTING_INDEX*/
                STARTING_INDEX
            }
            LoadType.PREPEND -> {
                /*val remoteKeys = getRemoteKeyForFirstItem(state)
                val prevKey = remoteKeys?.prevKey
                    ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                prevKey*/
                return MediatorResult.Success(endOfPaginationReached = true)
            }
            LoadType.APPEND -> {
                val remoteKeys = getRemoteKeyForLastItem(state)
                val nextKey = remoteKeys?.nextKey
                if (nextKey == null) {
                    return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                }
                nextKey
            }
        }

        Timber.d("Mediator: page=$page")

        try {
            val usersResponse = apiService.getUsers(NETWORK_PAGE_SIZE, page/*, seed*/)

            seed = usersResponse.info.seed
            val users = usersResponse.results.map { fromResult(it) }
            val endOfPaginationReached = users.isEmpty() || users.size < NETWORK_PAGE_SIZE
            database.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    database.userRemoteKeysDao().clearRemoteKeys()
                    database.usersDao().clearUsers()
                }
                val prevKey = if (page == STARTING_INDEX) null else page - 1
                val nextKey = if (endOfPaginationReached) null else page + 1
                Timber.d("Mediator: page prev=$prevKey next=$nextKey endOfPaginationReached=$endOfPaginationReached")
                val keys = users.map {
                    UserRemoteKeys(uuid = it.uuid, prevKey = prevKey, nextKey = nextKey)
                }
                database.userRemoteKeysDao().insertAll(keys)
                database.usersDao().insertAll(users)
            }
            return MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        } catch (exception: IOException) {
            return MediatorResult.Error(exception)
        } catch (exception: HttpException) {
            return MediatorResult.Error(exception)
        }
    }

    private fun fromResult(result: Result): UserData {
        return UserData(
            uuid = result.login.uuid,
            name = result.name.first + " " + result.name.last,
            email = result.email,
            nat = result.nat,
            phone = result.phone,
            longitude = result.location.coordinates.longitude,
            latitude = result.location.coordinates.latitude,
            photoLarge = result.picture.large,
            photoMedium = result.picture.medium,
            photoThumbnail = result.picture.thumbnail,
            country = result.location.country,
            city = result.location.city,
            postCode = result.location.postCode,
            state = result.location.state,
            streetName = result.location.street.name,
            streetNumber = result.location.street.number,
            registeredOn = result.registered.date,
            createdAt = System.currentTimeMillis(),
            dob = result.dob.date,
            age = result.dob.age
        )
    }

    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, UserData>): UserRemoteKeys? {
        return state.pages.lastOrNull() { it.data.isNotEmpty() }?.data?.lastOrNull()
            ?.let { userData ->
                database.userRemoteKeysDao().remoteKeysUserUuid(userData.uuid)
            }
    }

    private suspend fun getRemoteKeyForFirstItem(state: PagingState<Int, UserData>): UserRemoteKeys? {
        return state.pages.firstOrNull() { it.data.isNotEmpty() }?.data?.firstOrNull()
            ?.let { userData ->
                database.userRemoteKeysDao().remoteKeysUserUuid(userData.uuid)
            }
    }

    private suspend fun getRemoteKeyClosestToCurrentPosition(
        state: PagingState<Int, UserData>
    ): UserRemoteKeys? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestItemToPosition(anchorPosition)?.uuid?.let {
                database.userRemoteKeysDao().remoteKeysUserUuid(it)
            }
        }
    }
}