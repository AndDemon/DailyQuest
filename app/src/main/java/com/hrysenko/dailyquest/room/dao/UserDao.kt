package com.hrysenko.dailyquest.room.dao
import androidx.room.*
import com.hrysenko.dailyquest.room.entities.User

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: User)

    @Update
    suspend fun updateUser(user: User)

    @Query("SELECT * FROM user WHERE id = 1 LIMIT 1")
    suspend fun getUser(): User?
}