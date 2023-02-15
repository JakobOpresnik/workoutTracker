package com.example.workouttracker.utils

data class Exercise (
    val name: String,
    val muscleGroup: MuscleGroup,
    val sets: Int,
    val repetitions: Int
)