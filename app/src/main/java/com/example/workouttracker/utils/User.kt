package com.example.workouttracker.utils

data class User(
    val username: String,
    val email: String,
    val workouts: MutableList<Workout>
)