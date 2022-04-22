package com.example.trainingwheel01.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.io.Serializable

@Entity(
    tableName = "users",
    indices = [
        Index(value = arrayOf("name"), unique = false)
    ]
)
data class UserData(
    @PrimaryKey
    val uuid: String,
    val name: String,
    val email: String,
    val nat: String,
    val phone: String,
    val longitude: String,
    val latitude: String,
    val photoLarge: String,
    val photoMedium: String,
    val photoThumbnail: String,
    val country: String,
    val city: String,
    val postCode: String,
    val state: String,
    val streetName: String,
    val streetNumber: Int,
    val registeredOn: String,
    val createdAt: Long
) : Serializable