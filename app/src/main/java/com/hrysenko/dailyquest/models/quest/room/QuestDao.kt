package com.hrysenko.dailyquest.models.quest.room
import androidx.room.*

@Dao
interface QuestDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuest(quest: Quest)

    @Update
    suspend fun updateQuest(quest: Quest)

    @Delete
    suspend fun deleteQuest(quest: Quest)

    @Query("SELECT * FROM quests ORDER BY id DESC")
    fun getAllQuests(): kotlinx.coroutines.flow.Flow<List<Quest>>
}