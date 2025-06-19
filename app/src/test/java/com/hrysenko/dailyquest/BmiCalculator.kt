package com.hrysenko.dailyquest

object BmiCalculator {
    fun calculateBmi(weightKg: Double, heightCm: Double): Double {
        if (weightKg <= 0 || heightCm <= 0) throw IllegalArgumentException("Invalid input")
        val heightM = heightCm / 100
        return weightKg / (heightM * heightM)
    }
}