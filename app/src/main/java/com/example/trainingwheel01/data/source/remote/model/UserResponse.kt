package com.example.trainingwheel01.data.source.remote.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class UserResponse(
    val info: Info,
    val results: List<Result>
) : Serializable

data class Result(
    val email: String,
    val picture: Picture,
    val nat: String,
    val phone: String,
    val name: Name,
    val location: Location,
    val registered: Registered,
    val login: Login,
    val dob: Dob
) : Serializable

data class Login(
    val uuid: String,
    val username: String
)

data class Dob(
    val date: String,
    val age: Int
)

data class Registered(
    val date: String,
    val age: Int
)

data class Location(
    val country: String,
    val city: String,
    @SerializedName("postcode") val postCode: String,
    val state: String,
    val coordinates: Coordinates,
    val street: Street
) : Serializable

data class Street(
    val name: String,
    val number: Int
) : Serializable

data class Coordinates(
    val longitude: String,
    val latitude: String
) : Serializable

data class Name(
    val first: String,
    val title: String,
    val last: String,
) : Serializable

data class Picture(
    val large: String,
    val medium: String,
    val thumbnail: String
) : Serializable

data class Info(
    val page: Int,
    val seed: String,
    val results: Int,
    val version: String
) : Serializable
