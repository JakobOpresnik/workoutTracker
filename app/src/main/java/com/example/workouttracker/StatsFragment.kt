package com.example.workouttracker

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.navigation.fragment.findNavController
import com.example.workouttracker.databinding.FragmentStatsBinding
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.absoluteValue

enum class GraphDates {
    ALL, FILTERED
}

class StatsFragment : Fragment() {

    private lateinit var binding: FragmentStatsBinding
    private lateinit var lineChart: LineChart
    private lateinit var pieChart: PieChart
    private lateinit var saveFilterButton: ImageButton

    private lateinit var navigation: BottomNavigationView

    private lateinit var selectedMetric: String
    private lateinit var selectedTimeMetric: String

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
                                } else {
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
        val spinnerAdapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, spinnerData)
        spinner.adapter = spinnerAdapter

        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item)
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedMetric = parent?.getItemAtPosition(position).toString()
                Log.i("selected workout metric", selectedMetric)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // time metric filter
        val timeSpinner = binding.timeMetricsFilter
        val timeSpinnerData = arrayListOf("all time", "last week", "last month", "last year")
        val timeSpinnerAdapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, timeSpinnerData)
        timeSpinner.adapter = timeSpinnerAdapter

        timeSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item)
        timeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedTimeMetric = parent?.getItemAtPosition(position).toString()
                Log.i("selected time metric", selectedTimeMetric)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }


        val id = auth.currentUser?.uid
        if (id != null) {
            firestore.collection("users").document(id).get()
                .addOnSuccessListener { user ->
                    val workouts = user.get("workouts") as MutableList<HashMap<String, String>>
                    val firstWorkout = workouts[0]
                    val firstDate = firstWorkout["date"]
                    val dates = mutableListOf<String>()
                    for (workout in workouts) {
                        dates.add(workout["date"].toString())
                    }

                    selectedTimeMetric = "all time"
                    val datesData = getDatesData(selectedTimeMetric, GraphDates.FILTERED, firstDate!!)
                    val datesAll = getDatesData(selectedTimeMetric, GraphDates.ALL, firstDate)
                    lineChart.xAxis.valueFormatter = XAxisValueFormatter(datesData)

                    val entries = ArrayList<Entry>()
                    for ((index, date) in datesAll.withIndex()) {
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

                    styleLineChart(entries, Color.RED, Color.WHITE, Color.RED, 5F, 10F)

                    saveFilterButton.setOnClickListener {
                        val datesData = getDatesData(selectedTimeMetric, GraphDates.FILTERED, firstDate)
                        val datesAll = getDatesData(selectedTimeMetric, GraphDates.ALL, firstDate)
                        lineChart.xAxis.valueFormatter = XAxisValueFormatter(datesData)

                        val entries = ArrayList<Entry>()
                        for ((index, date) in datesAll.withIndex()) {
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
                        when (selectedMetric) {
                            "duration" -> {
                                styleLineChart(entries, Color.RED, Color.WHITE, Color.RED, 5F, 10F)
                            }
                            "motivation" -> {
                                styleLineChart(
                                    entries,
                                    Color.YELLOW,
                                    Color.WHITE,
                                    Color.YELLOW,
                                    5F,
                                    10F
                                )
                            }
                            "exhaustion" -> {
                                styleLineChart(
                                    entries,
                                    Color.GREEN,
                                    Color.WHITE,
                                    Color.GREEN,
                                    5F,
                                    10F
                                )
                            }
                        }
                    }
                }.addOnFailureListener {
                    Log.i("current user", "/")
                }
        }
    }

    private fun styleLineChart(
        entries: ArrayList<Entry>,
        lineColor: Int,
        textColor: Int,
        fillColor: Int,
        lineWidth: Float,
        textSize: Float
    ) {
        val dataSet = LineDataSet(entries, "")
        dataSet.color = lineColor
        dataSet.setDrawCircles(false)
        dataSet.setDrawCircleHole(false)
        dataSet.mode = LineDataSet.Mode.HORIZONTAL_BEZIER   // smoothing the graph line
        //dataSet.setCircleColor(Color.BLUE)
        //dataSet.circleRadius = 8F
        //dataSet.circleHoleRadius = 10F
        dataSet.lineWidth = lineWidth
        dataSet.valueTextSize = textSize
        dataSet.valueTextColor = textColor
        dataSet.setDrawFilled(true)
        dataSet.fillColor = fillColor
        val lineData = LineData(dataSet)
        lineData.setDrawValues(false)
        lineChart.data = lineData
        lineChart.description.text = ""
        /*lineChart.description.textColor = Color.WHITE
        lineChart.description.textSize = 15F
        lineChart.description.textAlign = Paint.Align.LEFT
        lineChart.description.yOffset = 10F*/
        lineChart.invalidate()
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
        val totalActivites =
            counterWeightlifting + counterGymCardio + counterRun + counterSprint + counterHike + counterBicycle
        val entries = ArrayList<PieEntry>()
        if (counterWeightlifting / totalActivites.toFloat() > 0) {
            entries.add(PieEntry(counterWeightlifting / totalActivites.toFloat(), "Weightlifting"))
        }
        if (counterGymCardio / totalActivites.toFloat() > 0) {
            entries.add(PieEntry(counterGymCardio / totalActivites.toFloat(), "Gym Cardio"))
        }
        if (counterRun / totalActivites.toFloat() > 0) {
            entries.add(PieEntry(counterRun / totalActivites.toFloat(), "Run"))
        }
        if (counterSprint / totalActivites.toFloat() > 0) {
            entries.add(PieEntry(counterSprint / totalActivites.toFloat(), "Sprint"))
        }
        if (counterHike / totalActivites.toFloat() > 0) {
            entries.add(PieEntry(counterHike / totalActivites.toFloat(), "Hike"))
        }
        if (counterBicycle / totalActivites.toFloat() > 0) {
            entries.add(PieEntry(counterBicycle / totalActivites.toFloat(), "Bike"))
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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getDatesData(filter: String, datesType: GraphDates, firstDate: String): MutableList<String> {
        val currentDate = LocalDate.now()
        lateinit var dates: MutableList<String>

        when (filter) {
            "last week" -> {
                val lastWeek = mutableListOf<String>()
                val lastWeekAll = mutableListOf<String>()

                // all dates from last week (properly formatted)
                for (i in 7 downTo 0) {
                    if (i % 2 == 0) {
                        val date = currentDate.minusDays(i.toLong())
                        val formatter = DateTimeFormatter.ofPattern("dd/MM")
                        val formattedDate = date.format(formatter)
                        lastWeek.add(formattedDate.toString())
                    } else {
                        lastWeek.add("")
                    }
                }
                for (i in 7 downTo 0) {
                    val date = currentDate.minusDays(i.toLong())
                    val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
                    val formattedDate = date.format(formatter)
                    lastWeekAll.add(formattedDate.toString())
                }
                if (datesType == GraphDates.ALL) {
                    dates = lastWeekAll
                } else {
                    dates = lastWeek
                }
            }
            "last month" -> {
                val lastMonth = mutableListOf<String>()
                val lastMonthAll = mutableListOf<String>()
                for (i in 30 downTo 0) {
                    if (i % 10 == 0) {
                        val date = currentDate.minusDays(i.toLong())
                        val formatter = DateTimeFormatter.ofPattern("dd/MM")
                        val formattedDate = date.format(formatter)
                        lastMonth.add(formattedDate.toString())
                    } else {
                        lastMonth.add("")
                    }
                }
                for (i in 30 downTo 0) {
                    val date = currentDate.minusDays(i.toLong())
                    val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
                    val formattedDate = date.format(formatter)
                    lastMonthAll.add(formattedDate.toString())
                }
                if (datesType == GraphDates.ALL) {
                    dates = lastMonthAll
                } else {
                    dates = lastMonth
                }
            }
            "last year" -> {
                val lastYear = mutableListOf<String>()
                val lastYearAll = mutableListOf<String>()
                for (i in 366 downTo 0) {
                    val date = currentDate.minusDays(i.toLong())
                    val formatter = DateTimeFormatter.ofPattern("dd/MM")
                    val formattedDate = date.format(formatter)
                    if (formattedDate == "01/01") {
                        lastYear.add(formattedDate.toString())
                    } else {
                        lastYear.add("")
                    }
                }
                for (i in 365 downTo 0) {
                    val date = currentDate.minusDays(i.toLong())
                    val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
                    val formattedDate = date.format(formatter)
                    lastYearAll.add(formattedDate.toString())
                }
                if (datesType == GraphDates.ALL) {
                    dates = lastYearAll
                } else {
                    dates = lastYear
                }
            }
            "all time" -> {
                val allTimeDates = mutableListOf<String>()
                val allTimeDatesAll = mutableListOf<String>()

                val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
                val originalDateFormat = LocalDate.parse(firstDate, formatter)
                val daysFromFirstWorkout = currentDate.until(originalDateFormat, ChronoUnit.DAYS).toInt().absoluteValue
                for (i in daysFromFirstWorkout downTo 0) {
                    if (i == daysFromFirstWorkout || i == daysFromFirstWorkout/2) {
                        val date = currentDate.minusDays(i.toLong())
                        val formatter = DateTimeFormatter.ofPattern("dd/MM")
                        val formattedDate = date.format(formatter)
                        allTimeDates.add(formattedDate.toString())
                    } else {
                        val date = currentDate.minusDays(i.toLong())
                        val formatter = DateTimeFormatter.ofPattern("dd/MM")
                        val formattedDate = date.format(formatter)
                        if (date == currentDate) {
                            allTimeDates.add(formattedDate.toString())
                        }
                        else {
                            allTimeDates.add("")
                        }
                    }
                }
                for (i in daysFromFirstWorkout downTo 0) {
                    val date = currentDate.minusDays(i.toLong())
                    val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
                    val formattedDate = date.format(formatter)
                    allTimeDatesAll.add(formattedDate.toString())
                }
                if (datesType == GraphDates.ALL) {
                    dates = allTimeDatesAll
                } else {
                    dates = allTimeDates
                }
            }
        }
        return dates
    }
}

class XAxisValueFormatter(private val values: List<String>) : ValueFormatter() {
    override fun getFormattedValue(value: Float): String {
        return values[value.toInt() % values.size]
    }
}