package com.example.workouttracker

import android.media.Image
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import androidx.navigation.fragment.findNavController
import com.example.workouttracker.databinding.FragmentChestExercisesBinding

class ChestExercisesFragment : Fragment() {

    private lateinit var binding: FragmentChestExercisesBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChestExercisesBinding.inflate(inflater, container, false)

        val addedExercises = arrayListOf<String>()

        val buttons = mutableListOf<ImageButton>()
        buttons.add(binding.benchPress)
        buttons.add(binding.chestPress)
        buttons.add(binding.inclineBenchPress)
        buttons.add(binding.diamondPushUp)
        buttons.add(binding.dips)
        buttons.add(binding.dumbbellBenchPress)
        buttons.add(binding.dumbbellFly)
        buttons.add(binding.machineChestFly)

        for (button in buttons) {
            var clicked = false
            button.setOnClickListener {
                val exerciseName = resources.getResourceName(button.id)
                if (!clicked) {
                    button.setBackgroundResource(R.drawable.border)
                    addedExercises.add(exerciseName)
                }
                else {
                    button.setBackgroundResource(R.drawable.custom_image_button)
                    addedExercises.remove(exerciseName)
                }
                clicked = !clicked
            }
        }

        val saveButton = binding.saveExercises
        saveButton.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("exercises", addedExercises.toString())
            findNavController().navigate(R.id.exercisesFragment, bundle)
        }

        return binding.root
    }
}