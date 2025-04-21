package com.hrysenko.dailyquest.presentation.login

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.hrysenko.dailyquest.R
import com.hrysenko.dailyquest.databinding.ActivityLoginBinding
import com.hrysenko.dailyquest.presentation.main.MainActivity
import android.animation.ObjectAnimator

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val fragmentOrder = listOf(
        IntroFragment::class.java,
        GenderFragment::class.java,
        AgeFragment::class.java,
        WeightFragment::class.java,
        HeightFragment::class.java,
        GoalFragment::class.java,
        PhysLevelFragment::class.java
    )
    private var currentFragmentIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val prefs = getSharedPreferences("dailyquest_prefs", MODE_PRIVATE)
        val isRegistered = prefs.getBoolean("is_registered", false)

        if (isRegistered) {

            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }


        if (savedInstanceState == null) {
            showFragment(IntroFragment())
        }

        supportFragmentManager.addOnBackStackChangedListener {
            updateProgressBar()
        }
    }

    fun showFragment(fragment: Fragment) {
        val fragmentClass = fragment::class.java

        if (fragmentClass == FinishFragment::class.java) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(fragmentClass.simpleName)
                .commit()
            updateProgressBar()
            return
        }

        currentFragmentIndex = fragmentOrder.indexOf(fragmentClass)
        if (currentFragmentIndex == -1) {
            currentFragmentIndex = fragmentOrder.size - 1
        }

        updateProgressBar()

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(fragmentClass.simpleName)
            .commit()
    }

    private fun updateProgressBar() {
        val backStackCount = supportFragmentManager.backStackEntryCount
        val currentFragment = supportFragmentManager.fragments.lastOrNull()

        if (currentFragment is FinishFragment) {
            currentFragmentIndex = fragmentOrder.size
        } else {
            currentFragmentIndex = (backStackCount - 1).coerceAtLeast(0).coerceAtMost(fragmentOrder.size - 1)
        }

        ObjectAnimator.ofInt(binding.progressBar, "progress", binding.progressBar.progress, currentFragmentIndex)
            .setDuration(300)
            .start()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (supportFragmentManager.backStackEntryCount > 1) {
            supportFragmentManager.popBackStack()
        } else {
            finish()
        }
    }
}