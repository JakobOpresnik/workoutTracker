package com.example.workouttracker

import android.annotation.SuppressLint
import android.graphics.Color
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
        val motivation: Int = itemsViewModel["motivation"].toString().toInt()
        val exhaustion: Int = itemsViewModel["exhaustion"].toString().toInt()

        holder.type.text = type
        holder.dateTime.text = "on $date at $time"
        if (duration.toInt() == 1) {
            holder.duration.text = "for $duration minute"
        }
        else {
            holder.duration.text = "for $duration minutes"
        }

        if (motivation == 0) {
            holder.motivation.text = "unmotivated \uD83D\uDE25"
            holder.motivation.setTextColor(Color.parseColor("#FF0800")) // set text color to red
        }
        else if (motivation in 1..3) {
            holder.motivation.text = "poorly motivated \uD83D\uDE13"
            holder.motivation.setTextColor(Color.parseColor("#FF8400")) // set text color to orange
        }
        else if (motivation in 4..6) {
            holder.motivation.text = "normally motivated \uD83D\uDE10"
            holder.motivation.setTextColor(Color.parseColor("#FFB700")) // set text color to yellow
        }
        else if (motivation in 7..9) {
            holder.motivation.text = "highly motivated \uD83D\uDE42"
            holder.motivation.setTextColor(Color.parseColor("#75C900")) // set text color to lime
        }
        else if (motivation == 10) {
            holder.motivation.text = "extremely motivated \uD83D\uDE01"
            holder.motivation.setTextColor(Color.parseColor("#00C90A")) // set text color to green
        }

        if (exhaustion == 0) {
            holder.exhaustion.text = "not exhausted \uD83D\uDE00"
            holder.exhaustion.setTextColor(Color.parseColor("#00C90A")) // set text color to green
        }
        else if (exhaustion in 1..3) {
            holder.exhaustion.text = "lighty exhausted \uD83D\uDE42"
            holder.exhaustion.setTextColor(Color.parseColor("#75C900")) // set text color to lime
        }
        else if (exhaustion in 4..6) {
            holder.exhaustion.text = "normally exhausted \uD83D\uDE10"
            holder.exhaustion.setTextColor(Color.parseColor("#FFB700")) // set text color to yellow
        }
        else if (exhaustion in 7..9) {
            holder.exhaustion.text = "really exhausted \uD83D\uDE2B"
            holder.exhaustion.setTextColor(Color.parseColor("#FF8400")) // set text color to orange
        }
        else if (exhaustion == 10) {
            holder.exhaustion.text = "dead \uD83D\uDC80"
            holder.exhaustion.setTextColor(Color.parseColor("#FF0800")) // set text color to red
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