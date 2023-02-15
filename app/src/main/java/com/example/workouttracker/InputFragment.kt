package com.example.workouttracker

//noinspection SuspiciousImport
import android.R
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.workouttracker.databinding.FragmentInputBinding
import com.example.workouttracker.utils.Workout
import com.example.workouttracker.utils.WorkoutType
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*


class InputFragment : Fragment() {

    private lateinit var binding: FragmentInputBinding
    private lateinit var motivationValue: TextView
    private lateinit var exhaustionValue: TextView
    private lateinit var motivationSeekBar: SeekBar
    private lateinit var exhaustionSeekBar: SeekBar
    private lateinit var saveWorkoutButton: Button

    private lateinit var selectedWorkoutType: WorkoutType

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentInputBinding.inflate(inflater, container, false)

        val durationInput = binding.durationInput
        motivationValue = binding.motivationValue
        exhaustionValue = binding.exhaustionValue
        motivationSeekBar = binding.motivationSeekbar
        exhaustionSeekBar = binding.exhaustionSeekbar
        saveWorkoutButton = binding.saveWorkout

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val spinner = binding.workoutTypePicker
        val spinnerData = arrayListOf("Weightlifting", "Gym Cardio", "Run", "Sprint", "Hike", "Bicycle Ride")
        val adapter = ArrayAdapter(requireContext(), R.layout.simple_spinner_item, spinnerData)
        spinner.adapter = adapter

        adapter.setDropDownViewResource(R.layout.simple_spinner_item)
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val buffer = parent?.getItemAtPosition(position).toString().replace(" ", "_").toUpperCase()
                selectedWorkoutType = WorkoutType.valueOf(buffer)
                Log.i("selected workout type", selectedWorkoutType.toString())
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        motivationSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                motivationValue.text = progress.toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        exhaustionSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                exhaustionValue.text = progress.toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        saveWorkoutButton.setOnClickListener {
            val id = auth.currentUser?.uid
            Log.i("id", id.toString())
            if (id != null) {
                firestore.collection("users").document(id).get().addOnSuccessListener { user ->
                    val startTime = getStartTime()
                    val date = getDate()
                    if (durationInput.text.toString() != "") {
                        val duration = durationInput.text.toString().toInt()
                        val motivation = motivationValue.text.toString().toInt()
                        val exhaustion = exhaustionValue.text.toString().toInt()
                        val workout = Workout(selectedWorkoutType, startTime, date, duration, motivation, exhaustion)

                        firestore.collection("users").document(id).update("workouts", FieldValue.arrayUnion(workout))
                            .addOnSuccessListener {
                                Log.i("workout", "workout successfully added to user")
                            }.addOnFailureListener {
                                Log.i("workout", "workout failed to be added to user")
                            }
                        findNavController().navigate(com.example.workouttracker.R.id.lobbyFragment)
                    }
                    else {
                        durationInput.error = "Enter duration"
                        //Toast.makeText(context, "missing duration", Toast.LENGTH_SHORT).show()
                    }
                }.addOnFailureListener {
                    Log.i("current user", "/")
                }
            }
            else {
                Toast.makeText(context, "nobody is currently logged in", Toast.LENGTH_SHORT).show()
                Log.i("current user", "nobody is logged in")
            }

        }

        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getDate(): String {
        val currentDate = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
        return currentDate.format(formatter)
    }

    private fun getStartTime(): String {
        val timepicker = binding.timePicker
        val hour = timepicker.hour
        val minute = timepicker.minute
        if (minute < 10) {
            return "$hour : 0$minute"
        }
        return "$hour : $minute"
    }
}