package com.hrysenko.dailyquest

import org.junit.Assert.*
import org.junit.Test

class BmiCalculatorTest {

    @Test
    fun bmi_isCorrect_forNormalInput() {
        val bmi = BmiCalculator.calculateBmi(70.0, 175.0)
        assertEquals(22.86, bmi, 0.01)
    }

    @Test
    fun bmi_isCorrect_forUnderweight() {
        val bmi = BmiCalculator.calculateBmi(45.0, 165.0)
        assertEquals(16.53, bmi, 0.01)
    }

    @Test
    fun bmi_isCorrect_forOverweight() {
        val bmi = BmiCalculator.calculateBmi(85.0, 170.0)
        assertEquals(29.41, bmi, 0.01)
    }

    @Test
    fun bmi_isCorrect_forObesity() {
        val bmi = BmiCalculator.calculateBmi(120.0, 165.0)
        assertEquals(44.08, bmi, 0.01)
    }

    @Test
    fun bmi_isCorrect_forHighHeight() {
        val bmi = BmiCalculator.calculateBmi(100.0, 200.0)
        assertEquals(25.0, bmi, 0.01)
    }

    @Test(expected = IllegalArgumentException::class)
    fun bmi_throwsException_forZeroWeight() {
        BmiCalculator.calculateBmi(0.0, 170.0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun bmi_throwsException_forNegativeHeight() {
        BmiCalculator.calculateBmi(70.0, -160.0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun bmi_throwsException_forBothValuesZero() {
        BmiCalculator.calculateBmi(0.0, 0.0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun bmi_throwsException_forBothValuesNegative() {
        BmiCalculator.calculateBmi(-70.0, -160.0)
    }

    @Test
    fun bmi_isCorrect_forMinimumPositiveValues() {
        val bmi = BmiCalculator.calculateBmi(1.0, 1.0)
        assertEquals(10000.0, bmi, 0.01)
    }
}
