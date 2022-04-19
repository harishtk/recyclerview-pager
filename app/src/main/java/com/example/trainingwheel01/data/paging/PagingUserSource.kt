package com.example.trainingwheel01.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.trainingwheel01.data.entity.UserData
import com.example.trainingwheel01.data.source.remote.ApiService
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException

class PagingUserSource(
    private val apiService: ApiService
) : PagingSource<Int, UserData>() {

    private var seed = ""

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, UserData> {
        Timber.d("LoadResult: LoadParams $params")
        val position = params.key ?: STARTING_INDEX
        return try {
            val response = apiService.getUsers(limit = NETWORK_PAGE_SIZE, page = position, seed = seed)
            val userData: MutableList<UserData> = mutableListOf()
            seed = response.info.seed
            if (response.results.isNotEmpty()) {
                response.results.forEach { userData.add(UserData(it.name.first)) }
            }

            val nextKey = if (userData.isEmpty() || userData.size < NETWORK_PAGE_SIZE) {
                null
            } else {
                position + (params.loadSize / NETWORK_PAGE_SIZE)
            }
            return LoadResult.Page(
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

    override fun getRefreshKey(state: PagingState<Int, UserData>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

}

const val NETWORK_PAGE_SIZE = 10
const val STARTING_INDEX    = 1