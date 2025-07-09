package com.example.minisiasat

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.minisiasat.utils.AttendanceSession // Menggunakan AttendanceSession
import com.google.android.material.button.MaterialButton

class AttendanceAdapter(
    private val sessions: List<AttendanceSession>, // Menggunakan AttendanceSession
    private val studentId: String,
    private val onAttendClick: (sessionId: String) -> Unit
) : RecyclerView.Adapter<AttendanceAdapter.ViewHolder>() {

    // Menggunakan ID dari layout yang sudah ada
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val topic: TextView = view.findViewById(R.id.topicTextView)
        val date: TextView = view.findViewById(R.id.dateTextView)
        val status: TextView = view.findViewById(R.id.statusTextView)
        val button: MaterialButton = view.findViewById(R.id.toggleAttendanceButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_attendance_session, parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val session = sessions[position]
        holder.topic.text = session.topic
        holder.date.text = session.date

        val isPresent = session.attendees?.containsKey(studentId) == true

        // Sembunyikan status text view, karena kita akan menampilkannya di tombol
        holder.status.visibility = View.GONE

        if (isPresent) {
            holder.button.text = "Hadir"
            holder.button.isEnabled = false
            // Gunakan warna dari resource jika ada, atau parse warna
            holder.button.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.green))
            holder.button.strokeColor = ContextCompat.getColorStateList(holder.itemView.context, R.color.green)
        } else {
            if (session.isOpen) {
                holder.button.text = "Absen"
                holder.button.isEnabled = true
                holder.button.setOnClickListener { onAttendClick(session.sessionId!!) }
            } else {
                holder.button.text = "Ditutup"
                holder.button.isEnabled = false
            }
        }
    }

    override fun getItemCount() = sessions.size
}