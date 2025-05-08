package com.hrysenko.dailyquest.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.IBinder
import kotlinx.coroutines.*
import java.util.*

class PedometerService : Service(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private var stepSensor: Sensor? = null
    private var initialSteps = -1
    private var currentSteps = 0
    private var currentCalories = 0.0
    private var lastResetTime = 0L
    private lateinit var sharedPreferences: SharedPreferences
    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    companion object {
        const val STEP_UPDATE_ACTION = "com.hrysenko.dailyquest.STEP_UPDATE"
        const val EXTRA_STEPS = "steps"
        const val EXTRA_CALORIES = "calories"
        private const val PREFS_NAME = "PedometerPrefs"
        private const val KEY_INITIAL_STEPS = "initialSteps"
        private const val KEY_CURRENT_STEPS = "currentSteps"
        private const val KEY_CURRENT_CALORIES = "currentCalories"
        private const val KEY_LAST_RESET = "lastResetTime"
        private const val CALORIES_PER_STEP = 0.04 // Average calories burned per step

        private var stepCount = 0
        private var calorieCount = 0.0
        fun getCurrentSteps(): Int = stepCount
        fun getCurrentCalories(): Double = calorieCount
    }

    override fun onCreate() {
        super.onCreate()
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        restoreState()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        stepSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        } ?: stopSelf()

        scope.launch {
            scheduleDailyReset()
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (it.sensor.type == Sensor.TYPE_STEP_COUNTER) {
                val totalSteps = it.values[0].toInt()
                updateSteps(totalSteps)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun restoreState() {
        initialSteps = sharedPreferences.getInt(KEY_INITIAL_STEPS, -1)
        currentSteps = sharedPreferences.getInt(KEY_CURRENT_STEPS, 0)
        currentCalories = sharedPreferences.getFloat(KEY_CURRENT_CALORIES, 0.0f).toDouble()
        lastResetTime = sharedPreferences.getLong(KEY_LAST_RESET, System.currentTimeMillis())
        stepCount = currentSteps
        calorieCount = currentCalories

        val calendar = Calendar.getInstance()
        val lastResetCalendar = Calendar.getInstance().apply { timeInMillis = lastResetTime }
        if (calendar.get(Calendar.DAY_OF_YEAR) != lastResetCalendar.get(Calendar.DAY_OF_YEAR) ||
            calendar.get(Calendar.YEAR) != lastResetCalendar.get(Calendar.YEAR)
        ) {
            resetSteps()
        }
    }

    private fun saveState() {
        sharedPreferences.edit().apply {
            putInt(KEY_INITIAL_STEPS, initialSteps)
            putInt(KEY_CURRENT_STEPS, currentSteps)
            putFloat(KEY_CURRENT_CALORIES, currentCalories.toFloat())
            putLong(KEY_LAST_RESET, lastResetTime)
            apply()
        }
    }

    private fun updateSteps(totalSteps: Int) {
        if (initialSteps == -1) {
            initialSteps = totalSteps
            saveState()
        }

        currentSteps = totalSteps - initialSteps
        if (currentSteps < 0) {
            initialSteps = totalSteps
            currentSteps = 0
            currentCalories = 0.0
            saveState()
        }

        currentCalories = currentSteps * CALORIES_PER_STEP
        stepCount = currentSteps
        calorieCount = currentCalories
        saveState()

        sendBroadcast(Intent(STEP_UPDATE_ACTION).apply {
            putExtra(EXTRA_STEPS, currentSteps)
            putExtra(EXTRA_CALORIES, currentCalories)
        })
    }

    private fun scheduleDailyReset() {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            add(Calendar.DAY_OF_YEAR, 1)
        }

        val delay = calendar.timeInMillis - System.currentTimeMillis()
        scope.launch {
            delay(delay)
            resetSteps()
            scheduleDailyReset()
        }
    }

    private fun resetSteps() {
        initialSteps = -1
        currentSteps = 0
        currentCalories = 0.0
        stepCount = 0
        calorieCount = 0.0
        lastResetTime = System.currentTimeMillis()
        saveState()
        sendBroadcast(Intent(STEP_UPDATE_ACTION).apply {
            putExtra(EXTRA_STEPS, 0)
            putExtra(EXTRA_CALORIES, 0.0)
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
        job.cancel()
    }
}