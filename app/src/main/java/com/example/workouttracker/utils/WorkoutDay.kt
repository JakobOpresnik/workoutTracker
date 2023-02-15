package com.example.workouttracker.utils

enum class MuscleGroup {
    CHEST,
    TRICEPS,
    BICEPS,
    FOREARMS,
    BACK,
    SHOULDERS,
    LEGS,
    ABS,
    NECK,
    HIPS
}

data class WorkoutDay (
    val name: String,       // monday/tuesday/.../sunday
    val muscleGroups: MutableList<MuscleGroup>,
    val exercises: MutableList<Exercise>
)