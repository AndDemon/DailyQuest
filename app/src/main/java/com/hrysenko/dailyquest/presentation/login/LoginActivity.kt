package com.hrysenko.dailyquest.presentation.login

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.hrysenko.dailyquest.R
import com.hrysenko.dailyquest.databinding.ActivityLoginBinding
import com.hrysenko.dailyquest.presentation.main.MainActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
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

        if (savedInstanceState == null) {
            showFragment(IntroFragment())
        }
    }

    fun showFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.slide_out_left,
                R.anim.slide_in_left,
                R.anim.slide_out_right
            )
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()

        // Оновлення прогресу
        when (fragment) {
            is IntroFragment -> binding.progressBar.progress = 1
            is GenderFragment -> binding.progressBar.progress = 2
            is AgeFragment -> binding.progressBar.progress = 3
            is WeightFragment -> binding.progressBar.progress = 4
            is HeightFragment -> binding.progressBar.progress = 5
            is GoalFragment -> binding.progressBar.progress = 6
            is PhysLevelFragment -> binding.progressBar.progress = 7
            is FinishFragment -> binding.progressBar.progress = 8
        }
    }
}