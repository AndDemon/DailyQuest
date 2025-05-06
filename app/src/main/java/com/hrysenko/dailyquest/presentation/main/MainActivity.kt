package com.hrysenko.dailyquest.presentation.main

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.hrysenko.dailyquest.R
import com.hrysenko.dailyquest.databinding.ActivityMainBinding
import com.hrysenko.dailyquest.models.AppDatabase
import com.hrysenko.dailyquest.presentation.assistant.AssistantFragment
import com.hrysenko.dailyquest.presentation.mainmenu.MainMenuFragment
import com.hrysenko.dailyquest.presentation.profile.ProfileFragment
import com.hrysenko.dailyquest.presentation.quests.QuestsFragment
import com.hrysenko.dailyquest.services.PedometerService

class MainActivity : AppCompatActivity(), MainMenuFragment.OnButtonClickListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var database: AppDatabase

    companion object {
        lateinit var appDatabase: AppDatabase
            private set

        @SuppressLint("StaticFieldLeak")
        private var sharedWebView: WebView? = null

        @SuppressLint("SetJavaScriptEnabled")
        fun getSharedWebView(context: MainActivity): WebView {
            if (sharedWebView == null) {
                sharedWebView = WebView(context).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
                    webViewClient = WebViewClient()
                    loadUrl("https://grok.com/")
                }
            }
            return sharedWebView!!
        }

        private const val REQUEST_NOTIFICATION_PERMISSION = 1001
        private const val REQUEST_ACTIVITY_RECOGNITION_PERMISSION = 1002
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestNotificationPermission()
        requestActivityRecognitionPermission()

        database = AppDatabase.getDatabase(this)
        appDatabase = database

        getSharedWebView(this)

        setupBottomNavigation()

        if (savedInstanceState == null) {
            loadFragment(MainMenuFragment())
            binding.bottomNavigation.selectedItemId = R.id.main
        }


        if (checkActivityRecognitionPermission()) {
            startService(Intent(this, PedometerService::class.java))
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    REQUEST_NOTIFICATION_PERMISSION
                )
            }
        }
    }

    private fun requestActivityRecognitionPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACTIVITY_RECOGNITION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
                    REQUEST_ACTIVITY_RECOGNITION_PERMISSION
                )
            }
        }
    }

    private fun checkActivityRecognitionPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACTIVITY_RECOGNITION
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.main -> {
                    loadFragment(MainMenuFragment())
                    true
                }
                R.id.assistant -> {
                    loadFragment(AssistantFragment())
                    true
                }
                R.id.dailyQuest -> {
                    loadFragment(QuestsFragment())
                    true
                }
                R.id.profile -> {
                    loadFragment(ProfileFragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.main_fragments, fragment)
            .commit()
    }

    override fun onDestroy() {
        super.onDestroy()
        sharedWebView?.destroy()
        sharedWebView = null
    }

    override fun onCheckButtonClick() {
        loadFragment(QuestsFragment())
        binding.bottomNavigation.selectedItemId = R.id.dailyQuest
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_NOTIFICATION_PERMISSION -> {

            }
            REQUEST_ACTIVITY_RECOGNITION_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startService(Intent(this, PedometerService::class.java))
                } else {

                }
            }
        }
    }
}