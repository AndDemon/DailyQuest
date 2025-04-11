package com.hrysenko.dailyquest.presentation.login

import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFinishBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.finishButton.setOnClickListener {
            val name = binding.loginName.text.toString().trim()
            if (name.isEmpty()) {
                Toast.makeText(requireContext(), "Введіть ваше ім'я", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            viewModel.name = name

            if (viewModel.isDataComplete()) {
                lifecycleScope.launch(Dispatchers.IO) {
                    val database = AppDatabase.getDatabase(requireContext())
                    database.userDao().insertUser(viewModel.toUser())

                    requireActivity().getSharedPreferences("dailyquest_prefs", MODE_PRIVATE)
                        .edit()
                        .putBoolean("is_registered", true)
                        .apply()

                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Дані збережено!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(requireContext(), MainActivity::class.java))
                        requireActivity().finish()
                    }
                }
            } else {
                Toast.makeText(requireContext(), "Заповніть усі поля", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}