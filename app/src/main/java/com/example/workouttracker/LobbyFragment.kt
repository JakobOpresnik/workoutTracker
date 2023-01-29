package com.example.workouttracker

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.SubMenu
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.navigation.fragment.findNavController
import com.example.workouttracker.databinding.FragmentLobbyBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class LobbyFragment : Fragment() {

    private lateinit var binding: FragmentLobbyBinding
    private lateinit var anotherWorkoutButton: Button
    private lateinit var calendarButton: Button
    private lateinit var statsButton: Button
    private lateinit var title: TextView

    // firebase database
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLobbyBinding.inflate(inflater, container, false)
        anotherWorkoutButton = binding.anotherWorkoutButton
        calendarButton = binding.calendarButton
        statsButton = binding.statsButton
        title = binding.title

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val id = auth.currentUser?.uid
        if (id != null) {
            firestore.collection("users").document(id).get()
                .addOnSuccessListener { user ->
                    val workouts = user.get("workouts") as MutableList<HashMap<String, String>>
                    val currentDate = getDate()
                    var todayWorkoutCounter = 0
                    for (workout in workouts) {
                        if (workout["date"] == currentDate) {
                            todayWorkoutCounter++
                        }
                    }
                    // edit title text to say how many workouts you've done today already
                    val titleText = title.text
                    val index = titleText.lastIndexOf(" ")
                    val substringTitle = titleText.substring(0, index)
                    val substring = when (todayWorkoutCounter) {
                        1 -> {
                            "once today!"
                        }
                        2 -> {
                            "twice today!"
                        }
                        else -> {
                            "$todayWorkoutCounter times today!"
                        }
                    }
                    val newTitleText = "$substringTitle $substring"
                    title.text = newTitleText

                }.addOnFailureListener {
                    Log.i("current user", "/")
                }
        }

        // get number of workouts for logged in user from login page via navigation argument
        //val numberTodayWorkouts = arguments?.getString("today_workout_counter")

        anotherWorkoutButton.setOnClickListener {
            findNavController().navigate(R.id.inputFragment)
        }

        calendarButton.setOnClickListener {
            findNavController().navigate(R.id.calendarFragment)
        }

        statsButton.setOnClickListener {
            findNavController().navigate(R.id.statsFragment)
        }

        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getDate(): String {
        val currentDate = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
        return currentDate.format(formatter)
    }
}