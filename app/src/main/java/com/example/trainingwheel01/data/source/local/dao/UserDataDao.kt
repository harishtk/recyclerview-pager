package com.example.trainingwheel01.data.source.local.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.trainingwheel01.data.entity.UserData

@Dao
interface UserDataDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(users: List<UserData>)

    @Query("SELECT * FROM users")
    fun getUsers(): PagingSource<Int, UserData>

    @Query("DELETE FROM users")
    suspend fun clearUsers()
}