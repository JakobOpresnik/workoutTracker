package com.example.workouttracker

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.fragment.findNavController
import com.example.workouttracker.databinding.FragmentProfileBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding
    private lateinit var navigation: BottomNavigationView
    private lateinit var username: TextView
    private lateinit var email: TextView
    private lateinit var inputBio: EditText
    private lateinit var addBio: Button
    private lateinit var displayBio: TextView
    private lateinit var numberWorkouts: TextView
    private lateinit var popularActivity: TextView
    private lateinit var pastWorkoutsButton: Button
    private lateinit var logoutButton: Button

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(inflater, container, false)

        navigation = binding.bottomNavigation
        username = binding.username
        email = binding.email
        inputBio = binding.inputBio
        addBio = binding.addBioButton
        displayBio = binding.displayBio
        numberWorkouts = binding.number
        popularActivity = binding.activity
        pastWorkoutsButton = binding.pastWorkouts
        logoutButton = binding.logoutButton

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        navigation.selectedItemId = R.id.profile

        navigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.calendar -> {
                    findNavController().navigate(R.id.planFragment)
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

        var counterWeightlifting = 0
        var counterGymCardio = 0
        var counterRun = 0
        var counterSprint = 0
        var counterHike = 0
        var counterBicycle = 0

        val id = auth.currentUser?.uid
        if (id != null) {
            firestore.collection("users").document(id).get().addOnSuccessListener { user ->
                username.text = user.getString("username").toString()
                email.text = user.getString("email").toString()
                val workouts = user.get("workouts") as MutableList<HashMap<String, String>>
                // get number of workouts
                numberWorkouts.text = workouts.size.toString()
                // count each type of workout
                for (workout in workouts) {
                    when (workout["type"]) {
                        "WEIGHTLIFTING" -> {
                            counterWeightlifting++
                        }
                        "GYM_CARDIO" -> {
                            counterGymCardio++
                        }
                        "RUN" -> {
                            counterRun++
                        }
                        "SPRINT" -> {
                            counterSprint++
                        }
                        "HIKE" -> {
                            counterHike++
                        }
                        "BICYCLE_RIDE" -> {
                            counterBicycle++
                        }
                    }
                }
                val counters = listOf(counterWeightlifting, counterGymCardio, counterRun, counterSprint, counterHike, counterBicycle)
                val positiveCounters = counters.filter { it > 0 }
                if (positiveCounters.isEmpty()) {
                    popularActivity.text = "no activities recorded yet"
                }
                else {
                    // check if all types of workouts are equally popular
                    // in which case, pick the latest one
                    if (positiveCounters.distinct().size == 1) {
                        popularActivity.text = workouts[workouts.size-1]["type"].toString().replace("_", " ").toLowerCase().capitalize()
                    }
                    else {
                        // get most popular type of workout if not all are equally popular
                        when (counters.max()) {
                            counterWeightlifting -> {
                                popularActivity.text = "Weightlifting"
                            }
                            counterGymCardio -> {
                                popularActivity.text = "Gym Cardio"
                            }
                            counterRun -> {
                                popularActivity.text = "Run"
                            }
                            counterSprint -> {
                                popularActivity.text = "Sprint"
                            }
                            counterHike -> {
                                popularActivity.text = "Hike"
                            }
                            counterBicycle -> {
                                popularActivity.text = "Bicycle Ride"
                            }
                        }
                    }
                }
            }.addOnFailureListener {
                Log.i("current user", "/")
            }
        }
        else {
            Toast.makeText(context, "nobody is currently logged in", Toast.LENGTH_SHORT).show()
            Log.i("current user", "nobody is logged in")
        }

        if (id != null) {
            firestore.collection("users").document(id).get().addOnSuccessListener { user ->
                val bio = user.getString("bio")

                // if user hasn't set a bio yet
                if (bio == "") {
                    inputBio.visibility = View.VISIBLE
                    addBio.visibility = View.VISIBLE

                    // first time setting bio
                    addBio.setOnClickListener {
                        val userBio = inputBio.text
                        val params = displayBio.layoutParams as ConstraintLayout.LayoutParams
                        params.horizontalBias = 0.5f
                        params.verticalBias = 0.5f

                        // if bio is empty
                        if (userBio.isEmpty()) {
                            inputBio.visibility = View.VISIBLE
                            addBio.visibility = View.VISIBLE
                            displayBio.visibility = View.GONE
                            displayBio.layoutParams = params

                            // empty string is saved to database
                            firestore.collection("users").document(id)
                                .update("bio", "")
                                .addOnSuccessListener {
                                    Log.i("bio", "bio added to user")
                                }.addOnFailureListener {
                                    Log.e("bio", "bio not added to user")
                                }

                            /*displayBio.setOnClickListener {
                                inputBio.visibility = View.VISIBLE
                                addBio.visibility = View.VISIBLE
                                displayBio.visibility = View.GONE
                                val editableBio = Editable.Factory.getInstance().newEditable(displayBio.text)
                                inputBio.text = editableBio
                            }*/
                        }
                        // if bio isn't empty
                        else {
                            inputBio.visibility = View.GONE
                            addBio.visibility = View.GONE
                            displayBio.visibility = View.VISIBLE
                            displayBio.layoutParams = params

                            displayBio.text = userBio.toString()
                            // user bio saved to database
                            firestore.collection("users").document(id)
                                .update("bio", displayBio.text.toString())
                                .addOnSuccessListener {
                                    Log.i("bio", "bio added to user")
                                }.addOnFailureListener {
                                    Log.e("bio", "bio not added to user")
                                }

                            // if user clicks on the bio to edit it
                            displayBio.setOnClickListener {
                                inputBio.visibility = View.VISIBLE
                                addBio.visibility = View.VISIBLE
                                displayBio.visibility = View.GONE
                                // bio is set as text into the EditText component
                                val editableBio = Editable.Factory.getInstance().newEditable(displayBio.text)
                                inputBio.text = editableBio
                            }
                        }
                    }
                }
                // if user already had a bio set
                else {
                    inputBio.visibility = View.GONE
                    addBio.visibility = View.GONE

                    displayBio.text = bio

                    // if user tries to edit the bio (by clicking on it)
                    displayBio.setOnClickListener {
                        inputBio.visibility = View.VISIBLE
                        addBio.visibility = View.VISIBLE
                        displayBio.visibility = View.GONE
                        var editableBio = Editable.Factory.getInstance().newEditable(displayBio.text)
                        inputBio.text = editableBio

                        // saving new bio
                        addBio.setOnClickListener {
                            val userBio = inputBio.text
                            val params = displayBio.layoutParams as ConstraintLayout.LayoutParams
                            params.horizontalBias = 0.5f
                            params.verticalBias = 0.5f

                            // if bio is empty
                            if (userBio.isEmpty()) {
                                inputBio.visibility = View.VISIBLE
                                addBio.visibility = View.VISIBLE
                                displayBio.visibility = View.GONE
                                displayBio.layoutParams = params

                                // empty string is saved to database
                                firestore.collection("users").document(id)
                                    .update("bio", "")
                                    .addOnSuccessListener {
                                        Log.i("bio", "bio added to user")
                                    }.addOnFailureListener {
                                        Log.e("bio", "bio not added to user")
                                    }
                            }
                            // if bio isn't empty
                            else {
                                inputBio.visibility = View.GONE
                                addBio.visibility = View.GONE
                                displayBio.visibility = View.VISIBLE
                                displayBio.layoutParams = params

                                displayBio.text = userBio.toString()
                                // user bio saved to database
                                firestore.collection("users").document(id)
                                    .update("bio", displayBio.text.toString())
                                    .addOnSuccessListener {
                                        Log.i("bio", "bio added to user")
                                    }.addOnFailureListener {
                                        Log.e("bio", "bio not added to user")
                                    }

                                // if user clicks on the bio to edit it
                                displayBio.setOnClickListener {
                                    inputBio.visibility = View.VISIBLE
                                    addBio.visibility = View.VISIBLE
                                    displayBio.visibility = View.GONE
                                    // bio is set as text into the EditText component
                                    editableBio = Editable.Factory.getInstance().newEditable(displayBio.text)
                                    inputBio.text = editableBio
                                }
                            }
                        }
                    }
                }
            }
        }

        pastWorkoutsButton.setOnClickListener {
            findNavController().navigate(R.id.listWorkoutsFragment)
        }

        logoutButton.setOnClickListener {
            logout()
            findNavController().navigate(R.id.loginFragment)
        }


        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getDate(): String {
        val currentDate = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
        return currentDate.format(formatter)
    }

    private fun logout() {
        auth.signOut()
    }
}