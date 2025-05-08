package com.hrysenko.dailyquest.presentation.quests

import android.content.Context
import com.hrysenko.dailyquest.R
import com.hrysenko.dailyquest.models.user.room.User
import java.time.LocalDate
import kotlin.math.pow

object QuestGenerator {
    fun calculateBMI(user: User): Double? {
        return if (user.height != null && user.weight != null && user.height!! > 0) {
            user.weight!! / (user.height!! / 100).pow(2)
        } else null
    }

    fun generateDailyQuests(context: Context, user: User?): List<Quest> {
        val date = LocalDate.now().toString()
        val quests = mutableListOf<Quest>()

        val defaultUser = user ?: User(
            id = 1,
            name = "Guest",
            age = 30,
            height = 170.0,
            weight = 70.0,
            sex = "male",
            goal = "maintain fitness",
            physLevel = "moderate",
            trainingLocation = "home"
        )
        val selectedUser = user ?: defaultUser

        val bmi = calculateBMI(selectedUser)
        val intensityMultiplier = when (selectedUser.physLevel) {
            "sedentary" -> 0.7
            "light" -> 0.9
            "moderate" -> 1.0
            "active" -> 1.2
            else -> 1.0
        }

        val ageFactor = when {
            selectedUser.age < 25 -> 1.1
            selectedUser.age > 45 -> 0.8
            else -> 1.0
        }

        val bmiFactor = when {
            bmi == null -> 1.0
            bmi < 18.5 -> 0.8
            bmi > 30 -> 0.7
            else -> 1.0
        }

        val isGym = selectedUser.trainingLocation == "gym"
        val trainingLocation = selectedUser.trainingLocation ?: "home"

        when (selectedUser.goal) {
            context.getString(R.string.lose_weight) -> {
                quests.add(
                    Quest(
                        name = context.getString(R.string.quest_walk),
                        amount = ((5000 * intensityMultiplier * ageFactor * bmiFactor).toInt() / 100 * 100).coerceAtLeast(2000),
                        complexity = if (intensityMultiplier < 1.0) context.getString(R.string.complexity_easy) else context.getString(R.string.complexity_moderate),
                        date = date
                    )
                )
                quests.add(
                    Quest(
                        name = context.getString(R.string.quest_jumping_jacks),
                        amount = (30 * intensityMultiplier * ageFactor * bmiFactor).toInt().coerceAtLeast(20),
                        complexity = context.getString(R.string.complexity_easy),
                        date = date
                    )
                )
                quests.add(
                    Quest(
                        name = context.getString(R.string.quest_high_knees),
                        amount = (25 * intensityMultiplier * ageFactor * bmiFactor).toInt().coerceAtLeast(15),
                        complexity = context.getString(R.string.complexity_moderate),
                        date = date
                    )
                )
                quests.add(
                    Quest(
                        name = if (isGym) context.getString(R.string.quest_treadmill) else context.getString(R.string.quest_bicycle_crunches),
                        amount = (if (isGym) 15 else 20 * intensityMultiplier * ageFactor * bmiFactor).toInt().coerceAtLeast(if (isGym) 10 else 12),
                        complexity = context.getString(R.string.complexity_moderate),
                        date = date
                    )
                )
            }
            context.getString(R.string.gain_muscle_mass) -> {
                quests.add(
                    Quest(
                        name = if (isGym) context.getString(R.string.quest_bench_press) else context.getString(R.string.quest_push_ups),
                        amount = (20 * intensityMultiplier * ageFactor * bmiFactor).toInt().coerceAtLeast(15),
                        complexity = if (intensityMultiplier > 1.0) context.getString(R.string.complexity_hard) else context.getString(R.string.complexity_moderate),
                        date = date
                    )
                )
                quests.add(
                    Quest(
                        name = if (isGym) context.getString(R.string.quest_deadlift) else context.getString(R.string.quest_squats),
                        amount = (25 * intensityMultiplier * ageFactor * bmiFactor).toInt().coerceAtLeast(20),
                        complexity = context.getString(R.string.complexity_moderate),
                        date = date
                    )
                )
                quests.add(
                    Quest(
                        name = if (isGym) context.getString(R.string.quest_lat_pulldown) else context.getString(R.string.quest_lunges),
                        amount = (20 * intensityMultiplier * ageFactor * bmiFactor).toInt().coerceAtLeast(15),
                        complexity = context.getString(R.string.complexity_moderate),
                        date = date
                    )
                )
                quests.add(
                    Quest(
                        name = context.getString(R.string.quest_plank),
                        amount = (40 * intensityMultiplier * ageFactor * bmiFactor).toInt().coerceAtLeast(30),
                        complexity = context.getString(R.string.complexity_hard),
                        date = date
                    )
                )
            }
            context.getString(R.string.maintain_fitness) -> {
                quests.add(
                    Quest(
                        name = context.getString(R.string.quest_walk),
                        amount = ((4000 * intensityMultiplier * ageFactor * bmiFactor).toInt() / 100 * 100).coerceAtLeast(2000),
                        complexity = context.getString(R.string.complexity_easy),
                        date = date
                    )
                )
                quests.add(
                    Quest(
                        name = context.getString(R.string.quest_plank),
                        amount = (30 * intensityMultiplier * ageFactor * bmiFactor).toInt().coerceAtLeast(20),
                        complexity = context.getString(R.string.complexity_moderate),
                        date = date
                    )
                )
                quests.add(
                    Quest(
                        name = context.getString(R.string.quest_stretch),
                        amount = (15 * intensityMultiplier * ageFactor * bmiFactor).toInt().coerceAtLeast(10),
                        complexity = context.getString(R.string.complexity_easy),
                        date = date
                    )
                )
                quests.add(
                    Quest(
                        name = if (isGym) context.getString(R.string.quest_leg_press) else context.getString(R.string.quest_sit_ups),
                        amount = (20 * intensityMultiplier * ageFactor * bmiFactor).toInt().coerceAtLeast(15),
                        complexity = context.getString(R.string.complexity_moderate),
                        date = date
                    )
                )
            }
            else -> {
                quests.add(
                    Quest(
                        name = context.getString(R.string.quest_walk),
                        amount = 4000,
                        complexity = context.getString(R.string.complexity_easy),
                        date = date
                    )
                )
                quests.add(
                    Quest(
                        name = context.getString(R.string.quest_stretch),
                        amount = 15,
                        complexity = context.getString(R.string.complexity_easy),
                        date = date
                    )
                )
                quests.add(
                    Quest(
                        name = context.getString(R.string.quest_jumping_jacks),
                        amount = 20,
                        complexity = context.getString(R.string.complexity_easy),
                        date = date
                    )
                )
                if (isGym) {
                    quests.add(
                        Quest(
                            name = context.getString(R.string.quest_treadmill),
                            amount = 10,
                            complexity = context.getString(R.string.complexity_moderate),
                            date = date
                        )
                    )
                } else {
                    quests.add(
                        Quest(
                            name = context.getString(R.string.quest_sit_ups),
                            amount = 15,
                            complexity = context.getString(R.string.complexity_moderate),
                            date = date
                        )
                    )
                }
            }
        }

        if (selectedUser.sex == context.getString(R.string.female)) {
            quests.forEach { quest ->
                quest.amount = (quest.amount * 0.9).toInt().coerceAtLeast(10)
            }
        }

        return quests
    }
}