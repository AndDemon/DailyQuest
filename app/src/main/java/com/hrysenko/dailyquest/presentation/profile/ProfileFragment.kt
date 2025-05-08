package com.hrysenko.dailyquest.presentation.profile

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.hrysenko.dailyquest.R
import com.hrysenko.dailyquest.databinding.FragmentProfileBinding
import com.hrysenko.dailyquest.models.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var database: AppDatabase
    private val CHANNEL_ID = "dailyquest_channel"
    private val NOTIFICATION_ID = 1

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            val sharedPreferences = requireContext().getSharedPreferences("DailyQuestPrefs", Context.MODE_PRIVATE)
            sharedPreferences.edit { putBoolean("notifications_enabled", true) }
            binding.switchMaterial.isChecked = true

        } else {
            Toast.makeText(requireContext(), getString(R.string.no_allow), Toast.LENGTH_SHORT).show()
            binding.switchMaterial.isChecked = false
        }
    }

    private val editProfileLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            loadUserData()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        database = Room.databaseBuilder(
            requireContext(),
            AppDatabase::class.java, "dailyquest_db"
        ).build()

        createNotificationChannel()

        loadUserData()

        val sharedPreferences = requireContext().getSharedPreferences("DailyQuestPrefs", Context.MODE_PRIVATE)

        val isNotificationsEnabled = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ActivityCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (hasPermission) {
                sharedPreferences.edit { putBoolean("notifications_enabled", true) }
                true
            } else {
                sharedPreferences.getBoolean("notifications_enabled", false)
            }
        } else {
            sharedPreferences.getBoolean("notifications_enabled", true)
        }

        binding.switchMaterial.isChecked = isNotificationsEnabled

        binding.materialCardView2.setOnClickListener {
            binding.switchMaterial.isChecked = !binding.switchMaterial.isChecked
        }

        binding.switchMaterial.setOnCheckedChangeListener { _: CompoundButton, isChecked: Boolean ->
            if (isChecked && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ActivityCompat.checkSelfPermission(
                        requireContext(),
                        android.Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                    return@setOnCheckedChangeListener
                }
            }
            sharedPreferences.edit { putBoolean("notifications_enabled", isChecked) }
            if (isChecked) {
                Toast.makeText(requireContext(), getString(R.string.notif_on), Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), getString(R.string.notif_off), Toast.LENGTH_SHORT).show()
            }
        }

        binding.editButton.setOnClickListener {
            val intent = Intent(requireContext(), EditProfileActivity::class.java)
            editProfileLauncher.launch(intent)
        }

        binding.cardSupport.setOnClickListener {
            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:support@dailyquest.com")
                putExtra(Intent.EXTRA_SUBJECT, "Support Request - DailyQuest")
                putExtra(Intent.EXTRA_TEXT, "Please describe your issue or question:")
            }
            try {
                startActivity(Intent.createChooser(emailIntent, "Send email"))
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "No email app found", Toast.LENGTH_SHORT).show()
                Log.e("ProfileFragment", "Error launching email intent: ${e.message}")
            }
        }

        binding.cardAbout.setOnClickListener {
            val url = "https://github.com/AndDemon/DailyQuest"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            try {
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Unable to open link", Toast.LENGTH_SHORT).show()
                Log.e("ProfileFragment", "Error opening GitHub link: ${e.message}")
            }
        }
    }

    private fun loadUserData() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val user = database.userDao().getUser()
                Log.d("ProfileFragment", "User from DB: $user")

                withContext(Dispatchers.Main) {
                    if (user != null) {
                        binding.userNameText.text = user.name
                        binding.userGoalText.text = user.goal
                        binding.textAge.text = user.age.toString()
                        binding.textHeight.text = formatDimension(user.height, R.string.cm)
                        binding.textWeight.text = formatDimension(user.weight, R.string.kg)
                    }
                }
            } catch (e: Exception) {
                Log.e("ProfileFragment", "Error loading user data: ${e.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Error", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun formatDimension(value: Double?, unitResId: Int): String {
        if (value == null) return ""
        return if (value % 1.0 == 0.0) {
            "${value.toInt()} ${getString(unitResId)}"
        } else {
            "$value ${getString(unitResId)}"
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.app_name)
            val descriptionText = "Channel for DailyQuest notifications"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}