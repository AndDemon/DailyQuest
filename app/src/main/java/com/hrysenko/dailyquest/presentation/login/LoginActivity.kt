package com.hrysenko.dailyquest.presentation.login

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.hrysenko.dailyquest.databinding.ActivityLoginBinding
import com.hrysenko.dailyquest.presentation.main.MainActivity
import com.hrysenko.dailyquest.room.database.AppDatabase
import com.hrysenko.dailyquest.room.entities.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.concurrent.thread

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var database: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)


        database = AppDatabase.getDatabase(this)

        binding.loginBtn.setOnClickListener {
            saveUserData()
        }
    }

    private fun saveUserData() {
        val name = binding.loginName.text.toString().trim()
        val age = binding.loginAge.text.toString().toIntOrNull()
        val height = binding.loginHeight.text.toString().toDoubleOrNull()
        val weight = binding.loginWeight.text.toString().toDoubleOrNull()
        val sex = "Unknown"

        if (name.isEmpty() || age == null || height == null || weight == null) {
            Toast.makeText(this, "Будь ласка, заповніть усі поля!", Toast.LENGTH_SHORT).show()
            return
        }

        val user = User(name = name, age = age, height = height, weight = weight, sex = sex)


        lifecycleScope.launch(Dispatchers.IO) {
            database.userDao().insert(user)


            withContext(Dispatchers.Main) {
                Toast.makeText(this@LoginActivity, "Дані збережено!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                finish()
            }
        }
    }

}