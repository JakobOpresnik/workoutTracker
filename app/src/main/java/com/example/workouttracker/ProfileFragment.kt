package com.example.workouttracker

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.workouttracker.databinding.FragmentProfileBinding
import com.example.workouttracker.utils.Workout
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding
    private lateinit var navigation: BottomNavigationView
    private lateinit var username: TextView
    private lateinit var email: TextView
    private lateinit var numberWorkouts: TextView

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(inflater, container, false)

        navigation = binding.bottomNavigation
        username = binding.username
        email = binding.email
        numberWorkouts = binding.number

        navigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.calendar -> {
                    true
                }
                R.id.stats -> {
                    true
                }
                R.id.new_workout -> {
                    findNavController().navigate(R.id.homeFragment)
                    true
                }
                R.id.profile -> {
                    findNavController().navigate(R.id.profileFragment)
                    true
                }
                R.id.settings -> {
                    true
                }
                else -> true
            }
        }

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val id = auth.currentUser?.uid
        if (id != null) {
            firestore.collection("users").document(id).get().addOnSuccessListener { user ->
                username.text = user.getString("username").toString()
                email.text = user.getString("email").toString()
                val workouts = user.get("workouts") as List<Workout>
                numberWorkouts.text = workouts.size.toString()

            }.addOnFailureListener {
                Log.i("current user", "/")
            }
        }
        else {
            Toast.makeText(context, "nobody is currently logged in", Toast.LENGTH_SHORT).show()
            Log.i("current user", "nobody is logged in")
        }

        return binding.root
    }
}