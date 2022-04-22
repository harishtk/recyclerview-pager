package com.example.trainingwheel01.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.io.Serializable

@Entity(tableName = "users")
data class UserData(
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
    val streetNumber: Int
) : Serializable {
    @PrimaryKey(autoGenerate = true)
    var id: Long? = null
}