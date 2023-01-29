package com.example.workouttracker

//noinspection SuspiciousImport
import android.R
import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.workouttracker.databinding.FragmentListWorkoutsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ListWorkoutsFragment : Fragment() {

    private lateinit var binding: FragmentListWorkoutsBinding
    private lateinit var adapter: RecyclerAdapter
    private lateinit var saveFilterButton: ImageButton
    private lateinit var numberWorkouts: TextView
    private lateinit var noWorkoutText: TextView

    private lateinit var selectedWorkoutType: String

    private lateinit var data: MutableList<HashMap<String, String>>

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentListWorkoutsBinding.inflate(inflater, container, false)
        saveFilterButton = binding.saveTypeFilter
        numberWorkouts = binding.numberWorkouts
        noWorkoutText = binding.noWorkouts
        noWorkoutText.visibility = View.GONE

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val spinner = binding.typeFilter
        val spinnerData = arrayListOf("All", "Weightlifting", "Gym Cardio", "Run", "Sprint", "Hike", "Bicycle Ride")
        val spinnerAdapter = ArrayAdapter(requireContext(), R.layout.simple_spinner_item, spinnerData)
        spinner.adapter = spinnerAdapter

        spinnerAdapter.setDropDownViewResource(R.layout.simple_spinner_item)
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedWorkoutType = parent?.getItemAtPosition(position).toString().replace(" ", "_").toUpperCase()
                Log.i("selected workout type", selectedWorkoutType)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }


        val id = auth.currentUser?.uid
        if (id != null) {
            firestore.collection("users").document(id).get()
                .addOnSuccessListener {  user ->
                    data = user.get("workouts") as MutableList<HashMap<String, String>>
                    // if a user has no workouts of any type yet
                    if (data.isEmpty()) {
                        binding.recyclerView.visibility = View.GONE
                        noWorkoutText.visibility = View.VISIBLE
                    }

                    // upon applying the filter
                    saveFilterButton.setOnClickListener {
                        binding.recyclerView.visibility = View.VISIBLE

                        data = mutableListOf<HashMap<String, String>>()
                        firestore.collection("users").document(id).get()
                            .addOnSuccessListener { user ->
                                val workouts = user.get("workouts") as MutableList<HashMap<String, String>>
                                if (selectedWorkoutType == "ALL") {
                                    data = workouts
                                }
                                else {
                                    for (workout in workouts) {
                                        if (workout["type"] == selectedWorkoutType) {
                                            data.add(workout)
                                        }
                                    }
                                    // if a user has no workouts of a particular type yet
                                    if (data.isEmpty()) {
                                        handleNoWorkouts()
                                    }
                                }
                                try {
                                    addNumberWorkouts(data.size)
                                    binding.recyclerView.layoutManager =
                                        LinearLayoutManager(this.context)
                                    adapter = RecyclerAdapter(
                                        data,
                                        object : RecyclerAdapter.myOnClick {
                                            @SuppressLint("NotifyDataSetChanged")
                                            override fun onClick(p0: View?, position: Int) {
                                                val builder =
                                                    AlertDialog.Builder(this@ListWorkoutsFragment.requireContext())
                                                builder.setTitle("DELETE THIS WORKOUT?")
                                                builder.setMessage("Are you sure you want to delete one of your previously saved workout sessions?")
                                                builder.setIcon(android.R.drawable.ic_dialog_alert)
                                                builder.setPositiveButton("Yes") { dialogInterface, which ->
                                                    Toast.makeText(
                                                        context,
                                                        "deleting workout session...",
                                                        Toast.LENGTH_LONG
                                                    ).show()
                                                    //val documentId = data[position].getID()
                                                    data.removeAt(position)
                                                    adapter.notifyDataSetChanged()


                                                    /*firebase = FirebaseFirestore.getInstance()
                                            firebase.collection("reservations").document(documentId)
                                                .delete()
                                                .addOnSuccessListener {
                                                    //Toast.makeText(activity?.applicationContext, "successfully deleted reservation from firebase", Toast.LENGTH_SHORT).show()
                                                }
                                                .addOnFailureListener {
                                                    //Toast.makeText(activity?.applicationContext, "failed to delete reservation from firebase", Toast.LENGTH_SHORT).show()
                                                }*/

                                                }
                                                builder.setNeutralButton("Cancel") { dialogInterface, which ->
                                                    Toast.makeText(
                                                        context,
                                                        "deletion cancelled",
                                                        Toast.LENGTH_LONG
                                                    ).show()
                                                }
                                                builder.setNegativeButton("No") { dialogInterface, which ->
                                                    Toast.makeText(
                                                        context,
                                                        "deletion rejected",
                                                        Toast.LENGTH_LONG
                                                    ).show()
                                                }
                                                val alertDialog: AlertDialog =
                                                    builder.create()
                                                alertDialog.setCancelable(false)
                                                alertDialog.show()
                                            }
                                        })
                                    binding.recyclerView.adapter = adapter
                                } catch (err: Error) {
                                    Log.i("error", "error regarding dialog box (${err})")
                                }
                            }.addOnFailureListener {
                                Log.i("current user", "/")
                        }
                    }
                    addNumberWorkouts(data.size)
                    try {
                        binding.recyclerView.layoutManager = LinearLayoutManager(this.context)
                        adapter = RecyclerAdapter(data, object: RecyclerAdapter.myOnClick {
                            @SuppressLint("NotifyDataSetChanged")
                            override fun onClick(p0: View?, position: Int) {
                                val builder = AlertDialog.Builder(this@ListWorkoutsFragment.requireContext())
                                builder.setTitle("DELETE THIS WORKOUT?")
                                builder.setMessage("Are you sure you want to delete one of your previously saved workout sessions?")
                                builder.setIcon(android.R.drawable.ic_dialog_alert)
                                builder.setPositiveButton("Yes") { dialogInterface, which ->
                                    Toast.makeText(context, "deleting workout session...", Toast.LENGTH_LONG).show()
                                    //val documentId = data[position].getID()
                                    data.removeAt(position)
                                    adapter.notifyDataSetChanged()


                                    /*firebase = FirebaseFirestore.getInstance()
                                    firebase.collection("reservations").document(documentId)
                                        .delete()
                                        .addOnSuccessListener {
                                            //Toast.makeText(activity?.applicationContext, "successfully deleted reservation from firebase", Toast.LENGTH_SHORT).show()
                                        }
                                        .addOnFailureListener {
                                            //Toast.makeText(activity?.applicationContext, "failed to delete reservation from firebase", Toast.LENGTH_SHORT).show()
                                        }*/

                                }
                                builder.setNeutralButton("Cancel") { dialogInterface, which ->
                                    Toast.makeText(context, "deletion cancelled", Toast.LENGTH_LONG).show()
                                }
                                builder.setNegativeButton("No") { dialogInterface, which ->
                                    Toast.makeText(context, "deletion rejected", Toast.LENGTH_LONG).show()
                                }
                                val alertDialog: AlertDialog = builder.create()
                                alertDialog.setCancelable(false)
                                alertDialog.show()
                            }
                        })
                        binding.recyclerView.adapter = adapter
                    } catch (err: Error) {
                        Log.i("error", "error regarding dialog box (${err})")
                    }
                }.addOnFailureListener {
                    Log.i("current user", "/")
                }
        }

        return binding.root
    }

    private fun handleNoWorkouts() {
        binding.recyclerView.visibility = View.GONE
        val text = noWorkoutText.text
        val index = text.indexOf(" ")
        val substringText = text.substring(0, index)
        val workoutType = selectedWorkoutType.replace("_", " ").toLowerCase()
        val newText = "$substringText $workoutType workouts yet \uD83D\uDE34"
        noWorkoutText.text = newText
        noWorkoutText.visibility = View.VISIBLE
    }

    @SuppressLint("SetTextI18n")
    private fun addNumberWorkouts(number: Int) {
        if (number == 1) {
            numberWorkouts.text = "$number workout"
        }
        else {
            numberWorkouts.text = "$number workouts"
        }
    }
}