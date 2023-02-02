package com.example.workouttracker

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.navigation.fragment.findNavController
import com.example.workouttracker.databinding.FragmentStatsBinding
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.DefaultValueFormatter
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class StatsFragment : Fragment() {

    private lateinit var binding: FragmentStatsBinding
    private lateinit var pieChart: PieChart

    private lateinit var navigation: BottomNavigationView

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentStatsBinding.inflate(inflater, container, false)
        navigation = binding.bottomNavigation
        pieChart = binding.piechart

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        navigation.selectedItemId = R.id.stats

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



        val id = auth.currentUser?.uid
        if (id != null) {
            firestore.collection("users").document(id).get().addOnSuccessListener { user ->
                val workouts = user.get("workouts") as MutableList<HashMap<String, String>>
                setupPieChart()
                loadPieChartData(workouts)
            }.addOnFailureListener {
                Log.i("current user", "/")
            }
        }

        return binding.root
    }

    private fun setupPieChart() {
        pieChart.isDrawHoleEnabled = true
        pieChart.setUsePercentValues(true)
        pieChart.setEntryLabelTextSize(12F)
        pieChart.centerText = "Your workout categories"
        pieChart.setCenterTextSize(20F)
        pieChart.description.isEnabled = false

        // setup piechart legend
        val legend = pieChart.legend
        legend.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
        legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
        legend.orientation = Legend.LegendOrientation.VERTICAL
        legend.textColor = Color.WHITE
        legend.textSize = 17F
        legend.yOffset = 10F
        legend.setDrawInside(false)
        legend.isEnabled = true
    }

    private fun loadPieChartData(workouts: MutableList<HashMap<String, String>>) {
        var counterWeightlifting = 0
        var counterGymCardio = 0
        var counterRun = 0
        var counterSprint = 0
        var counterHike = 0
        var counterBicycle = 0

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

        // setup entries
        val totalActivites = counterWeightlifting + counterGymCardio + counterRun + counterSprint + counterHike + counterBicycle
        val entries = ArrayList<PieEntry>()
        if (counterWeightlifting/totalActivites.toFloat() > 0) {
            entries.add(PieEntry(counterWeightlifting/totalActivites.toFloat(), "Weightlifting"))
        }
        if (counterGymCardio/totalActivites.toFloat() > 0) {
            entries.add(PieEntry(counterGymCardio/totalActivites.toFloat(), "Gym Cardio"))
        }
        if (counterRun/totalActivites.toFloat() > 0) {
            entries.add(PieEntry(counterRun/totalActivites.toFloat(), "Run"))
        }
        if (counterSprint/totalActivites.toFloat() > 0) {
            entries.add(PieEntry(counterSprint/totalActivites.toFloat(), "Sprint"))
        }
        if (counterHike/totalActivites.toFloat() > 0) {
            entries.add(PieEntry(counterHike/totalActivites.toFloat(), "Hike"))
        }
        if (counterBicycle/totalActivites.toFloat() > 0) {
            entries.add(PieEntry(counterBicycle/totalActivites.toFloat(), "Bike"))
        }
        val dataSet = PieDataSet(entries, "")

        // setup colors
        val colors = ArrayList<Int>()
        for (color in ColorTemplate.MATERIAL_COLORS) {
            colors.add(color)
        }
        for (color in ColorTemplate.VORDIPLOM_COLORS) {
            colors.add(color)
        }
        dataSet.colors = colors

        val data = PieData(dataSet)
        data.setDrawValues(true)    // display actual values
        data.setValueFormatter(PercentFormatter(pieChart))  // format values to percentages
        dataSet.valueTextSize = 15f
        dataSet.valueTextColor = Color.WHITE

        pieChart.data = data
        pieChart.invalidate()   // updates chart

        // adding animation
        pieChart.animateY(1200, Easing.EaseInOutQuad)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getDate(): String {
        val currentDate = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
        return currentDate.format(formatter)
    }
}