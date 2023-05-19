package com.example.daysuntil

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.daysuntil.database.DatesDatabase
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class DatesListAdapter(
    private var dates: Array<DatesDatabase>,
    private var periods: Array<String>,
    private val update: (position: Int, date: DatesDatabase,
                         (position: Int)->Unit,
                         (position: Int, name: String, date: LocalDate, repeat: Int, period: String)->Unit
    )->Unit
): RecyclerView.Adapter<DatesListAdapter.ViewHolder>() {
    class ViewHolder(view: View): RecyclerView.ViewHolder(view){
        val name: TextView by lazy {view.findViewById(R.id.name)}
        val timeUntil: TextView by lazy {view.findViewById(R.id.timeUntil)}
        val date: TextView by lazy {view.findViewById(R.id.date)}
        val dateCard: CardView by lazy {view.findViewById(R.id.dateCard)}
    }
    fun addItem(newDates: DatesDatabase, newPeriods: String){
        dates += newDates
        periods += newPeriods
        notifyItemInserted(dates.size-1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.date_card, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = dates.size

    private fun getFormat(repeat: Int): String{
        return when(repeat){
            0->"EEEE"
            1->"dd"
            2->"dd MMMM"
            else->"dd MMMM yyyy"
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.name.text = dates[position].name
        holder.timeUntil.text = periods[position]
        holder.date.text = dates[position].date.format(DateTimeFormatter.ofPattern(getFormat(dates[position].repeat)))

        holder.dateCard.setOnClickListener {
            update(position, dates[position], {
                dates = dates.removeAt(it)
                periods = periods.removeAt(it)
                notifyItemRemoved(it)
                for(i in it until dates.size) notifyItemChanged(i)
            }, {pos, name, date, repeat, period->
                dates[pos].name = name
                dates[pos].date = date
                dates[pos].repeat = repeat
                periods[pos] = period
                notifyItemChanged(pos)
            })
        }
    }
}