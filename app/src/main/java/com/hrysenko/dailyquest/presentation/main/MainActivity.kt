package com.hrysenko.dailyquest.presentation.main

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.hrysenko.dailyquest.databinding.ActivityMainBinding
import com.hrysenko.dailyquest.models.AppDatabase
import com.hrysenko.dailyquest.presentation.login.LoginActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var database: AppDatabase
    private lateinit var preferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = AppDatabase.getDatabase(this)
        preferences = getSharedPreferences("dailyquest_prefs", MODE_PRIVATE)

        loadUserData()

        // Логіка виходу
        binding.logoutButton.setOnClickListener {
            preferences.edit().putBoolean("is_registered", false).apply()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
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
                    binding.userGoal.text = "Мета: ${it.goal}"
                    binding.userPhysLevel.text = "Фізичний рівень: ${it.physLevel}"
                }
            }
        }
    }
}
