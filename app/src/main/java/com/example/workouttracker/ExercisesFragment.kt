package com.example.workouttracker

//noinspection SuspiciousImport
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import com.example.workouttracker.R
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.navigation.fragment.findNavController
import com.example.workouttracker.databinding.FragmentExercisesBinding


class ExercisesFragment : Fragment() {

    private lateinit var binding: FragmentExercisesBinding
    private lateinit var selectedMuscleGroup: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentExercisesBinding.inflate(inflater, container, false)

        // initial value
        //selectedMuscleGroup = "all"

        val chestButton = binding.chestButton
        chestButton.setOnClickListener {
            findNavController().navigate(R.id.chestExercisesFragment)
        }

        var selectedChestExercises = mutableListOf<String>()
        var chestExercises = arguments?.getString("exercises")
        Log.i("chest exercises", chestExercises.toString())
        val commas = chestExercises.toString().count { it == ',' }
        val numExercises = commas + 1

        for (i in 0 until numExercises) {
            val firstIndex = chestExercises.toString().indexOf("/")
            var endIndex = chestExercises.toString().indexOf(",")
            if (endIndex == -1) {
                endIndex = chestExercises.toString().indexOf("]")
            }
            if (firstIndex != -1 && endIndex != -1) {
                val exercise = chestExercises.toString().substring(firstIndex+1, endIndex)
                selectedChestExercises.add(exercise)
                Log.i("exercise", exercise)
                if (endIndex+2 < chestExercises.toString().length) {
                    val remaining = chestExercises.toString().substring(endIndex+2, chestExercises.toString().length)
                    chestExercises = remaining
                }
            }
        }

        val saveButton = binding.saveExercises
        saveButton.setOnClickListener {
            findNavController().navigate(R.id.planFragment)
        }

        // muscle group filter
        /*val muscleGroupSpinner = binding.muscleGroupFilter
        val spinnerData = arrayListOf("all", "chest", "triceps", "biceps", "forearms", "back", "shoulders", "legs", "abs", "neck", "hips")
        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, spinnerData)
        muscleGroupSpinner.adapter = spinnerAdapter

        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item)
        muscleGroupSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedMuscleGroup = parent?.getItemAtPosition(position).toString()
                Log.i("selected muscle group", selectedMuscleGroup)

                when (selectedMuscleGroup) {
                    "chest" -> {
                        val constraintLayout = binding.layout

                        val exerciseImageButton = ImageButton(context)
                        exerciseImageButton.id = View.generateViewId()
                        val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.bench_press)
                        val scaledDrawable = BitmapDrawable(resources, Bitmap.createScaledBitmap((drawable as BitmapDrawable).bitmap, 150, 150, true))
                        exerciseImageButton.setImageDrawable(scaledDrawable)

                        constraintLayout.addView(exerciseImageButton)
                        exerciseImageButton.layoutParams = ConstraintLayout.LayoutParams(
                            ConstraintLayout.LayoutParams.WRAP_CONTENT,
                            ConstraintLayout.LayoutParams.WRAP_CONTENT
                        )
                        val constraintSet = ConstraintSet()
                        constraintSet.clone(constraintLayout)
                        constraintSet.connect(
                            exerciseImageButton.id,
                            ConstraintSet.TOP,
                            binding.muscleGroupFilter.id,
                            ConstraintSet.BOTTOM,
                            40
                        )
                        /*constraintSet.connect(
                            exerciseImageButton.id,
                            ConstraintSet.START,
                            binding.muscleGroupFilter.id,
                            ConstraintSet.START,
                        )
                        constraintSet.connect(
                            exerciseImageButton.id,
                            ConstraintSet.END,
                            binding.muscleGroupFilter.id,
                            ConstraintSet.END,
                        )*/

                        constraintSet.applyTo(constraintLayout)

                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }*/

        //imageListenerEvent(binding.squat)

        return binding.root
    }

    private fun imageListenerEvent(imageButton: ImageButton) {
        // get exercise name from file name
        var exerciseName = resources.getResourceName(imageButton.id)
        exerciseName = exerciseName.substring(exerciseName.lastIndexOf("/") + 1)
        Log.i("filename", exerciseName)
        imageButton.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("exercise", exerciseName)
            findNavController().navigate(com.example.workouttracker.R.id.planFragment, bundle)
        }
    }
}