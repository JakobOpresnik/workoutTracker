package com.example.workouttracker

import android.app.AlertDialog
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
import com.github.mikephil.charting.components.LegendEntry
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
                    findNavController().navigate(R.id.planFragment)
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

        val legend = lineChart.legend

        legend.isEnabled = true
        legend.textColor = Color.WHITE
        legend.textSize = 15F
        legend.form = Legend.LegendForm.CIRCLE
        legend.formSize = 10F
        legend.xEntrySpace = 15F
        legend.yEntrySpace = 15F
        legend.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
        legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
        legend.orientation = Legend.LegendOrientation.HORIZONTAL

        lineChart.legend.setDrawInside(false)

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadLineChartData() {

        val id = auth.currentUser?.uid
        val colors = arrayListOf(Color.RED, Color.YELLOW, Color.GREEN)

        // workout metric filter to display on graph (line chart)
        /*val spinner = binding.metricsFilter
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
        }*/

        var selectedItems: Array<String>
        val checkedItems = booleanArrayOf(true, false, false)
        val metrics = arrayOf("duration", "motivation", "exhaustion")
        val metricsFilterMultiple = binding.metricsFilterMultiple
        metricsFilterMultiple.setOnClickListener {
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Select metrics")
            builder.setCancelable(false)
            builder.setMultiChoiceItems(metrics, checkedItems) { dialog, index, isChecked ->
                checkedItems[index] = isChecked
            }
            builder.setPositiveButton("OK") { dialog, index ->
                selectedItems = metrics.filterIndexed { index, _ -> checkedItems[index] }.toTypedArray()
                for (i in selectedItems) {
                    Log.i("selected items", i)
                }
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

                            // graph entries are setup (updated) when clicking 'OK' in the multiple choice dialog
                            setupEntryValues(workouts, dates, firstDate!!, selectedItems, colors)
                            // and when clicking on filter save button
                            saveFilterButton.setOnClickListener {
                                setupEntryValues(workouts, dates, firstDate, selectedItems, colors)
                            }
                        }.addOnFailureListener {
                            Log.i("current user", "/")
                        }
                }
            }
            builder.setNegativeButton("Cancel") { dialog, index ->
                dialog.dismiss()
            }
            val dialog = builder.create()
            dialog.show()
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

                    // default time filter is 'all time'
                    selectedTimeMetric = "all time"
                    val datesData = getDatesData(selectedTimeMetric, GraphDates.FILTERED, firstDate!!)
                    val datesAll = getDatesData(selectedTimeMetric, GraphDates.ALL, firstDate)
                    lineChart.xAxis.valueFormatter = XAxisValueFormatter(datesData)

                    // default metric is 'duration'
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
                    val dataSet = arrayListOf(entries)
                    selectedItems = arrayOf("duration")
                    styleLineChart(selectedItems, dataSet, colors, Color.WHITE, colors, 5F, 10F)

                    saveFilterButton.setOnClickListener {
                        setupEntryValues(workouts, dates, firstDate, selectedItems, colors)
                    }
                }.addOnFailureListener {
                    Log.i("current user", "/")
                }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupEntryValues(workouts: MutableList<HashMap<String, String>>, dates: MutableList<String>, firstDate: String, selectedItems: Array<String>, colors: ArrayList<Int>) {
        val datesData = getDatesData(selectedTimeMetric, GraphDates.FILTERED, firstDate)
        val datesAll = getDatesData(selectedTimeMetric, GraphDates.ALL, firstDate)
        lineChart.xAxis.valueFormatter = XAxisValueFormatter(datesData)

        val entries = ArrayList<Entry>()
        val entries2 = ArrayList<Entry>()
        val entries3 = ArrayList<Entry>()
        for ((index, date) in datesAll.withIndex()) {
            if (dates.contains(date)) {
                var value = 0
                var value2 = 0
                var value3 = 0
                for (workout in workouts) {
                    if (workout["date"] == date) {
                        // when no metric is selected
                        if (selectedItems.isEmpty()) {
                            value += workout["duration"].toString().toInt()
                        }
                        else if (selectedItems.size == 1) {
                            value += when (selectedItems[0]) {
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
                        else if (selectedItems.size == 2) {
                            value += when (selectedItems[0]) {
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
                            value2 += when (selectedItems[1]) {
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
                        else if (selectedItems.size == 3) {
                            value += when (selectedItems[0]) {
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
                            value2 += when (selectedItems[1]) {
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
                            value3 += when (selectedItems[2]) {
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
                }
                when (selectedItems.size) {
                    1 -> {
                        entries.add(Entry(index.toFloat(), value.toFloat()))
                    }
                    2 -> {
                        entries.add(Entry(index.toFloat(), value.toFloat()))
                        entries2.add(Entry(index.toFloat(), value2.toFloat()))
                    }
                    3 -> {
                        entries.add(Entry(index.toFloat(), value.toFloat()))
                        entries2.add(Entry(index.toFloat(), value2.toFloat()))
                        entries3.add(Entry(index.toFloat(), value3.toFloat()))
                    }
                }
            } else {
                when (selectedItems.size) {
                    1 -> {
                        entries.add(Entry(index.toFloat(), 0F))
                    }
                    2 -> {
                        entries.add(Entry(index.toFloat(), 0F))
                        entries2.add(Entry(index.toFloat(), 0F))
                    }
                    3 -> {
                        entries.add(Entry(index.toFloat(), 0F))
                        entries2.add(Entry(index.toFloat(), 0F))
                        entries3.add(Entry(index.toFloat(), 0F))
                    }
                }
            }
        }
        when (selectedItems.size) {
            1 -> {
                val dataSets = arrayListOf(entries)
                styleLineChartForMetric(selectedItems[0], selectedItems, dataSets, colors)
            }
            2 -> {
                val dataSets = arrayListOf(entries, entries2)
                for (item in selectedItems) {
                    styleLineChartForMetric(item, selectedItems, dataSets, colors)
                }
            }
            3 -> {
                val dataSets = arrayListOf(entries, entries2, entries3)
                for (item in selectedItems) {
                    styleLineChartForMetric(item, selectedItems, dataSets, colors)
                }
            }
        }
    }

    private fun styleLineChartForMetric(metric: String, selectedItems: Array<String>, dataSets: ArrayList<ArrayList<Entry>>, colors: ArrayList<Int>) {
        when (metric) {
            "duration" -> {
                styleLineChart(selectedItems, dataSets, colors, Color.WHITE, colors, 5F, 10F)
            }
            "motivation" -> {
                styleLineChart(selectedItems, dataSets, colors, Color.WHITE, colors, 5F, 10F)
            }
            "exhaustion" -> {
                styleLineChart(selectedItems, dataSets, colors, Color.WHITE, colors, 5F, 10F)
            }
        }
    }

    private fun styleLineChart(
        metrics: Array<String>,
        dataSets: ArrayList<ArrayList<Entry>>,
        lineColors: ArrayList<Int>,
        textColor: Int,
        fillColors: ArrayList<Int>,
        lineWidth: Float,
        textSize: Float
    ) {
        val dataSet: LineDataSet
        val dataSet2: LineDataSet
        val dataSet3: LineDataSet
        when (dataSets.size) {
            1 -> {
                dataSet = LineDataSet(dataSets[0], "")

                when (metrics[0]) {
                    "duration" -> {
                        dataSet.color = lineColors[0]
                        dataSet.fillColor = fillColors[0]

                        val legendEntries = ArrayList<LegendEntry>()
                        val durationLegendEntry = LegendEntry()
                        setupLineChartLegend(legendEntries, durationLegendEntry, "duration", Color.RED)
                    }
                    "motivation" -> {
                        dataSet.color = lineColors[1]
                        dataSet.fillColor = fillColors[1]

                        val legendEntries = ArrayList<LegendEntry>()
                        val motivationLegendEntry = LegendEntry()
                        setupLineChartLegend(legendEntries, motivationLegendEntry, "motivation", Color.YELLOW)
                    }
                    "exhaustion" -> {
                        dataSet.color = lineColors[2]
                        dataSet.fillColor = fillColors[2]

                        val legendEntries = ArrayList<LegendEntry>()
                        val exhaustionLegendEntry = LegendEntry()
                        setupLineChartLegend(legendEntries, exhaustionLegendEntry, "exhaustion", Color.GREEN)
                    }
                }

                setupDatasetStyle(dataSet, lineWidth, textSize, textColor)

                val lineData = LineData(dataSet)
                lineData.setDrawValues(false)
                lineChart.data = lineData
                lineChart.description.text = ""
                lineChart.axisRight.isEnabled = false
                lineChart.invalidate()
            }
            2 -> {
                dataSet = LineDataSet(dataSets[0], "")
                dataSet2 = LineDataSet(dataSets[1], "")

                val legendEntries = ArrayList<LegendEntry>()

                when (metrics[0]) {
                    "duration" -> {
                        dataSet.color = lineColors[0]
                        dataSet.fillColor = fillColors[0]

                        val durationLegendEntry = LegendEntry()
                        setupLineChartLegend(legendEntries, durationLegendEntry, "duration", Color.RED)
                    }
                    "motivation" -> {
                        dataSet.color = lineColors[1]
                        dataSet.fillColor = fillColors[1]

                        val motivationLegendEntry = LegendEntry()
                        setupLineChartLegend(legendEntries, motivationLegendEntry, "motivation", Color.YELLOW)
                    }
                    "exhaustion" -> {
                        dataSet.color = lineColors[2]
                        dataSet.fillColor = fillColors[2]

                        val exhaustionLegendEntry = LegendEntry()
                        setupLineChartLegend(legendEntries, exhaustionLegendEntry, "exhaustion", Color.GREEN)
                    }
                }
                when (metrics[1]) {
                    "duration" -> {
                        dataSet2.color = lineColors[0]
                        dataSet2.fillColor = fillColors[0]

                        val durationLegendEntry = LegendEntry()
                        setupLineChartLegend(legendEntries, durationLegendEntry, "duration", Color.RED)
                    }
                    "motivation" -> {
                        dataSet2.color = lineColors[1]
                        dataSet2.fillColor = fillColors[1]

                        val motivationLegendEntry = LegendEntry()
                        setupLineChartLegend(legendEntries, motivationLegendEntry, "motivation", Color.YELLOW)
                    }
                    "exhaustion" -> {
                        dataSet2.color = lineColors[2]
                        dataSet2.fillColor = fillColors[2]

                        val exhaustionLegendEntry = LegendEntry()
                        setupLineChartLegend(legendEntries, exhaustionLegendEntry, "exhaustion", Color.GREEN)
                    }
                }

                setupDatasetStyle(dataSet, lineWidth, textSize, textColor)
                setupDatasetStyle(dataSet2, lineWidth, textSize, textColor)

                val lineData = LineData(dataSet, dataSet2)
                lineData.setDrawValues(false)
                lineChart.data = lineData
                lineChart.description.text = ""
                lineChart.axisRight.isEnabled = false
                lineChart.invalidate()
            }
            3 -> {
                dataSet = LineDataSet(dataSets[0], "")
                dataSet2 = LineDataSet(dataSets[1], "")
                dataSet3 = LineDataSet(dataSets[2], "")

                val legendEntries = ArrayList<LegendEntry>()

                when (metrics[0]) {
                    "duration" -> {
                        dataSet.color = lineColors[0]
                        dataSet.fillColor = fillColors[0]

                        val durationLegendEntry = LegendEntry()
                        setupLineChartLegend(legendEntries, durationLegendEntry, "duration", Color.RED)
                    }
                    "motivation" -> {
                        dataSet.color = lineColors[1]
                        dataSet.fillColor = fillColors[1]

                        val motivationLegendEntry = LegendEntry()
                        setupLineChartLegend(legendEntries, motivationLegendEntry, "motivation", Color.YELLOW)
                    }
                    "exhaustion" -> {
                        dataSet.color = lineColors[2]
                        dataSet.fillColor = fillColors[2]

                        val exhaustionLegendEntry = LegendEntry()
                        setupLineChartLegend(legendEntries, exhaustionLegendEntry, "exhaustion", Color.GREEN)
                    }
                }
                when (metrics[1]) {
                    "duration" -> {
                        dataSet2.color = lineColors[0]
                        dataSet2.fillColor = fillColors[0]

                        val durationLegendEntry = LegendEntry()
                        setupLineChartLegend(legendEntries, durationLegendEntry, "duration", Color.RED)
                    }
                    "motivation" -> {
                        dataSet2.color = lineColors[1]
                        dataSet2.fillColor = fillColors[1]

                        val motivationLegendEntry = LegendEntry()
                        setupLineChartLegend(legendEntries, motivationLegendEntry, "motivation", Color.YELLOW)
                    }
                    "exhaustion" -> {
                        dataSet2.color = lineColors[2]
                        dataSet2.fillColor = fillColors[2]

                        val exhaustionLegendEntry = LegendEntry()
                        setupLineChartLegend(legendEntries, exhaustionLegendEntry, "exhaustion", Color.GREEN)
                    }
                }
                when (metrics[2]) {
                    "duration" -> {
                        dataSet3.color = lineColors[0]
                        dataSet3.fillColor = fillColors[0]

                        val durationLegendEntry = LegendEntry()
                        setupLineChartLegend(legendEntries, durationLegendEntry, "duration", Color.RED)
                    }
                    "motivation" -> {
                        dataSet3.color = lineColors[1]
                        dataSet3.fillColor = fillColors[1]

                        val motivationLegendEntry = LegendEntry()
                        setupLineChartLegend(legendEntries, motivationLegendEntry, "motivation", Color.YELLOW)
                    }
                    "exhaustion" -> {
                        dataSet3.color = lineColors[2]
                        dataSet3.fillColor = fillColors[2]

                        val exhaustionLegendEntry = LegendEntry()
                        setupLineChartLegend(legendEntries, exhaustionLegendEntry, "exhaustion", Color.GREEN)
                    }
                }

                setupDatasetStyle(dataSet, lineWidth, textSize, textColor)
                setupDatasetStyle(dataSet2, lineWidth, textSize, textColor)
                setupDatasetStyle(dataSet3, lineWidth, textSize, textColor)

                val lineData = LineData(dataSet, dataSet2, dataSet3)
                lineData.setDrawValues(false)
                lineChart.data = lineData
                lineChart.description.text = ""
                lineChart.axisRight.isEnabled = false
                lineChart.invalidate()
            }
        }
    }

    private fun setupDatasetStyle(dataSet: LineDataSet, lineWidth: Float, textSize: Float, textColor: Int) {
        dataSet.setDrawCircles(false)
        dataSet.setDrawCircleHole(false)
        dataSet.mode = LineDataSet.Mode.HORIZONTAL_BEZIER   // smoothing the graph line
        dataSet.lineWidth = lineWidth
        dataSet.valueTextSize = textSize
        dataSet.valueTextColor = textColor
        dataSet.setDrawFilled(true)
    }

    private fun setupLineChartLegend(legendEntries: ArrayList<LegendEntry>, legendEntry: LegendEntry, label: String, color: Int) {
        legendEntry.form = Legend.LegendForm.CIRCLE
        legendEntry.formSize = 10F
        legendEntry.label = label
        legendEntry.formColor = color
        legendEntries.add(legendEntry)
        lineChart.legend.setCustom(legendEntries)
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
                for (i in 365 downTo 0) {
                    val date = currentDate.minusDays(i.toLong())
                    val formatter = DateTimeFormatter.ofPattern("dd/MM")
                    val formattedDate = date.format(formatter)
                    lastYear.add(formattedDate.toString())
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
                    if (i == daysFromFirstWorkout || i == daysFromFirstWorkout/2 || i == 1) {
                        val date = currentDate.minusDays(i.toLong())
                        val formatter = DateTimeFormatter.ofPattern("dd/MM")
                        val formattedDate = date.format(formatter)
                        allTimeDates.add(formattedDate.toString())
                    } else {
                        val date = currentDate.minusDays(i.toLong())
                        val formatter = DateTimeFormatter.ofPattern("dd/MM")
                        val formattedDate = date.format(formatter)
                        allTimeDates.add(formattedDate.toString())
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