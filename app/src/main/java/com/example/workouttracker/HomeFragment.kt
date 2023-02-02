package com.example.workouttracker

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.navigation.fragment.findNavController
import com.example.workouttracker.databinding.FragmentHomeBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var usernameTextView: TextView
    private lateinit var username: String
    private lateinit var email: String

    private lateinit var workoutButton: Button
    private lateinit var restdayButton: Button
    private lateinit var logoutButton: Button

    private lateinit var navigation: BottomNavigationView

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        usernameTextView = binding.username
        workoutButton = binding.workoutButton
        restdayButton = binding.restdayButton
        logoutButton = binding.logoutButton
        navigation = binding.bottomNavigation

        navigation.selectedItemId = R.id.new_workout

        navigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.calendar -> {
                    findNavController().navigate(R.id.calendarFragment)
                    true
                }
                R.id.stats -> {
                    findNavController().navigate(R.id.statsFragment)
                    true
                }
                R.id.new_workout -> {
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
                                if (todayWorkoutCounter > 0) {
                                    findNavController().navigate(R.id.lobbyFragment)
                                }
                                else {
                                    findNavController().navigate(R.id.homeFragment)
                                }
                            }.addOnFailureListener {
                                Log.i("current user", "/")
                            }
                    }
                    true
                }
                R.id.profile -> {
                    findNavController().navigate(R.id.profileFragment)
                    true
                }
                R.id.settings -> {
                    true
                }
                else -> false
            }
        }

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val id = auth.currentUser?.uid
        if (id != null) {
            firestore.collection("users").document(id).get().addOnSuccessListener { user ->
                username = user.getString("username").toString()
                email = user.getString("email").toString()
                usernameTextView.text = username
                Log.i("current user", "$username --> $email")
            }.addOnFailureListener {
                Log.i("current user", "/")
            }
        }
        else {
            Toast.makeText(context, "nobody is currently logged in", Toast.LENGTH_SHORT).show()
            Log.i("current user", "nobody is logged in")
        }

        workoutButton.setOnClickListener {
            findNavController().navigate(R.id.inputFragment)
        }

        restdayButton.setOnClickListener {
            findNavController().navigate(R.id.restDayFragment)
        }

        logoutButton.setOnClickListener {
            logout()
            findNavController().navigate(R.id.loginFragment)
        }

        return binding.root
    }

    private fun logout() {
        auth.signOut()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getDate(): String {
        val currentDate = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
        return currentDate.format(formatter)
    }
}