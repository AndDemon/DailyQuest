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
                            // Log error or show a toast
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

        var notificationPermissionWasRequested = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestNotificationPermission()
                notificationPermissionWasRequested = true
            }
        }

        if (!notificationPermissionWasRequested) {
            requestActivityRecognitionPermissionIfNeeded()
        }

        database = AppDatabase.getDatabase(this)
        appDatabase = database

        // It's good practice to initialize WebView when it's actually needed,
        // for example, when navigating to the AssistantFragment.
        // Pre-initializing it here might not be necessary unless it's used immediately.
        // getSharedWebView(this) // Consider moving this to where it's first used.

        setupBottomNavigation()

        if (savedInstanceState == null) {
            loadFragment(MainMenuFragment())
            binding.bottomNavigation.selectedItemId = R.id.main
        }

        if (checkActivityRecognitionPermission()) {
            startPedometerService()
        }
    }

    private fun requestNotificationPermission() {
        // This function is defined below, no need to re-check SDK and permission status here
        // if the function itself does it.
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.POST_NOTIFICATIONS),
            REQUEST_NOTIFICATION_PERMISSION
        )
    }

    private fun requestActivityRecognitionPermissionIfNeeded() {
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

    private fun startPedometerService() {
        try {
            startService(Intent(this, PedometerService::class.java))
        } catch (e: IllegalStateException) {
            // This can happen if trying to start a foreground service when the app is not in a valid state
            // (e.g., background on Android 12+ without appropriate exemptions).
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
        when (requestCode) {
            REQUEST_NOTIFICATION_PERMISSION -> {
                // After notification permission result, request activity recognition if needed
                requestActivityRecognitionPermissionIfNeeded()
                // No need to explicitly check if granted here for starting service,
                // that will be handled when REQUEST_ACTIVITY_RECOGNITION_PERMISSION result comes
                // or if it was already granted.
            }
            REQUEST_ACTIVITY_RECOGNITION_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startPedometerService()
                } else {
                    Toast.makeText(this, "Activity recognition permission denied. Step counting will not work.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}