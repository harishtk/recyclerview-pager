package com.example.trainingwheel01.data.source.local.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.trainingwheel01.data.entity.UserData
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDataDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(users: List<UserData>)

    @Query("SELECT * FROM users")
    fun getUsers(): PagingSource<Int, UserData>

    @Query("SELECT * FROM users WHERE name LIKE :name")
    fun getUsersByName(name: String): PagingSource<Int, UserData>

    @Query("SELECT * FROM users WHERE uuid=:uuid")
    fun getUserByUuid(uuid: String): Flow<List<UserData>>

    @Query("DELETE FROM users")
    suspend fun clearUsers()
}