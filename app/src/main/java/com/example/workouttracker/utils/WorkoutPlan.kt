package com.example.workouttracker.utils

data class WorkoutPlan (
    val label: String,
    val days: MutableList<WorkoutDay>
)