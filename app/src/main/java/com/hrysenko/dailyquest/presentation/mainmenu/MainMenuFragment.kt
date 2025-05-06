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

class MainMenuFragment : Fragment() {
    private var _binding: FragmentMainMenuBinding? = null
    private val binding get() = _binding!!
    private var callback: OnButtonClickListener? = null
    private var bmiDialog: androidx.appcompat.app.AlertDialog? = null
    private lateinit var stepsReceiver: BroadcastReceiver

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
        if (checkActivityRecognitionPermission()) {
            loadSteps()
            setupStepsReceiver()
        } else {
            binding.stepsCount.text = "N/A"
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
                binding.stepsCount.text = steps.toString()
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
                                "Некоректні дані користувача",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        binding.streakCount.text = "0"
                        Toast.makeText(
                            requireContext(),
                            "Дані користувача відсутні",
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
                        "Помилка завантаження даних",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun loadSteps() {
        binding.stepsCount.text = PedometerService.getCurrentSteps().toString()
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

    override fun onDestroyView() {
        bmiDialog?.dismiss()
        bmiDialog = null
        if (::stepsReceiver.isInitialized) {
            requireContext().unregisterReceiver(stepsReceiver)
        }
        super.onDestroyView()
        _binding = null
    }
}