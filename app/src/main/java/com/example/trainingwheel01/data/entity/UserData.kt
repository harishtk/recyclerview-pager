package com.example.trainingwheel01.data.entity

import androidx.room.Entity

@Entity(tableName = "users")
data class UserData(
    val name: String
)