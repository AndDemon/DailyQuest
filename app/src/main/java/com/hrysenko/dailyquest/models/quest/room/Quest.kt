package com.hrysenko.dailyquest.models.quest.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quests")
data class Quest(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    var name: String,
    var complexity: String,
    var amount: String,
    var isReady: Boolean
)
