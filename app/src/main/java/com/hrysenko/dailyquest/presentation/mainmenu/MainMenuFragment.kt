package com.hrysenko.dailyquest.presentation.mainmenu

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.hrysenko.dailyquest.R
import com.hrysenko.dailyquest.databinding.FragmentMainMenuBinding
import com.hrysenko.dailyquest.presentation.main.MainActivity
import com.hrysenko.dailyquest.services.PedometerService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class MainMenuFragment : Fragment() {
    private var _binding: FragmentMainMenuBinding? = null
    private val binding get() = _binding!!
    private var callback: OnButtonClickListener? = null
    private var bmiDialog: androidx.appcompat.app.AlertDialog? = null
    private lateinit var stepsReceiver: BroadcastReceiver
    private var isStepsReceiverRegistered = false

    interface OnButtonClickListener {
        fun onCheckButtonClick()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callback = try {
            context as OnButtonClickListener
        } catch (e: ClassCastException) {
            throw ClassCastException("$context must implement OnButtonClickListener")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewMore.setOnClickListener { showBMIDialog() }
        binding.dqCheck.setOnClickListener {
            bmiDialog?.dismiss()
            callback?.onCheckButtonClick()
        }

        loadUserData()
        updateStepsAndCaloriesUI()
        loadAndDisplayTips()
    }

    override fun onResume() {
        super.onResume()
        updateStepsAndCaloriesUI()
        loadAndDisplayTips()
    }

    override fun onPause() {
        super.onPause()
        if (isStepsReceiverRegistered) {
            requireContext().unregisterReceiver(stepsReceiver)
            isStepsReceiverRegistered = false
        }
    }

    private fun updateStepsAndCaloriesUI() {
        if (checkActivityRecognitionPermission()) {
            loadStepsAndCalories()
            loadQuestProgress()
            if (!isStepsReceiverRegistered) {
                setupStepsReceiver()
                isStepsReceiverRegistered = true
            }
        } else {
            binding.stepsCount.text = "N/A"
            binding.caloriesCount.text = "N/A"
            binding.progressCount.text = "0%"
            binding.dailyProgressBar.progress = 0
            Toast.makeText(
                requireContext(),
                getString(R.string.activity_recognition_permission_message),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun checkActivityRecognitionPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACTIVITY_RECOGNITION
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    private fun setupStepsReceiver() {
        stepsReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val steps = intent?.getIntExtra(PedometerService.EXTRA_STEPS, 0) ?: 0
                val calories = intent?.getDoubleExtra(PedometerService.EXTRA_CALORIES, 0.0) ?: 0.0
                binding.stepsCount.text = steps.toString()
                binding.caloriesCount.text = String.format("%.0f", calories)
                loadQuestProgress()
            }
        }
        val filter = IntentFilter(PedometerService.STEP_UPDATE_ACTION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requireContext().registerReceiver(stepsReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            ContextCompat.registerReceiver(
                requireContext(),
                stepsReceiver,
                filter,
                ContextCompat.RECEIVER_NOT_EXPORTED
            )
        }
    }

    private fun loadUserData() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val user = MainActivity.appDatabase.userDao().getUser()
                val preferences = requireContext().getSharedPreferences("DailyQuestPrefs", Context.MODE_PRIVATE)
                val streak = preferences.getInt("streak", 0)
                Log.d("MainMenuFragment", "User from DB: $user, Streak: $streak")
                withContext(Dispatchers.Main) {
                    if (user != null) {
                        binding.userNameText.text = user.name
                        binding.streakCount.text = streak.toString()

                        val height = user.height
                        val weight = user.weight

                        if (height != null && weight != null) {
                            val heightInMeters = height / 100.0
                            val bmi = weight / (heightInMeters * heightInMeters)
                            binding.bmiNum.text = String.format("%.1f", bmi)
                            binding.bmiWeight.text = getBMICategory(bmi)
                        } else {
                            Toast.makeText(
                                requireContext(),
                                getString(R.string.invalid_user_data_message),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        binding.streakCount.text = "0"
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.no_user_data_message),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("MainMenuFragment", "Error loading user data: ${e.message}")
                withContext(Dispatchers.Main) {
                    binding.streakCount.text = "0"
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.error_loading_data_message),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun loadStepsAndCalories() {
        binding.stepsCount.text = PedometerService.getCurrentSteps().toString()
        binding.caloriesCount.text = String.format("%.0f", PedometerService.getCurrentCalories())
    }

    private fun loadQuestProgress() {
        val preferences = requireContext().getSharedPreferences("DailyQuestPrefs", Context.MODE_PRIVATE)
        val progressPercentage = preferences.getInt("quest_progress_percentage", 0)
        binding.progressCount.text = "$progressPercentage%"
        binding.dailyProgressBar.progress = progressPercentage
    }

    private fun getBMICategory(bmi: Double): String = when {
        bmi < 18.5 -> getString(R.string.you_are_underweight)
        bmi in 18.5..24.9 -> getString(R.string.you_have_a_normal_weight)
        bmi in 25.0..29.9 -> getString(R.string.you_are_overweight)
        else -> getString(R.string.you_are_obese)
    }

    private fun showBMIDialog() {
        val bmiRecommendation = getBMIRecommendation()

        bmiDialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.bmi_recommendations)
            .setMessage(bmiRecommendation)
            .setPositiveButton(android.R.string.ok) { dialog, _ -> dialog.dismiss() }
            .setCancelable(true)
            .create()

        bmiDialog?.show()
    }

    private fun getBMIRecommendation(): String {
        val bmi = binding.bmiNum.text.toString().replace(",", ".").toDoubleOrNull() ?: 0.0
        return when {
            bmi < 18.5 -> String.format(
                getString(R.string.bmi_info),
                bmi,
                getString(R.string.bmi_underweight_recommendation)
            )
            bmi in 18.5..24.9 -> String.format(
                getString(R.string.bmi_info),
                bmi,
                getString(R.string.bmi_normal_recommendation)
            )
            bmi in 25.0..29.9 -> String.format(
                getString(R.string.bmi_info),
                bmi,
                getString(R.string.bmi_overweight_recommendation)
            )
            else -> String.format(
                getString(R.string.bmi_info),
                bmi,
                getString(R.string.bmi_obese_recommendation)
            )
        }
    }

    private fun loadAndDisplayTips() {
        lifecycleScope.launch(Dispatchers.IO) {
            val preferences = requireContext().getSharedPreferences("DailyQuestPrefs", Context.MODE_PRIVATE)
            val lastTipUpdate = preferences.getLong("last_tip_update", 0L)
            val currentTime = Calendar.getInstance().timeInMillis
            val oneDayInMillis = 24 * 60 * 60 * 1000L // 24 hours

            // Check if 24 hours have passed since the last tip update
            val tipIndex = if (currentTime - lastTipUpdate > oneDayInMillis) {
                val newIndex = preferences.getInt("tip_index", 0) + 1
                preferences.edit().putInt("tip_index", newIndex % 3).apply()
                preferences.edit().putLong("last_tip_update", currentTime).apply()
                newIndex % 3
            } else {
                preferences.getInt("tip_index", 0)
            }

            withContext(Dispatchers.Main) {
                displayTips(tipIndex)
            }
        }
    }

    private fun displayTips(tipIndex: Int) {
        // Tips lists
        val nutritionTips = listOf(
            getString(R.string.tip_eat_vegetables_protein),
            getString(R.string.tip_balanced_diet),
            getString(R.string.tip_avoid_sugary_drinks)
        )
        val sleepTips = listOf(
            getString(R.string.tip_sleep_7_8_hours),
            getString(R.string.tip_create_bedtime_routine),
            getString(R.string.tip_avoid_screens_before_bed)
        )
        val hydrationTips = listOf(
            getString(R.string.tip_drink_2_liters_water),
            getString(R.string.tip_carry_water_bottle),
            getString(R.string.tip_add_lemon_mint_water)
        )
        val activityTips = listOf(
            getString(R.string.tip_walk_30_minutes),
            getString(R.string.tip_strength_training_3_times),
            getString(R.string.tip_stretch_10_minutes)
        )

        // Update UI with tips
        binding.tipNutrition.text = nutritionTips[tipIndex]
        binding.tipSleep.text = sleepTips[tipIndex]
        binding.tipHydration.text = hydrationTips[tipIndex]
        binding.tipActivity.text = activityTips[tipIndex]
    }

    override fun onDestroyView() {
        bmiDialog?.dismiss()
        bmiDialog = null
        if (isStepsReceiverRegistered) {
            requireContext().unregisterReceiver(stepsReceiver)
            isStepsReceiverRegistered = false
        }
        super.onDestroyView()
        _binding = null
    }
}