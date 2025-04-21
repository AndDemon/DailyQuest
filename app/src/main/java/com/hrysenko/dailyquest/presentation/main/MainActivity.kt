package com.hrysenko.dailyquest.presentation.main

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.hrysenko.dailyquest.R
import com.hrysenko.dailyquest.databinding.ActivityMainBinding
import com.hrysenko.dailyquest.databinding.FragmentProfileBinding
import com.hrysenko.dailyquest.models.AppDatabase
import com.hrysenko.dailyquest.presentation.assistant.AssistantFragment
import com.hrysenko.dailyquest.presentation.login.LoginActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var database: AppDatabase
    private lateinit var preferences: SharedPreferences

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

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = AppDatabase.getDatabase(this)
        appDatabase = database
        preferences = getSharedPreferences("dailyquest_prefs", MODE_PRIVATE)


        getSharedWebView(this)

        setupBottomNavigation()

        if (savedInstanceState == null) {
            loadFragment(HomeFragment())
            binding.bottomNavigation.selectedItemId = R.id.main
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.main -> {
                    loadFragment(HomeFragment())
                    true
                }
                R.id.assistant -> {
                    loadFragment(AssistantFragment())
                    true
                }
                R.id.dailyQuest -> {
                    loadFragment(DailyQuestFragment())
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
}

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadUserData()
    }

    private fun loadUserData() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val user = MainActivity.appDatabase.userDao().getUser()
                withContext(Dispatchers.Main) {
                    if (user != null) {
                        binding.userName.text = "Ім'я: ${user.name}"
                        binding.userAge.text = "Вік: ${user.age}"
                        binding.userHeight.text = "Зріст: ${user.height} см"
                        binding.userWeight.text = "Вага: ${user.weight} кг"
                        binding.userSex.text = "Стать: ${user.sex}"
                        binding.userGoal.text = "Мета: ${user.goal}"
                        binding.userPhysLevel.text = "Рівень активності: ${user.physLevel}"
                        binding.userAvatar.text = "Аватар: ${user.avatar ?: "Немає"}"
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {

                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun AppCompatActivity.toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}

class HomeFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_main_menu, container, false)
    }
}

class DailyQuestFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_quests, container, false)
    }
}