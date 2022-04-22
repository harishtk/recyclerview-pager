package com.example.trainingwheel01.data.source.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.trainingwheel01.data.entity.UserRemoteKeys

@Dao
interface RemoteKeysDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(remoteKey: List<UserRemoteKeys>)

    @Query("SELECT * FROM remote_keys WHERE uuid = :uuid")
    suspend fun remoteKeysUserUuid(uuid: String): UserRemoteKeys?

    @Query("DELETE FROM remote_keys")
    suspend fun clearRemoteKeys()
}
