package com.hrysenko.dailyquest.presentation.main

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.hrysenko.dailyquest.R
import com.hrysenko.dailyquest.databinding.ActivityMainBinding
import com.hrysenko.dailyquest.room.database.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var database: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "dailyquest_db"
        ).build()

        loadUserData()
    }

    private fun loadUserData() {
        lifecycleScope.launch(Dispatchers.IO) {
            val user = database.userDao().getUser()

            withContext(Dispatchers.Main) {
                user?.let {
                    binding.userName.text = "Ім'я: ${it.name}"
                    binding.userAge.text = "Вік: ${it.age}"
                    binding.userHeight.text = "Зріст: ${it.height} см"
                    binding.userWeight.text = "Вага: ${it.weight} кг"
                    binding.userSex.text = "Стать: ${it.sex}"
                }
            }
        }
    }
}
