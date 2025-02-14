package com.hrysenko.dailyquest.presentation.login

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.hrysenko.dailyquest.R
import com.hrysenko.dailyquest.databinding.ActivityLoginBinding
import com.hrysenko.dailyquest.models.AppDatabase
import com.hrysenko.dailyquest.models.user.room.User
import com.hrysenko.dailyquest.presentation.main.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var database: AppDatabase
    private lateinit var preferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        preferences = getSharedPreferences("dailyquest_prefs", MODE_PRIVATE)


        if (preferences.getBoolean("is_registered", false)) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

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
        val sex = binding.loginSex.selectedItem.toString()
        val goal = binding.loginGoal.selectedItem.toString()
        val physLevel = binding.loginPhysLevel.selectedItem.toString()

        if (name.isEmpty() || age == null || height == null || weight == null) {
            Toast.makeText(this, getString(R.string.pl_fill_all), Toast.LENGTH_SHORT).show()
            return
        }

        val user = User(
            name = name,
            age = age,
            height = height,
            weight = weight,
            sex = sex,
            goal = goal,
            physLevel = physLevel
        )

        lifecycleScope.launch(Dispatchers.IO) {
            database.userDao().insertUser(user)

            // Зберігаємо інформацію, що користувач зареєстрований
            preferences.edit().putBoolean("is_registered", true).apply()

            withContext(Dispatchers.Main) {
                Toast.makeText(this@LoginActivity, "Дані збережено!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                finish()
            }
        }
    }
}
