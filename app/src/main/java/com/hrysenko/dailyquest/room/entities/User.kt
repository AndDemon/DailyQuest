package com.hrysenko.dailyquest.room.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user")
data class User(
    @PrimaryKey val id: Int = 1,
    var name: String,
    var age: Int,
    var height: Double,
    var weight: Double,
    var sex: String
)
