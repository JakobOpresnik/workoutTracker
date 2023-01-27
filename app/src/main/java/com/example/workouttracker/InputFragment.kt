package com.example.workouttracker

//noinspection SuspiciousImport
import android.R
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsSeekBar
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.workouttracker.databinding.FragmentInputBinding


class InputFragment : Fragment() {

    private lateinit var binding: FragmentInputBinding
    private lateinit var motivationValue: TextView
    private lateinit var exhaustionValue: TextView
    private lateinit var motivationSeekBar: SeekBar
    private lateinit var exhaustionSeekBar: SeekBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentInputBinding.inflate(inflater, container, false)

        motivationValue = binding.motivationValue
        exhaustionValue = binding.exhaustionValue
        motivationSeekBar = binding.motivationSeekbar
        exhaustionSeekBar = binding.exhaustionSeekbar

        val spinner = binding.workoutTypePicker
        val spinnerData = arrayListOf("Weightlifting", "Gym Cardio", "Run", "Sprint", "Hike", "Bicycle Ride")
        val adapter = ArrayAdapter(requireContext(), R.layout.simple_spinner_item, spinnerData)
        spinner.adapter = adapter

        adapter.setDropDownViewResource(R.layout.simple_spinner_item)
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selected = parent?.getItemAtPosition(position).toString()
                Log.i("selected workout type", selected)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        motivationSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                motivationValue.text = progress.toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        exhaustionSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                exhaustionValue.text = progress.toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        return binding.root
    }
}