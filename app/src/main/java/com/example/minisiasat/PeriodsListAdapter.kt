package com.example.minisiasat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.minisiasat.utils.Period // Menggunakan Period dari utils

// Data class PeriodStatus dihapus dari sini

class PeriodsListAdapter(
    // Kita sekarang menggunakan Pair untuk membawa ID dan objek Period
    private val periods: List<Pair<String, Period>>,
    private val onClick: (periodId: String) -> Unit
) : RecyclerView.Adapter<PeriodsListAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val idTextView: TextView = view.findViewById(R.id.periodIdTextView)
        val statusTextView: TextView = view.findViewById(R.id.periodStatusTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_period, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (id, period) = periods[position]
        holder.idTextView.text = formatPeriodId(id)

        val regStatus = if (period.isRegistrationOpen == true) "Dibuka" else "Ditutup"
        val lecStatus = if (period.isLectureOpen == true) "Dibuka" else "Ditutup"

        holder.statusTextView.text = "Registrasi: $regStatus, Perkuliahan: $lecStatus"
        holder.itemView.setOnClickListener { onClick(id) }
    }

    override fun getItemCount() = periods.size

    private fun formatPeriodId(id: String): String {
        val parts = id.split("-")
        if (parts.size != 2) return id

        val year = parts[0]
        val semesterType = when (parts[1]) {
            "1" -> "Ganjil"
            "2" -> "Genap"
            "3" -> "Antara"
            else -> ""
        }
        return "$year - $semesterType"
    }
}