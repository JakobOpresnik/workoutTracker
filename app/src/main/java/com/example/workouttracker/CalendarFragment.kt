package com.example.workouttracker

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CalendarView
import androidx.navigation.fragment.findNavController
import com.example.workouttracker.databinding.FragmentCalendarBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class CalendarFragment : Fragment() {

    private lateinit var binding: FragmentCalendarBinding
    private lateinit var calendar: CalendarView
    private lateinit var navigation: BottomNavigationView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCalendarBinding.inflate(inflater, container, false)
        calendar = binding.calendar
        navigation = binding.bottomNavigation

        navigation.selectedItemId = R.id.calendar

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
                else -> false
            }
        }

        calendar.selectedWeekBackgroundColor = Color.WHITE

        return binding.root
    }
}