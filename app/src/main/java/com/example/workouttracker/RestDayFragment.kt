package com.example.workouttracker

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.workouttracker.databinding.FragmentRestDayBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class RestDayFragment : Fragment() {

    private lateinit var binding: FragmentRestDayBinding
    private lateinit var navigation: BottomNavigationView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRestDayBinding.inflate(inflater, container, false)

        navigation = binding.bottomNavigation
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

        return binding.root
    }
}