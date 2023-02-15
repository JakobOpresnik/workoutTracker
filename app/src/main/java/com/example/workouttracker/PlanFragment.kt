package com.example.workouttracker

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.transition.ChangeBounds
import android.transition.TransitionManager
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.example.workouttracker.databinding.FragmentPlanBinding
import com.example.workouttracker.utils.Exercise
import com.example.workouttracker.utils.MuscleGroup
import com.example.workouttracker.utils.WorkoutDay
import com.example.workouttracker.utils.WorkoutPlan
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class PlanFragment : Fragment() {

    private lateinit var binding: FragmentPlanBinding
    private lateinit var navigation: BottomNavigationView

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPlanBinding.inflate(inflater, container, false)
        navigation = binding.bottomNavigation

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        navigation.selectedItemId = R.id.calendar

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

        // adding dynamic input fields
        val inputsMonday = addExerciseInputs(binding.mondayButton, binding.addExerciseMonday, binding.mondayButton.id)
        val inputsTuesday = addExerciseInputs(binding.tuesdayButton, binding.addExerciseTuesday, binding.tuesdayButton.id)
        val inputsWednesday  = addExerciseInputs(binding.wednesdayButton, binding.addExerciseWednesday, binding.wednesdayButton.id)
        val inputsThursday = addExerciseInputs(binding.thursdayButton, binding.addExerciseThursday, binding.thursdayButton.id)
        val inputsFriday = addExerciseInputs(binding.fridayButton, binding.addExerciseFriday, binding.fridayButton.id)
        val inputsSaturday = addExerciseInputs(binding.saturdayButton, binding.addExerciseSaturday, binding.saturdayButton.id)
        val inputsSunday = addExerciseInputs(binding.sundayButton, binding.addExerciseSunday, binding.sundayButton.id)

        // save workout plan
        val savePlanButton = binding.saveButton
        savePlanButton.setOnClickListener {
            // setup each day
            val workoutMonday = makeWorkoutPlan("monday", inputsMonday)
            val workoutTuesday = makeWorkoutPlan("tuesday", inputsTuesday)
            val workoutWednesday = makeWorkoutPlan("wednesday", inputsWednesday)
            val workoutThursday = makeWorkoutPlan("thursday", inputsThursday)
            val workoutFriday = makeWorkoutPlan("friday", inputsFriday)
            val workoutSaturday = makeWorkoutPlan("saturday", inputsSaturday)
            val workoutSunday = makeWorkoutPlan("sunday", inputsSunday)

            val workoutDays = mutableListOf(workoutMonday, workoutTuesday, workoutWednesday, workoutThursday, workoutFriday, workoutSaturday, workoutSunday)

            val workoutPlan = WorkoutPlan("current split", workoutDays)

            val id = auth.currentUser?.uid
            if (id != null) {
                firestore.collection("users").document(id)
                    .update("workout_plan", FieldValue.arrayUnion(workoutPlan))
                    .addOnSuccessListener {
                        Log.i("workout_plan", "workout plan added to user")
                    }.addOnFailureListener {
                        Log.e("workout_plan", "workout plan not added to user")
                    }
            }
        }

        return binding.root
    }

    private fun makeWorkoutPlan(day: String, exerciseInputs: MutableList<EditText>): WorkoutDay {
        val exercises = mutableListOf<Exercise>()
        for (input in exerciseInputs) {
            val exercise = Exercise(input.text.toString(), MuscleGroup.BICEPS, 3, 12)
            exercises.add(exercise)
        }
        val workoutDay = WorkoutDay(day, mutableListOf(MuscleGroup.CHEST, MuscleGroup.TRICEPS), exercises)
        return workoutDay
    }

    // clicking the 'add' button dynamically adds elements on screen and updates their constraints
    private fun addExerciseInputs(dayButton: Button, addButton: ImageButton, lastInputFieldId: Int): MutableList<EditText> {
        val inputFields = mutableListOf<EditText>()
        var numTextViews = 0
        var lastId = lastInputFieldId
        addButton.setOnClickListener {
            val constraintLayout = binding.layout
            val newInputField = EditText(context)
            newInputField.hint = "${++numTextViews}. exercise"
            newInputField.setHintTextColor(Color.argb(200, 255, 255, 255))
            newInputField.setBackgroundResource(R.drawable.custom_input_box)
            newInputField.setPadding(30, 10, 15, 10)
            newInputField.width = 820
            newInputField.height = 120
            // ensuring unique input field IDs
            when (dayButton.text) {
                "MONDAY" -> {
                    newInputField.id = numTextViews+1000
                }
                "TUESDAY" -> {
                    newInputField.id = numTextViews+2000
                }
                "WEDNESDAY" -> {
                    newInputField.id = numTextViews+3000
                }
                "THURSDAY" -> {
                    newInputField.id = numTextViews+4000
                }
                "FRIDAY" -> {
                    newInputField.id = numTextViews+5000
                }
                "SATURDAY" -> {
                    newInputField.id = numTextViews+6000
                }
                "SUNDAY" -> {
                    newInputField.id = numTextViews+7000
                }
            }
            newInputField.setTextColor(Color.WHITE)
            constraintLayout.addView(newInputField)
            newInputField.layoutParams = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
            )
            val constraintSet = ConstraintSet()
            constraintSet.clone(constraintLayout)
            constraintSet.connect(
                newInputField.id,
                ConstraintSet.TOP,
                lastId,
                ConstraintSet.BOTTOM,
                40
            )
            constraintSet.connect(
                newInputField.id,
                ConstraintSet.START,
                dayButton.id,
                ConstraintSet.START
            )
            constraintSet.connect(
                newInputField.id,
                ConstraintSet.END,
                dayButton.id,
                ConstraintSet.END
            )

            constraintSet.applyTo(constraintLayout)

            val transition = ChangeBounds()
            transition.duration = 200
            TransitionManager.beginDelayedTransition(constraintLayout, transition)

            constraintSet.connect(
                addButton.id,
                ConstraintSet.TOP,
                newInputField.id,
                ConstraintSet.BOTTOM,
                40
            )
            constraintSet.applyTo(constraintLayout)
            lastId = newInputField.id

            inputFields.add(newInputField)
        }

        return inputFields
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getDate(): String {
        val currentDate = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
        return currentDate.format(formatter)
    }
}