package com.hrysenko.dailyquest.presentation.main

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.hrysenko.dailyquest.R
import com.hrysenko.dailyquest.databinding.ActivityMainBinding
import com.hrysenko.dailyquest.models.AppDatabase
import com.hrysenko.dailyquest.presentation.assistant.AssistantFragment
import com.hrysenko.dailyquest.presentation.mainmenu.MainMenuFragment
import com.hrysenko.dailyquest.presentation.profile.ProfileFragment
import com.hrysenko.dailyquest.presentation.quests.QuestsFragment

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
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = AppDatabase.getDatabase(this)
        appDatabase = database

        getSharedWebView(this)

        setupBottomNavigation()

        if (savedInstanceState == null) {
            loadFragment(MainMenuFragment())
            binding.bottomNavigation.selectedItemId = R.id.main
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
}