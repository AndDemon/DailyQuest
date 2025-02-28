package com.hrysenko.dailyquest.presentation.profile

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.hrysenko.dailyquest.databinding.FragmentProfileBinding
import com.hrysenko.dailyquest.models.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var database: AppDatabase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        database = Room.databaseBuilder(
            requireContext(),
            AppDatabase::class.java, "dailyquest_db"
        ).build()

        loadUserData()
    }

    private fun loadUserData() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val user = database.userDao().getUser()
                Log.d("ProfileFragment", "User from DB: $user")

                withContext(Dispatchers.Main) {
                    if (user != null) {
                        binding.userName.text = "Ім'я: ${user.name}"
                        binding.userAge.text = "Вік: ${user.age}"
                        binding.userHeight.text = "Зріст: ${user.height} см"
                        binding.userWeight.text = "Вага: ${user.weight} кг"
                        binding.userSex.text = "Стать: ${user.sex}"
                        binding.userGoal.text = "Мета: ${user.goal}"
                        binding.userPhysLevel.text = "Рівень активності: ${user.physLevel}"
                        binding.userAvatar.text = "Аватар: ${user.avatar ?: "Немає"}"
                    } else {
                        Toast.makeText(requireContext(), "Дані користувача відсутні", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("ProfileFragment", "Error loading user data: ${e.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Помилка завантаження даних", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}