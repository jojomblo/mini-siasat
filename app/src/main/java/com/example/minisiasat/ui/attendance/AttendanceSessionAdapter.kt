package com.example.minisiasat.ui.attendance

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.minisiasat.R
import com.example.minisiasat.domain.model.AttendanceSession

enum class AttendanceAction {
    TOGGLE,
    VIEW_DETAILS
}

class AttendanceSessionAdapter(
    private val sessions: List<AttendanceSession>,
    private val onAction: (AttendanceSession, AttendanceAction) -> Unit
) : RecyclerView.Adapter<AttendanceSessionAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val topic: TextView = view.findViewById(R.id.topicTextView)
        val date: TextView = view.findViewById(R.id.dateTextView)
        val status: TextView = view.findViewById(R.id.statusTextView)
        val toggleButton: Button = view.findViewById(R.id.toggleAttendanceButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_attendance_session, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val session = sessions[position]
        holder.topic.text = session.topic
        holder.date.text = session.date

        if (session.isOpen) {
            holder.status.text = "Status: Dibuka"
            holder.status.setTextColor(ContextCompat.getColor(holder.itemView.context,
                R.color.green
            ))
            holder.toggleButton.text = "Tutup"
        } else {
            holder.status.text = "Status: Ditutup"
            holder.status.setTextColor(ContextCompat.getColor(holder.itemView.context,
                R.color.warningRed
            ))
            holder.toggleButton.text = "Buka"
        }

        holder.toggleButton.setOnClickListener {
            onAction(session, AttendanceAction.TOGGLE)
        }
        holder.itemView.setOnClickListener {
            onAction(session, AttendanceAction.VIEW_DETAILS)
        }
    }

    override fun getItemCount() = sessions.size
}