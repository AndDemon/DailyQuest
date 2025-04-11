package com.hrysenko.dailyquest.presentation.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.hrysenko.dailyquest.databinding.FragmentAgeBinding

class AgeFragment : Fragment() {

    private var _binding: FragmentAgeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LoginViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAgeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Анімація появи
        binding.root.alpha = 0f
        binding.root.animate()
            .alpha(1f)
            .setDuration(300)
            .start()

        // Налаштування SeekBar
        binding.ageSeekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // Додаємо 10 до значення прогресу, щоб діапазон був від 10 до 100
                val age = progress + 10
                binding.ageValue.text = "$age років"
                viewModel.age = age
                // Анімація для TextView
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

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Обробка кнопки "Далі"
        binding.nextButton.setOnClickListener {
            val age = binding.ageSeekbar.progress + 10
            if (age < 10 || age > 100) {
                Toast.makeText(requireContext(), "Введіть коректний вік (10-100)", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            viewModel.age = age
            (activity as? LoginActivity)?.showFragment(WeightFragment())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}