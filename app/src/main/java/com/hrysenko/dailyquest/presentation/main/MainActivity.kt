package com.hrysenko.dailyquest.presentation.main

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
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
                    webViewClient = object : WebViewClient() {
                        override fun onReceivedError(
                            view: WebView?,
                            errorCode: Int,
                            description: String?,
                            failingUrl: String?
                        ) {
                            super.onReceivedError(view, errorCode, description, failingUrl)
                            Log.e("WebViewError", "Error $errorCode: $description for $failingUrl")
                        }
                    }
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

        database = AppDatabase.getDatabase(this)
        appDatabase = database

        checkAndRequestPermissions()
        setupBottomNavigation()

        if (savedInstanceState == null) {
            loadFragment(MainMenuFragment())
            binding.bottomNavigation.selectedItemId = R.id.main
        }
    }

    private fun checkAndRequestPermissions() {
        val permissionsToRequest = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACTIVITY_RECOGNITION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsToRequest.add(Manifest.permission.ACTIVITY_RECOGNITION)
            }
        }

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toTypedArray(),
                REQUEST_NOTIFICATION_PERMISSION
            )
        } else {
            startPedometerService()
        }
    }

    private fun startPedometerService() {
        try {
            val intent = Intent(this, PedometerService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
        } catch (e: Exception) {
            Log.e("PedometerService", "Failed to start PedometerService: ${e.message}")
            Toast.makeText(this, "Could not start step counter service.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            val currentFragment = supportFragmentManager.findFragmentById(R.id.main_fragments)
            when (item.itemId) {
                R.id.main -> {
                    if (currentFragment !is MainMenuFragment) loadFragment(MainMenuFragment())
                    true
                }
                R.id.assistant -> {
                    if (currentFragment !is AssistantFragment) loadFragment(AssistantFragment())
                    true
                }
                R.id.dailyQuest -> {
                    if (currentFragment !is QuestsFragment) loadFragment(QuestsFragment())
                    true
                }
                R.id.profile -> {
                    if (currentFragment !is ProfileFragment) loadFragment(ProfileFragment())
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
        sharedWebView?.let {
            (it.parent as? ViewGroup)?.removeView(it)
            it.destroy()
        }
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
        if (requestCode == REQUEST_NOTIFICATION_PERMISSION) {
            val activityRecognitionGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACTIVITY_RECOGNITION
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }

            if (activityRecognitionGranted) {
                startPedometerService()
            } else {
                Toast.makeText(
                    this,
                    "Activity recognition permission denied. Step counting will not work.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}