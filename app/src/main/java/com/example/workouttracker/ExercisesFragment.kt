package com.example.workouttracker

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.example.workouttracker.databinding.FragmentExercisesBinding

class ExercisesFragment : DialogFragment() {

    private lateinit var binding: FragmentExercisesBinding
    private lateinit var title: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentExercisesBinding.inflate(inflater, container, false)

        title = binding.title
        title.text = arguments?.getString("day")

        return binding.root
    }
}