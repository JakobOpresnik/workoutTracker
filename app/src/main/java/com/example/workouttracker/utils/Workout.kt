package com.example.workouttracker.utils

import java.util.UUID

enum class WorkoutType {
    WEIGHTLIFTING,
    GYM_CARDIO,
    RUN,
    SPRINT,
    HIKE,
    BICYCLE_RIDE
}

data class Workout (
    val type: WorkoutType,
    val time: String,
    val date: String,
    val duration: Int,
    val motivation: Int,
    val exhaustion: Int
)