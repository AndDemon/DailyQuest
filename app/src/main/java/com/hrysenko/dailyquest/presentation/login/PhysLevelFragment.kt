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
import com.hrysenko.dailyquest.databinding.FragmentPhysLevelBinding

class PhysLevelFragment : Fragment() {

    private var _binding: FragmentPhysLevelBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LoginViewModel by activityViewModels()
    private var selectedPhysLevel: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPhysLevelBinding.inflate(inflater, container, false)
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
            vibrateDevice()
            requireActivity().supportFragmentManager.popBackStack()
        }

        binding.sedentaryCard.setOnClickListener {
            vibrateDevice()
            selectedPhysLevel = getString(R.string.sedentary)
            updateCardSelection(
                binding.sedentaryCard,
                listOf(binding.lightCard, binding.moderateCard, binding.activeCard)
            )
        }

        binding.lightCard.setOnClickListener {
            vibrateDevice()
            selectedPhysLevel = getString(R.string.light)
            updateCardSelection(
                binding.lightCard,
                listOf(binding.sedentaryCard, binding.moderateCard, binding.activeCard)
            )
        }

        binding.moderateCard.setOnClickListener {
            vibrateDevice()
            selectedPhysLevel = getString(R.string.moderate)
            updateCardSelection(
                binding.moderateCard,
                listOf(binding.sedentaryCard, binding.lightCard, binding.activeCard)
            )
        }

        binding.activeCard.setOnClickListener {
            vibrateDevice()
            selectedPhysLevel = getString(R.string.active)
            updateCardSelection(
                binding.activeCard,
                listOf(binding.sedentaryCard, binding.lightCard, binding.moderateCard)
            )
        }

        binding.nextButton.setOnClickListener {
            vibrateDevice()
            if (selectedPhysLevel == null) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.choose_phys_level),
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            viewModel.physLevel = selectedPhysLevel!!
            (activity as? LoginActivity)?.showFragment(FinishFragment())
        }
    }

    private fun updateCardSelection(
        selectedCard: androidx.cardview.widget.CardView,
        unselectedCards: List<androidx.cardview.widget.CardView>
    ) {
        val selectedColor = MaterialColors.getColor(
            requireContext(),
            com.google.android.material.R.attr.colorSecondaryContainer,
            ContextCompat.getColor(requireContext(), android.R.color.white)
        )
        val unselectedColor = MaterialColors.getColor(
            requireContext(),
            com.google.android.material.R.attr.colorSurface,
            ContextCompat.getColor(requireContext(), android.R.color.white)
        )

        selectedCard.setCardBackgroundColor(selectedColor)
        unselectedCards.forEach { it.setCardBackgroundColor(unselectedColor) }

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