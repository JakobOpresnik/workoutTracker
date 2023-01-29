package com.example.workouttracker

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.SubMenu
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import com.example.workouttracker.databinding.FragmentLobbyBinding

class LobbyFragment : Fragment() {

    private lateinit var binding: FragmentLobbyBinding
    private lateinit var anotherWorkoutButton: Button
    private lateinit var calendarButton: Button
    private lateinit var statsButton: Button
    private lateinit var title: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLobbyBinding.inflate(inflater, container, false)
        anotherWorkoutButton = binding.anotherWorkoutButton
        calendarButton = binding.calendarButton
        statsButton = binding.statsButton
        title = binding.title

        val numberTodayWorkouts = arguments?.getString("today_workout_counter")

        val titleText = title.text
        val index = titleText.lastIndexOf(" ")
        val substringTitle = titleText.substring(0, index)

        val substring = when (numberTodayWorkouts) {
            "1" -> {
                "once today!"
            }
            "2" -> {
                "twice today!"
            }
            else -> {
                "$numberTodayWorkouts times today!"
            }
        }
        val newTitleText = "$substringTitle $substring"
        title.text = newTitleText

        anotherWorkoutButton.setOnClickListener {
            findNavController().navigate(R.id.homeFragment)
        }

        calendarButton.setOnClickListener {
            findNavController().navigate(R.id.calendarFragment)
        }

        statsButton.setOnClickListener {
            findNavController().navigate(R.id.statsFragment)
        }

        return binding.root
    }
}