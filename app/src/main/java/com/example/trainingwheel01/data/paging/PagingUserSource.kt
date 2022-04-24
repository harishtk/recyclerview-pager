package com.example.trainingwheel01.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.trainingwheel01.data.entity.UserData
import com.example.trainingwheel01.data.source.remote.ApiService
import com.example.trainingwheel01.data.source.remote.model.Result
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException

class PagingUserSource(
    private val apiService: ApiService
) : PagingSource<Int, UserData>() {

    private var seed = ""

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, UserData> {
        Timber.d("LoadResult: LoadParams $params ${params.loadSize}")
        val position = params.key ?: STARTING_INDEX
        return try {
            val response = apiService.getUsers(limit = NETWORK_PAGE_SIZE, page = position/*, seed = seed*/)
            val userData: MutableList<UserData> = mutableListOf()
            seed = response.info.seed
            if (response.results.isNotEmpty()) {
                response.results.map { fromResult(it) }.forEach { userData.add(it) }
            }

            val nextKey = if (userData.isEmpty() || userData.size < NETWORK_PAGE_SIZE) {
                null
            } else {
                position + (params.loadSize / NETWORK_PAGE_SIZE)
            }
            LoadResult.Page(
                data = userData,
                prevKey = if (position == STARTING_INDEX) null else position - 1,
                nextKey = nextKey
            )
        } catch (exception: IOException) {
            return LoadResult.Error(exception)
        } catch (exception: HttpException) {
            return LoadResult.Error(exception)
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

    override fun getRefreshKey(state: PagingState<Int, UserData>): Int? {
        return STARTING_INDEX
        /*return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }*/
    }

}

const val NETWORK_PAGE_SIZE = 25
const val STARTING_INDEX    = 1