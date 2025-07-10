package com.example.minisiasat.ui.transcript

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.minisiasat.R
import com.example.minisiasat.domain.model.CourseGrade

class TranscriptAdapter(
    private val courseGrades: List<CourseGrade>
) : RecyclerView.Adapter<TranscriptAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val courseName: TextView = view.findViewById(R.id.courseNameTextView)
        val courseInfo: TextView = view.findViewById(R.id.courseInfoTextView)
        val letterGrade: TextView = view.findViewById(R.id.letterGradeTextView)
        val numericGrade: TextView = view.findViewById(R.id.numericGradeTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transkrip, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = courseGrades[position]
        holder.courseName.text = item.course.courseName
        holder.courseInfo.text = "${item.course.courseCode} • ${item.course.credits} SKS"
        holder.letterGrade.text = item.grade?.grade ?: "-"
        holder.numericGrade.text = String.format("%.2f", getNumericGrade(item.grade?.grade))
    }

    override fun getItemCount() = courseGrades.size

    // Helper untuk konversi nilai huruf ke angka
    private fun getNumericGrade(letter: String?): Float {
        return when (letter) {
            "A" -> 4.0f
            "AB" -> 3.5f
            "B" -> 3.0f
            "BC" -> 2.5f
            "C" -> 2.0f
            "D" -> 1.0f
            "E" -> 0.0f
            else -> 0.0f
        }
    }
}