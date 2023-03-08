package com.example.workouttracker

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.transition.ChangeBounds
import android.transition.TransitionManager
import android.transition.Visibility
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.navigation.fragment.findNavController
import com.example.workouttracker.databinding.FragmentPlanBinding
import com.example.workouttracker.utils.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class PlanFragment : Fragment() {

    private lateinit var binding: FragmentPlanBinding
    private lateinit var navigation: BottomNavigationView

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private lateinit var sharedPrefs: SharedPreferences

    private lateinit var selectedExercise: String

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPlanBinding.inflate(inflater, container, false)
        navigation = binding.bottomNavigation

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        sharedPrefs = requireActivity().getSharedPreferences("mySharedPrefs", Context.MODE_PRIVATE)

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

        val name = arguments?.getString("exercise")
        Log.i("exercise name", name.toString())


        val mondayButton = binding.mondayButton
        val tuesdayButton = binding.tuesdayButton
        val wednesdayButton = binding.wednesdayButton
        val thursdayButton = binding.thursdayButton
        val fridayButton = binding.fridayButton
        val saturdayButton = binding.saturdayButton
        val sundayButton = binding.sundayButton

        // adding dynamic input fields
        val spinnersMonday = addExerciseSpinners(mondayButton, binding.addExerciseMonday, mondayButton.id)
        val spinnersTuesday = addExerciseSpinners(tuesdayButton, binding.addExerciseTuesday, tuesdayButton.id)
        val spinnersWednesday  = addExerciseSpinners(wednesdayButton, binding.addExerciseWednesday, wednesdayButton.id)
        val spinnersThursday = addExerciseSpinners(thursdayButton, binding.addExerciseThursday, thursdayButton.id)
        val spinnersFriday = addExerciseSpinners(fridayButton, binding.addExerciseFriday, fridayButton.id)
        val spinnersSaturday = addExerciseSpinners(saturdayButton, binding.addExerciseSaturday, saturdayButton.id)
        val spinnersSunday = addExerciseSpinners(sundayButton, binding.addExerciseSunday, sundayButton.id)

        muscleGroupSelectionDialog(mondayButton)
        muscleGroupSelectionDialog(tuesdayButton)
        muscleGroupSelectionDialog(wednesdayButton)
        muscleGroupSelectionDialog(thursdayButton)
        muscleGroupSelectionDialog(fridayButton)
        muscleGroupSelectionDialog(saturdayButton)
        muscleGroupSelectionDialog(sundayButton)

        // deserialize json string
        var mondayMuscleGroups: MutableList<String> = Gson().fromJson(sharedPrefs.getString("monday_muscle_groups", "[]"), object : TypeToken<MutableList<String>>() {}.type)
        var tuesdayMuscleGroups: MutableList<String> = Gson().fromJson(sharedPrefs.getString("tuesday_muscle_groups", "[]"), object : TypeToken<MutableList<String>>() {}.type)
        var wednesdayMuscleGroups: MutableList<String> = Gson().fromJson(sharedPrefs.getString("wednesday_muscle_groups", "[]"), object : TypeToken<MutableList<String>>() {}.type)
        var thursdayMuscleGroups: MutableList<String> = Gson().fromJson(sharedPrefs.getString("thursday_muscle_groups", "[]"), object : TypeToken<MutableList<String>>() {}.type)
        var fridayMuscleGroups: MutableList<String> = Gson().fromJson(sharedPrefs.getString("friday_muscle_groups", "[]"), object : TypeToken<MutableList<String>>() {}.type)
        var saturdayMuscleGroups: MutableList<String> = Gson().fromJson(sharedPrefs.getString("saturday_muscle_groups", "[]"), object : TypeToken<MutableList<String>>() {}.type)
        var sundayMuscleGroups: MutableList<String> = Gson().fromJson(sharedPrefs.getString("sunday_muscle_groups", "[]"), object : TypeToken<MutableList<String>>() {}.type)

        mondayMuscleGroups = mondayMuscleGroups.map { it.toUpperCase().replace("\\s".toRegex(), "") } as MutableList<String>
        val musclesMonday: MutableList<MuscleGroup> = mondayMuscleGroups.map { MuscleGroup.valueOf(it) } as MutableList<MuscleGroup>

        tuesdayMuscleGroups = tuesdayMuscleGroups.map { it.toUpperCase().replace("\\s".toRegex(), "") } as MutableList<String>
        val musclesTuesday: MutableList<MuscleGroup> = tuesdayMuscleGroups.map { MuscleGroup.valueOf(it) } as MutableList<MuscleGroup>

        wednesdayMuscleGroups = wednesdayMuscleGroups.map { it.toUpperCase().replace("\\s".toRegex(), "") } as MutableList<String>
        val musclesWednesday: MutableList<MuscleGroup> = wednesdayMuscleGroups.map { MuscleGroup.valueOf(it) } as MutableList<MuscleGroup>

        thursdayMuscleGroups = thursdayMuscleGroups.map { it.toUpperCase().replace("\\s".toRegex(), "") } as MutableList<String>
        val musclesThursday: MutableList<MuscleGroup> = thursdayMuscleGroups.map { MuscleGroup.valueOf(it) } as MutableList<MuscleGroup>

        fridayMuscleGroups = fridayMuscleGroups.map { it.toUpperCase().replace("\\s".toRegex(), "") } as MutableList<String>
        val musclesFriday: MutableList<MuscleGroup> = fridayMuscleGroups.map { MuscleGroup.valueOf(it) } as MutableList<MuscleGroup>

        saturdayMuscleGroups = saturdayMuscleGroups.map { it.toUpperCase().replace("\\s".toRegex(), "") } as MutableList<String>
        val musclesSaturday: MutableList<MuscleGroup> = saturdayMuscleGroups.map { MuscleGroup.valueOf(it) } as MutableList<MuscleGroup>

        sundayMuscleGroups = sundayMuscleGroups.map { it.toUpperCase().replace("\\s".toRegex(), "") } as MutableList<String>
        val musclesSunday: MutableList<MuscleGroup> = sundayMuscleGroups.map { MuscleGroup.valueOf(it) } as MutableList<MuscleGroup>

        Log.i("monday_muscle_groups", sharedPrefs.getString("monday_muscle_groups", null).toString())
        Log.i("tuesday_muscle_groups", sharedPrefs.getString("tuesday_muscle_groups", null).toString())

        for (i in musclesMonday) {
            Log.i("monday_muscle_groups", i.toString())
        }

        // save workout plan
        val savePlanButton = binding.saveButton
        savePlanButton.setOnClickListener {
            var disableSpinner = true
            if (savePlanButton.text == "SAVE YOUR WORKOUT PLAN") {
                // setup each day
                val workoutMonday = makeWorkoutPlan("monday", spinnersMonday, musclesMonday)
                val workoutTuesday = makeWorkoutPlan("tuesday", spinnersTuesday, musclesTuesday)
                val workoutWednesday =
                    makeWorkoutPlan("wednesday", spinnersWednesday, musclesWednesday)
                val workoutThursday = makeWorkoutPlan("thursday", spinnersThursday, musclesThursday)
                val workoutFriday = makeWorkoutPlan("friday", spinnersFriday, musclesFriday)
                val workoutSaturday = makeWorkoutPlan("saturday", spinnersSaturday, musclesSaturday)
                val workoutSunday = makeWorkoutPlan("sunday", spinnersSunday, musclesSunday)

                val workoutDays = mutableListOf(
                    workoutMonday,
                    workoutTuesday,
                    workoutWednesday,
                    workoutThursday,
                    workoutFriday,
                    workoutSaturday,
                    workoutSunday
                )

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

                binding.addExerciseMonday.visibility = View.GONE
                binding.addExerciseTuesday.visibility = View.GONE
                binding.addExerciseWednesday.visibility = View.GONE
                binding.addExerciseThursday.visibility = View.GONE
                binding.addExerciseFriday.visibility = View.GONE
                binding.addExerciseSaturday.visibility = View.GONE
                binding.addExerciseSunday.visibility = View.GONE

                changeSpinners(spinnersMonday, disableSpinner)
                changeSpinners(spinnersTuesday, disableSpinner)
                changeSpinners(spinnersWednesday, disableSpinner)
                changeSpinners(spinnersThursday, disableSpinner)
                changeSpinners(spinnersFriday, disableSpinner)
                changeSpinners(spinnersSaturday, disableSpinner)
                changeSpinners(spinnersSunday, disableSpinner)

                savePlanButton.text = "EDIT YOUR WORKOUT PLAN"
                savePlanButton.setBackgroundResource(R.drawable.custom_button_yellow)
            }
            else {
                binding.addExerciseMonday.visibility = View.VISIBLE
                binding.addExerciseTuesday.visibility = View.VISIBLE
                binding.addExerciseWednesday.visibility = View.VISIBLE
                binding.addExerciseThursday.visibility = View.VISIBLE
                binding.addExerciseFriday.visibility = View.VISIBLE
                binding.addExerciseSaturday.visibility = View.VISIBLE
                binding.addExerciseSunday.visibility = View.VISIBLE

                disableSpinner = false

                changeSpinners(spinnersMonday, disableSpinner)
                changeSpinners(spinnersTuesday, disableSpinner)
                changeSpinners(spinnersWednesday, disableSpinner)
                changeSpinners(spinnersThursday, disableSpinner)
                changeSpinners(spinnersFriday, disableSpinner)
                changeSpinners(spinnersSaturday, disableSpinner)
                changeSpinners(spinnersSunday, disableSpinner)

                savePlanButton.text = "SAVE YOUR WORKOUT PLAN"
                savePlanButton.setBackgroundResource(R.drawable.custom_button_red)
            }
        }

        return binding.root
    }

    private fun changeSpinners(spinners: MutableList<Spinner>, disable: Boolean) {
        for (spinner in spinners) {
            spinner.isEnabled = !disable
        }
    }

    private fun muscleGroupSelectionDialog(button: Button) {
        var selectedItems: Array<String>
        val checkedItems = booleanArrayOf(false, false, false, false, false, false, false, false, false, false, false)
        val muscleGroups = arrayOf("chest", "triceps", "biceps", "forearms", "back", "shoulders", "legs", "abs", "neck", "hips", "rest day")
        button.setOnClickListener {
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Select muscle groups")
            builder.setCancelable(false)
            builder.setMultiChoiceItems(muscleGroups, checkedItems) { dialog, index, isChecked ->
                checkedItems[index] = isChecked
            }
            builder.setPositiveButton("OK") { dialog, index ->
                selectedItems = muscleGroups.filterIndexed { index, _ -> checkedItems[index] }.toTypedArray()
                // serialize json string & save it to shared preferences
                val jsonMuscleGroups = Gson().toJson(selectedItems)
                with(sharedPrefs.edit()) {
                    val label = button.text.toString().toLowerCase() + "_muscle_groups"
                    if (selectedItems[0] == "rest day") {
                        button.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.rest, 0)
                    }
                    else {
                        button.setBackgroundResource(R.drawable.custom_button_green)
                        button.setTextColor(Color.WHITE)
                        button.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.muscle_small, 0)
                    }
                    putString(label, jsonMuscleGroups)
                    apply()
                }
            }
            builder.setNegativeButton("Cancel") { dialog, index ->
                dialog.dismiss()
            }
            val dialog = builder.create()
            dialog.show()
        }
    }

    private fun makeWorkoutPlan(day: String, spinners: MutableList<Spinner>, muscleGroups: MutableList<MuscleGroup>): WorkoutDay {
        val exercises = mutableListOf<Exercise>()
        for (spinner in spinners) {
            val exercise = Exercise(spinner.selectedItem.toString())
            exercises.add(exercise)
        }
        val workoutDay = WorkoutDay(day, muscleGroups, exercises)
        return workoutDay
    }

    // clicking the 'add' button dynamically adds elements on screen and updates their constraints
    private fun addExerciseSpinners(dayButton: Button, addButton: ImageButton, lastInputFieldId: Int): MutableList<Spinner> {
        val spinners = mutableListOf<Spinner>()
        var numSpinners = 0
        var lastId = lastInputFieldId
        addButton.setOnClickListener {

            findNavController().navigate(R.id.exercisesFragment)

            val constraintLayout = binding.layout

            val newSpinner = Spinner(context)

            // list of available exercises
            val newSpinnerData = arrayListOf("fly", "dumbbell fly", "bench press", "incline bench press", "decline bench press", "seated dumbbell press", "arnold press",
                                            "dips", "bench dips", "chest press", "dumbbell bench press", "chest push-up", "regular push-up", "close-grip push-up", "decline push-up",
                                            "pike push-up", "cable fly", "tricep pushdown", "lying barbell tricep extension", "tricep rope pushdown", "tricep kickback",
                                            "overhead dumbbell extension", "standing dumbbell curl", "barbell curl", "incline dumbbell curl", "hammer curl", "squat",
                                            "deadlift", "cable curl", "chin-up", "pull-up", "concentration curl", "preacher curl", "drag curl",
                                            "lat pull-down", "back extension", "barbell bent-over row", "dumbbell bent-over row", "seated cable row", "incline bench row",
                                            "plank", "decline crunch", "superman", "reverse fly", "forearm plank", "dumbbell pull-over", "hanging knee raise",
                                            "machine crunch", "crunch", "pallof press", "cable crunch", "russian twist", "ab roll-out", "ball pike", "dumbbell lateral raise",
                                            "cable lateral raise", "dumbbell upright row", "dumbbell front raise", "push press", "muscle-up", "barbell shrug", "dumbbell shrug",
                                            "cable face pull", "leg extension", "leg press", "dumbbell lunge", "leg curl", "seated calf raise", "standing calf raise",
                                            "lateral lunge", "step-up", "glute bridge", "hip thrust", "goblet squat", "split squat")

            newSpinnerData.sort()   // sorts alphabetically
            val newSpinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, newSpinnerData)
            newSpinner.adapter = newSpinnerAdapter

            newSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item)
            newSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    selectedExercise = parent?.getItemAtPosition(position).toString()
                    Log.i("selected exercise", selectedExercise)
                    // ensuring unique Spinner IDs
                    numSpinners++
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

            //newSpinner.hint = "${++numTextViews}. exercise"
            //newSpinner.setHintTextColor(Color.argb(200, 255, 255, 255))
            newSpinner.setBackgroundResource(R.drawable.custom_button)
            newSpinner.setPadding(10, 20, 20, 20)
            newSpinner.minimumWidth = 600
            //newSpinner.height = 120

            // ensuring unique input field IDs
            when (dayButton.text) {
                "MONDAY" -> {
                    newSpinner.id = numSpinners+1000
                }
                "TUESDAY" -> {
                    newSpinner.id = numSpinners+2000
                }
                "WEDNESDAY" -> {
                    newSpinner.id = numSpinners+3000
                }
                "THURSDAY" -> {
                    newSpinner.id = numSpinners+4000
                }
                "FRIDAY" -> {
                    newSpinner.id = numSpinners+5000
                }
                "SATURDAY" -> {
                    newSpinner.id = numSpinners+6000
                }
                "SUNDAY" -> {
                    newSpinner.id = numSpinners+7000
                }
            }
            //newSpinner.setTextColor(Color.WHITE)
            constraintLayout.addView(newSpinner)
            newSpinner.layoutParams = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
            )
            val constraintSet = ConstraintSet()
            constraintSet.clone(constraintLayout)
            constraintSet.connect(
                newSpinner.id,
                ConstraintSet.TOP,
                lastId,
                ConstraintSet.BOTTOM,
                40
            )
            constraintSet.connect(
                newSpinner.id,
                ConstraintSet.START,
                dayButton.id,
                ConstraintSet.START
            )
            constraintSet.connect(
                newSpinner.id,
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
                newSpinner.id,
                ConstraintSet.BOTTOM,
                40
            )
            constraintSet.applyTo(constraintLayout)
            lastId = newSpinner.id

            spinners.add(newSpinner)
        }

        return spinners
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getDate(): String {
        val currentDate = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
        return currentDate.format(formatter)
    }
}