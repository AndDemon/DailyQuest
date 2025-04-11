package com.hrysenko.dailyquest.presentation.login

import android.os.Bundle
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


        binding.maleCard.setOnClickListener {
            selectedGender = "Чоловік"
            updateCardSelection(binding.maleCard, binding.femaleCard)
        }


        binding.femaleCard.setOnClickListener {
            selectedGender = "Жінка"
            updateCardSelection(binding.femaleCard, binding.maleCard)
        }

        // Обробка кнопки "Далі"
        binding.nextButton.setOnClickListener {
            if (selectedGender == null) {
                Toast.makeText(requireContext(), "Виберіть стать", Toast.LENGTH_SHORT).show()
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}