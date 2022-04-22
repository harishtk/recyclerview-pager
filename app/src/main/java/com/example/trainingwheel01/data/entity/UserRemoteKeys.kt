package com.example.trainingwheel01.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "remote_keys")
class UserRemoteKeys(
    @PrimaryKey
    val uuid: String,
    val prevKey: Int?,
    val nextKey: Int?
)