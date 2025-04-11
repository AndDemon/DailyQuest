package com.hrysenko.dailyquest.presentation.login

import androidx.lifecycle.ViewModel
import com.hrysenko.dailyquest.models.user.room.User

class LoginViewModel : ViewModel() {
    var name: String = ""
    var age: Int? = null
    var height: Double? = null
    var weight: Double? = null
    var sex: String = ""
    var goal: String = ""
    var physLevel: String = ""

    fun isDataComplete(): Boolean {
        return name.isNotEmpty() && age != null && height != null && weight != null &&
                sex.isNotEmpty() && goal.isNotEmpty() && physLevel.isNotEmpty()
    }

    fun toUser(): User {
        return User(
            name = name,
            age = age ?: 0,
            height = height ?: 0.0,
            weight = weight ?: 0.0,
            sex = sex,
            goal = goal,
            physLevel = physLevel
        )
    }
}