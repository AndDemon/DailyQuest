package com.hrysenko.dailyquest.presentation.login

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.slider.Slider
import com.hrysenko.dailyquest.R
import com.hrysenko.dailyquest.databinding.FragmentAgeBinding

class AgeFragment : Fragment() {

    private var _binding: FragmentAgeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LoginViewModel by activityViewModels()
    private var lastVibratedAge: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAgeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.root.alpha = 0f
        binding.root.animate()
            .alpha(1f)
            .setDuration(300)
            .start()

        binding.backButton.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        binding.ageSlider.addOnChangeListener { _, value, fromUser ->
            val age = value.toInt()
            if (fromUser && lastVibratedAge != age) {
                vibrateDevice()
                lastVibratedAge = age
            }
            binding.ageValue.text = getString(R.string.n_years, age)
            viewModel.age = age
            binding.ageValue.animate()
                .scaleX(1.1f)
                .scaleY(1.1f)
                .setDuration(100)
                .withEndAction {
                    binding.ageValue.animate()
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .setDuration(100)
                        .start()
                }
                .start()
        }

        binding.ageSlider.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {
                lastVibratedAge = null
            }

            override fun onStopTrackingTouch(slider: Slider) {}
        })

        binding.nextButton.setOnClickListener {
            val age = binding.ageSlider.value.toInt()
            if (age < 10 || age > 100) {
                Toast.makeText(requireContext(),
                    getString(R.string.enter_correct_age), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            viewModel.age = age
            (activity as? LoginActivity)?.showFragment(WeightFragment())
        }
    }

    private fun vibrateDevice() {
        val vibrator = requireContext().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (vibrator.hasVibrator()) {
            vibrator.vibrate(VibrationEffect.createOneShot(20, 5))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}