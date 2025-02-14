package com.hrysenko.dailyquest.models
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.hrysenko.dailyquest.models.quest.room.QuestDao
import com.hrysenko.dailyquest.models.user.room.UserDao
import com.hrysenko.dailyquest.models.quest.room.Quest
import com.hrysenko.dailyquest.models.user.room.User

@Database(entities = [User::class, Quest::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun questDao(): QuestDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "dailyquest_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}