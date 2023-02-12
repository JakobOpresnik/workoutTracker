package com.example.workouttracker

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageButton
import androidx.annotation.RequiresApi
import androidx.navigation.fragment.findNavController
import com.example.workouttracker.databinding.FragmentStatsBinding
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.E

class StatsFragment : Fragment() {

    private lateinit var binding: FragmentStatsBinding
    private lateinit var lineChart: LineChart
    private lateinit var pieChart: PieChart
    private lateinit var saveFilterButton: ImageButton

    private lateinit var navigation: BottomNavigationView

    private lateinit var selectedMetric: String

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentStatsBinding.inflate(inflater, container, false)
        navigation = binding.bottomNavigation
        lineChart = binding.linechart
        pieChart = binding.piechart
        saveFilterButton = binding.saveMetricsFilter

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

                // display line chart
                setupLineChart()
                loadLineChartData()

                // display pie chart
                setupPieChart()
                loadPieChartData(workouts)

            }.addOnFailureListener {
                Log.i("current user", "/")
            }
        }

        return binding.root
    }

    private fun setupLineChart() {
        lineChart.xAxis.isEnabled = true
        lineChart.axisLeft.isEnabled = true
        lineChart.axisRight.isEnabled = true
        lineChart.xAxis.axisLineColor = Color.WHITE
        lineChart.xAxis.textColor = Color.WHITE
        lineChart.axisLeft.axisLineColor = Color.WHITE
        lineChart.axisLeft.textColor = Color.WHITE
        lineChart.axisRight.axisLineColor = Color.WHITE
        lineChart.axisRight.textColor = Color.WHITE

        lineChart.legend.isEnabled = false
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadLineChartData() {

        // workout metric filter to display on graph (line chart)
        val spinner = binding.metricsFilter
        val spinnerData = arrayListOf("duration", "motivation", "exhaustion")
        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, spinnerData)
        spinner.adapter = spinnerAdapter

        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item)
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedMetric = parent?.getItemAtPosition(position).toString()
                Log.i("selected workout metric", selectedMetric)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        val currentDate = LocalDate.now()
        val lastMonth = mutableListOf<String>()
        val lastMonthAll = mutableListOf<String>()

        // all dates from last month (properly formatted)
        for (i in 30 downTo 0) {
            if (i % 10 == 0) {
                val date = currentDate.minusDays(i.toLong())
                val formatter = DateTimeFormatter.ofPattern("dd/MM")
                val formattedDate = date.format(formatter)
                lastMonth.add(formattedDate.toString())
            }
            else {
                lastMonth.add("")
            }
        }

        for (i in 30 downTo 0) {
            val date = currentDate.minusDays(i.toLong())
            val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
            val formattedDate = date.format(formatter)
            lastMonthAll.add(formattedDate.toString())
        }
        lineChart.xAxis.valueFormatter = XAxisValueFormatter(lastMonth)

        val id = auth.currentUser?.uid
        if (id != null) {
            firestore.collection("users").document(id).get()
                .addOnSuccessListener { user ->
                    val workouts = user.get("workouts") as MutableList<HashMap<String, String>>
                    val dates = mutableListOf<String>()
                    for (workout in workouts) {
                        dates.add(workout["date"].toString())
                    }

                    val entries = ArrayList<Entry>()
                    for ((index, date) in lastMonthAll.withIndex()) {
                        if (dates.contains(date)) {
                            var value = 0
                            for (workout in workouts) {
                                if (workout["date"] == date) {
                                    value += workout["duration"].toString().toInt()
                                }
                            }
                            entries.add(Entry(index.toFloat(), value.toFloat()))
                        } else {
                            entries.add(Entry(index.toFloat(), 0F))
                        }
                    }
                    val dataSet = LineDataSet(entries, "LineChart")
                    dataSet.color = Color.CYAN
                    dataSet.setCircleColor(Color.BLUE)
                    dataSet.setDrawCircles(true)
                    dataSet.setDrawCircleHole(true)
                    dataSet.circleRadius = 8F
                    dataSet.circleHoleRadius = 10F
                    dataSet.lineWidth = 5F
                    dataSet.valueTextSize = 10F
                    dataSet.valueTextColor = Color.WHITE
                    dataSet.setDrawFilled(true)
                    val lineData = LineData(dataSet)
                    lineChart.data = lineData
                    lineChart.invalidate()

                    saveFilterButton.setOnClickListener {
                        val entries = ArrayList<Entry>()
                        for ((index, date) in lastMonthAll.withIndex()) {
                            if (dates.contains(date)) {
                                var value = 0
                                for (workout in workouts) {
                                    if (workout["date"] == date) {
                                        value += when (selectedMetric) {
                                            "duration" -> {
                                                workout["duration"].toString().toInt()
                                            }
                                            "motivation" -> {
                                                workout["motivation"].toString().toInt()
                                            }
                                            "exhaustion" -> {
                                                workout["exhaustion"].toString().toInt()
                                            }
                                            else -> {
                                                workout["duration"].toString().toInt()
                                            }
                                        }
                                    }
                                }
                                entries.add(Entry(index.toFloat(), value.toFloat()))
                            } else {
                                entries.add(Entry(index.toFloat(), 0F))
                            }
                        }
                        val dataSet = LineDataSet(entries, "LineChart")
                        dataSet.color = Color.CYAN
                        dataSet.setCircleColor(Color.BLUE)
                        dataSet.setDrawCircles(true)
                        dataSet.setDrawCircleHole(true)
                        dataSet.circleRadius = 8F
                        dataSet.circleHoleRadius = 10F
                        dataSet.lineWidth = 5F
                        dataSet.valueTextSize = 10F
                        dataSet.valueTextColor = Color.WHITE
                        dataSet.setDrawFilled(true)
                        val lineData = LineData(dataSet)
                        lineChart.data = lineData
                        lineChart.invalidate()
                    }
                }.addOnFailureListener {
                    Log.i("current user", "/")
                }
        }
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

class XAxisValueFormatter(private val values: List<String>) : ValueFormatter() {
    override fun getFormattedValue(value: Float): String {
        return values[value.toInt() % values.size]
    }
}