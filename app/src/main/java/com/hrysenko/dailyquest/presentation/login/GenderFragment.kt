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
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.color.MaterialColors
import com.hrysenko.dailyquest.R
import com.hrysenko.dailyquest.databinding.FragmentGenderBinding

class GenderFragment : Fragment() {

    private var _binding: FragmentGenderBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LoginViewModel by activityViewModels()
    private var selectedGender: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGenderBinding.inflate(inflater, container, false)
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

        binding.maleCard.setOnClickListener {
            vibrateDevice()
            selectedGender = getString(R.string.male)
            updateCardSelection(binding.maleCard, binding.femaleCard)
        }

        binding.femaleCard.setOnClickListener {
            vibrateDevice()
            selectedGender = getString(R.string.female)
            updateCardSelection(binding.femaleCard, binding.maleCard)
        }

        binding.nextButton.setOnClickListener {
            if (selectedGender == null) {
                Toast.makeText(requireContext(), getString(R.string.choose_sex), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            viewModel.sex = selectedGender!!
            (activity as? LoginActivity)?.showFragment(AgeFragment())
        }
    }

    private fun updateCardSelection(selectedCard: androidx.cardview.widget.CardView, unselectedCard: androidx.cardview.widget.CardView) {
        val selectedColor = MaterialColors.getColor(requireContext(), com.google.android.material.R.attr.colorSecondaryContainer, ContextCompat.getColor(requireContext(), android.R.color.white))
        val unselectedColor = MaterialColors.getColor(requireContext(), com.google.android.material.R.attr.colorSurface, ContextCompat.getColor(requireContext(), android.R.color.white))

        selectedCard.setCardBackgroundColor(selectedColor)
        unselectedCard.setCardBackgroundColor(unselectedColor)

        selectedCard.animate()
            .scaleX(1.05f)
            .scaleY(1.05f)
            .setDuration(100)
            .withEndAction {
                selectedCard.animate()
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setDuration(100)
                    .start()
            }
            .start()
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