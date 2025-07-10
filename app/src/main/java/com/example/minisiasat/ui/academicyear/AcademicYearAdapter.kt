package com.example.minisiasat.ui.academicyear

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.minisiasat.R

class AcademicYearAdapter(
    private val years: List<String>,
    private val onYearClick: (String) -> Unit
) : RecyclerView.Adapter<AcademicYearAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val yearTextView: TextView = view.findViewById(R.id.yearTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_academic_year, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val year = years[position].replace("-", "/") // Format tampilan
        holder.yearTextView.text = year
        holder.itemView.setOnClickListener { onYearClick(years[position]) }
    }

    override fun getItemCount() = years.size
}