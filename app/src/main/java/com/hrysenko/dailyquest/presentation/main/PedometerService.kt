package com.hrysenko.dailyquest.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.hrysenko.dailyquest.R
import kotlinx.coroutines.*
import java.util.*

class PedometerService : Service(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private var stepCounterSensor: Sensor? = null
    private var stepDetectorSensor: Sensor? = null
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
        private const val NOTIFICATION_CHANNEL_ID = "PedometerServiceChannel"
        private const val NOTIFICATION_ID = 1

        private var stepCount = 0
        private var calorieCount = 0.0
        fun getCurrentSteps(): Int = stepCount
        fun getCurrentCalories(): Double = calorieCount
    }

    override fun onCreate() {
        super.onCreate()
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        stepDetectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
        restoreState()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Pedometer Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Used for step counting service"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val intent = Intent(this, com.hrysenko.dailyquest.presentation.main.MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(getString(R.string.step_counter))
            .setContentText(getString(R.string.counting_your_steps))
            .setSmallIcon(android.R.drawable.ic_menu_directions)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (stepCounterSensor != null || stepDetectorSensor != null) {
            stepCounterSensor?.let {
                sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
            }
            stepDetectorSensor?.let {
                sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
            }
        } else {
            stopSelf()
        }

        scope.launch {
            scheduleDailyReset()
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            when (it.sensor.type) {
                Sensor.TYPE_STEP_COUNTER -> {
                    val totalSteps = it.values[0].toInt()
                    updateSteps(totalSteps)
                }
                Sensor.TYPE_STEP_DETECTOR -> {
                    // Each step detection event increments by 1
                    if (it.values[0] == 1.0f) {
                        currentSteps++
                        updateStepsAfterDetection()
                    }
                }
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

        broadcastUpdate()
    }

    private fun updateStepsAfterDetection() {
        currentCalories = currentSteps * CALORIES_PER_STEP
        stepCount = currentSteps
        calorieCount = currentCalories
        saveState()

        broadcastUpdate()
    }

    private fun broadcastUpdate() {
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
        broadcastUpdate()
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
        job.cancel()
    }
}