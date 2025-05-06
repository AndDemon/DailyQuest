package com.hrysenko.dailyquest.presentation.quests

data class Quest(
    val name: String,
    var amount: Int,
    val complexity: String,
    val date: String,
    var completed: Boolean = false
)