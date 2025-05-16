package com.hrysenko.dailyquest.presentation.profile

import android.app.Activity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.hrysenko.dailyquest.R
import com.hrysenko.dailyquest.databinding.ActivityEditProfileBinding
import com.hrysenko.dailyquest.models.AppDatabase
import com.hrysenko.dailyquest.models.user.room.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class EditProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var database: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "dailyquest_db"
        ).build()

        setupDropdownMenus()
        setupTrainingLocation()
        loadUserData()

        binding.editProfileBtn.setOnClickListener {
            saveUserData()
        }
    }

    private fun setupDropdownMenus() {
        // Gender options from string resource
        val genderOptions = resources.getStringArray(R.array.sex_options)
        val genderAdapter = ArrayAdapter(this, R.layout.dropdown_item, genderOptions)
        binding.loginGender.setAdapter(genderAdapter)

        // Goal options from string resource
        val goalOptions = resources.getStringArray(R.array.goal_options)
        val goalAdapter = ArrayAdapter(this, R.layout.dropdown_item, goalOptions)
        binding.loginGoal.setAdapter(goalAdapter)

        // Activity level options from string resource
        val activityLevelOptions = resources.getStringArray(R.array.phys_level_options)
        val activityLevelAdapter = ArrayAdapter(this, R.layout.dropdown_item, activityLevelOptions)
        binding.loginActivityLevel.setAdapter(activityLevelAdapter)
    }

    private fun setupTrainingLocation() {
        binding.trainingLocationGroup.setOnCheckedChangeListener { _, checkedId ->
            // No need to store trainingLocation here; it will be read in saveUserData
        }
    }

    private fun loadUserData() {
        lifecycleScope.launch(Dispatchers.IO) {
            val user = database.userDao().getUser()
            withContext(Dispatchers.Main) {
                if (user != null) {
                    binding.loginName.setText(user.name)
                    binding.loginAge.setText(user.age.toString())
                    binding.loginWeight.setText(user.weight?.let {
                        if (it % 1.0 == 0.0) it.toInt().toString() else String.format(Locale.US, "%.1f", it)
                    } ?: "")
                    binding.loginHeight.setText(user.height?.let {
                        if (it % 1.0 == 0.0) it.toInt().toString() else String.format(Locale.US, "%.1f", it)
                    } ?: "")
                    binding.loginGender.setText(user.sex, false)
                    binding.loginGoal.setText(user.goal, false)
                    binding.loginActivityLevel.setText(user.physLevel, false)
                    // Load training location
                    when (user.trainingLocation) {
                        "gym" -> binding.gymRadio.isChecked = true
                        "home" -> binding.homeRadio.isChecked = true
                        else -> binding.homeRadio.isChecked = true // Default to home
                    }
                } else {
                    Toast.makeText(this@EditProfileActivity, "User data not found", Toast.LENGTH_SHORT).show()
                    binding.homeRadio.isChecked = true // Default for new users
                }
            }
        }
    }

    private fun saveUserData() {
        val name = binding.loginName.text.toString().trim()
        val age = binding.loginAge.text.toString().toIntOrNull() ?: 0
        val height = binding.loginHeight.text.toString().toDoubleOrNull()
        val weight = binding.loginWeight.text.toString().toDoubleOrNull()
        val gender = binding.loginGender.text.toString()
        val goal = binding.loginGoal.text.toString()
        val activityLevel = binding.loginActivityLevel.text.toString()
        val trainingLocation = when (binding.trainingLocationGroup.checkedRadioButtonId) {
            R.id.gym_radio -> getString(R.string.gym)
            R.id.home_radio -> getString(R.string.home)
            else -> ""
        }

        // Validate all required fields, including trainingLocation
        if (name.isBlank() || age == 0 || weight == null || gender.isBlank() || goal.isBlank() || activityLevel.isBlank() || trainingLocation.isBlank()) {
            Toast.makeText(this, getString(R.string.please_fill_all_required_fields), Toast.LENGTH_SHORT).show()
            return
        }

        // Check constraints
        if (name.length > 12) {
            Toast.makeText(this, getString(R.string.name_too_long), Toast.LENGTH_SHORT).show()
            return
        }
        if (age < 10 || age > 100) {
            Toast.makeText(this, getString(R.string.invalid_age), Toast.LENGTH_SHORT).show()
            return
        }
        if (height != null && (height < 100 || height > 250)) {
            Toast.makeText(this, getString(R.string.invalid_height), Toast.LENGTH_SHORT).show()
            return
        }
        if (weight < 30 || weight > 200) {
            Toast.makeText(this, getString(R.string.invalid_weight), Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            val user = database.userDao().getUser() ?: User(
                id = 1,
                name = "",
                age = 0,
                height = null,
                weight = null,
                sex = "",
                goal = "",
                physLevel = "",
                trainingLocation = "home"
            )
            user.name = name
            user.age = age
            user.height = height
            user.weight = weight
            user.sex = gender
            user.goal = goal
            user.physLevel = activityLevel
            // Map localized trainingLocation to non-localized value
            user.trainingLocation = when (trainingLocation) {
                getString(R.string.gym), "Спортзал" -> "gym"
                getString(R.string.home), "Дім" -> "home"
                else -> "home"
            }

            database.userDao().insertUser(user)

            withContext(Dispatchers.Main) {
                Toast.makeText(this@EditProfileActivity, getString(R.string.profile_updated_successfully), Toast.LENGTH_SHORT).show()
                setResult(Activity.RESULT_OK)
                finish()
            }
        }
    }
}