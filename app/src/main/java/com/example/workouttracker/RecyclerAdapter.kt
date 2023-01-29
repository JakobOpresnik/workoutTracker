package com.example.workouttracker

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

class RecyclerAdapter(private val dataSet: MutableList<HashMap<String, String>>, private val onClickObject: myOnClick) : RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {

    interface myOnClick {
        fun onClick(p0: View?, position: Int)
    }

    interface myOnLongClick {
        fun onLongClick(p0: View?, position: Int)
    }

    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val type: TextView = view.findViewById(R.id.type)
        val dateTime: TextView = view.findViewById(R.id.date_time)
        val duration: TextView = view.findViewById(R.id.duration)
        val motivation: TextView = view.findViewById(R.id.motivation)
        val exhaustion: TextView = view.findViewById(R.id.exhaustion)
        val line: CardView = view.findViewById(R.id.cvLine)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.workouts_list, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val itemsViewModel: HashMap<String, String> = dataSet[position]

        val type = itemsViewModel["type"]?.replace("_", " ")
        val date = itemsViewModel["date"]?.replace("-", ". ")
        val time = itemsViewModel["time"]
        val duration: String = itemsViewModel["duration"].toString()
        val motivation: String = itemsViewModel["motivation"].toString()
        val exhaustion: String = itemsViewModel["exhaustion"].toString()

        holder.type.text = type
        holder.dateTime.text = "on $date at $time"
        if (duration.toInt() == 1) {
            holder.duration.text = "$duration minute"
        }
        else {
            holder.duration.text = "for $duration minutes"
        }

        when (motivation) {
            "10" -> holder.motivation.text = "zero motivation \uD83D\uDE25"
        }

        holder.line.setOnClickListener { p0 ->
            Log.i("short click", "here code comes click on ${holder.adapterPosition}")
            onClickObject.onClick(p0, holder.adapterPosition)
        }

        /*holder.line.setOnLongClickListener { p0 ->
            //holder.line.setCardBackgroundColor(Color.RED)
            onLongClickObject.onLongClick(p0, holder.adapterPosition)
            true
        }*/
    }

    override fun getItemCount() = dataSet.size
}