package com.hrysenko.dailyquest.presentation.profile

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
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
        val isNotificationsEnabled = sharedPreferences.getBoolean("notifications_enabled", false)
        binding.switchMaterial.isChecked = isNotificationsEnabled


        binding.switchMaterial.setOnCheckedChangeListener { _: CompoundButton, isChecked: Boolean ->

            sharedPreferences.edit().putBoolean("notifications_enabled", isChecked).apply()

            if (isChecked) {

                sendTestNotification()
                Toast.makeText(requireContext(), getString(R.string.notif_on), Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), getString(R.string.notif_off), Toast.LENGTH_SHORT).show()
            }
        }


        binding.editButton.setOnClickListener {
            Toast.makeText(requireContext(), "Edit button clicked", Toast.LENGTH_SHORT).show()
        }

        binding.supportBtn.setOnClickListener {
            Toast.makeText(requireContext(), "Support button clicked", Toast.LENGTH_SHORT).show()
        }

        binding.aboutBtn.setOnClickListener {
            Toast.makeText(requireContext(), "About button clicked", Toast.LENGTH_SHORT).show()
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
                    } else {
                        Toast.makeText(requireContext(), "Дані користувача відсутні", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("ProfileFragment", "Error loading user data: ${e.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Помилка завантаження даних", Toast.LENGTH_SHORT).show()
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

    //test notification
    private fun sendTestNotification() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Toast.makeText(requireContext(), "Дозвіл на сповіщення не надано", Toast.LENGTH_SHORT).show()
                return
            }
        }

        val builder = NotificationCompat.Builder(requireContext(), CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(getString(R.string.app_name))
            .setContentText("Це тестове сповіщення від DailyQuest!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(requireContext())) {
            notify(NOTIFICATION_ID, builder.build())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}