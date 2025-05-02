package com.hrysenko.dailyquest.models.user.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user")
data class User(
    @PrimaryKey val id: Int = 1,
    var name: String,                    // User's name
    var age: Int,                         // User's age
    var height: Double?,                   // User's height
    var weight: Double?,                   // User's weight
    var sex: String,                      // User's sex
    var goal: String,                     // User's fitness goal (e.g., lose weight, gain muscle)
    var physLevel: String,                // User's physical activity level
    var avatar: String? = null            // Avatar URL or local file path (nullable)
)
