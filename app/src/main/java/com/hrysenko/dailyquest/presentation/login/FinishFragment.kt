package com.hrysenko.dailyquest.presentation.login

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.hrysenko.dailyquest.R
import com.hrysenko.dailyquest.databinding.FragmentFinishBinding
import com.hrysenko.dailyquest.models.AppDatabase
import com.hrysenko.dailyquest.presentation.main.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FinishFragment : Fragment() {

    private var _binding: FragmentFinishBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LoginViewModel by activityViewModels()
    private val maxNameLength = 12

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFinishBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("StringFormatInvalid")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.root.alpha = 0f
        binding.root.animate()
            .alpha(1f)
            .setDuration(300)
            .start()

        binding.backButton.setOnClickListener {
            vibrateDevice()
            requireActivity().supportFragmentManager.popBackStack()
        }

        binding.finishButton.setOnClickListener {
            vibrateDevice()
            val name = binding.loginName.text.toString().trim()

            if (name.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.enter_your_name),
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (name.length > maxNameLength) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.name_too_long, maxNameLength),
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            viewModel.name = name

            if (viewModel.isDataComplete()) {
                lifecycleScope.launch(Dispatchers.IO) {
                    val database = AppDatabase.getDatabase(requireContext())
                    database.userDao().insertUser(viewModel.toUser())

                    requireActivity().getSharedPreferences("dailyquest_prefs", Context.MODE_PRIVATE)
                        .edit {
                            putBoolean("is_registered", true)
                        }

                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.data_saved),
                            Toast.LENGTH_SHORT
                        ).show()
                        startActivity(Intent(requireContext(), MainActivity::class.java))
                        requireActivity().finish()
                    }
                }
            } else {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.Fill_in_all_fields),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun vibrateDevice() {
        val vibrator = requireContext().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val effect = VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
                vibrator.vibrate(effect)
            } else {
                vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}