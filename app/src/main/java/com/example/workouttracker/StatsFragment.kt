package com.example.workouttracker

import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.workouttracker.databinding.FragmentStatsBinding
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class StatsFragment : Fragment() {

    private lateinit var binding: FragmentStatsBinding
    private lateinit var pieChart: PieChart

    private lateinit var navigation: BottomNavigationView

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentStatsBinding.inflate(inflater, container, false)

        navigation = binding.bottomNavigation
        navigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.calendar -> {
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
                else -> true
            }
        }

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        var counterWeightlifting = 0
        var counterGymCardio = 0
        var counterRun = 0
        var counterSprint = 0
        var counterHike = 0
        var counterBicycle = 0

        val id = auth.currentUser?.uid
        if (id != null) {
            firestore.collection("users").document(id).get().addOnSuccessListener { user ->
                val workouts = user.get("workouts") as MutableList<HashMap<String, String>>
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
                val totalActivites = counterWeightlifting + counterGymCardio + counterRun + counterSprint + counterHike + counterBicycle
                pieChart = binding.piechart
                val entries = ArrayList<PieEntry>()
                entries.add(PieEntry(40f, "Weightlifting"))
                entries.add(PieEntry(60f, "Gym Cardio"))
                entries.add(PieEntry(counterRun/3f, "Run"))
                entries.add(PieEntry(counterSprint/3f, "Sprint"))
                entries.add(PieEntry(counterHike/3f, "Hike"))
                entries.add(PieEntry(counterBicycle/3f, "Bike"))
                val dataSet = PieDataSet(entries, "")
                dataSet.valueTextSize = 15f
                dataSet.valueTextColor = Color.WHITE

                val colors = ArrayList<Int>()
                colors.add(Color.RED)
                colors.add(Color.BLUE)
                colors.add(Color.YELLOW)
                colors.add(Color.GREEN)
                colors.add(Color.MAGENTA)
                colors.add(Color.CYAN)
                dataSet.colors = colors
                pieChart.holeRadius = 0f
                pieChart.setDrawSlicesUnderHole(false)
                val data = PieData(dataSet)
                pieChart.data = data

                val legend = pieChart.legend
                legend.textColor = Color.WHITE
                legend.textSize = 20f
                legend.isWordWrapEnabled = true
                legend.maxSizePercent = 0.5f
                legend.yOffset = 0f

                dataSet.setDrawIcons(true)
                dataSet.setDrawValues(false)

            }.addOnFailureListener {
                Log.i("current user", "/")
            }
        }




        /*barchart = binding.barchart
        val data = BarData(
            listOf(
                BarDataSet(
                    listOf(
                        BarEntry(0f, 0f),
                        BarEntry(1f, 1f),
                        BarEntry(2f, 2f),
                        BarEntry(3f, 3f)
                    ), "Data Set 1"
                )
            )
        )
        barchart.data = data*/

        return binding.root
    }
}