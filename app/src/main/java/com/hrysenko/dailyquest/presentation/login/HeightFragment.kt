package com.hrysenko.dailyquest.presentation.login

import android.annotation.SuppressLint
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
import com.hrysenko.dailyquest.databinding.FragmentHeightBinding

class HeightFragment : Fragment() {

    private var _binding: FragmentHeightBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LoginViewModel by activityViewModels()
    private var lastVibratedHeight: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHeightBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("StringFormatMatches")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.root.alpha = 0f
        binding.root.animate()
            .alpha(1f)
            .setDuration(300)
            .start()

        binding.heightSeekbar.addOnChangeListener { slider, value, fromUser ->
            if (fromUser && lastVibratedHeight != value.toInt()) {
                vibrateDevice()
                lastVibratedHeight = value.toInt()
            }
            binding.heightValue.text = getString(R.string.n_cm, value.toInt())
            viewModel.height = value.toDouble()
            binding.heightValue.animate()
                .scaleX(1.1f)
                .scaleY(1.1f)
                .setDuration(100)
                .withEndAction {
                    binding.heightValue.animate()
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .setDuration(100)
                        .start()
                }
                .start()
        }

        binding.heightSeekbar.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {
                lastVibratedHeight = null
            }

            override fun onStopTrackingTouch(slider: Slider) {}
        })

        binding.backButton.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        binding.nextButton.setOnClickListener {
            val height = binding.heightSeekbar.value.toInt()
            if (height < 100 || height > 250) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.enter_height),
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            viewModel.height = height.toDouble()
            (activity as? LoginActivity)?.showFragment(GoalFragment())
        }
    }

    private fun vibrateDevice() {
        val vibrator = requireContext().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val effect = VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK)
                vibrator.vibrate(effect)
            } else {
                vibrator.vibrate(VibrationEffect.createOneShot(20, 5))
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}