// Lokasi: app/src/main/java/com/example/minisiasat/ui/grade/CourseGradingAdapter.kt

package com.example.minisiasat.ui.grade

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.minisiasat.R
import com.example.minisiasat.domain.model.Grade
import com.example.minisiasat.domain.model.Students

class CourseGradingAdapter(
    private val onGradeButtonClicked: (studentId: String, currentGrade: String?) -> Unit
) : RecyclerView.Adapter<CourseGradingAdapter.ViewHolder>() {

    private var studentGradeList = mutableListOf<Triple<String, Students, Grade?>>()

    fun submitList(list: List<Triple<String, Students, Grade?>>) {
        studentGradeList = list.toMutableList()
        notifyDataSetChanged()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val rowNumber: TextView = view.findViewById(R.id.rowNumberTextView)
        val studentName: TextView = view.findViewById(R.id.studentNameTextView)
        val studentNim: TextView = view.findViewById(R.id.studentNimTextView)
        val gradeButton: Button = view.findViewById(R.id.gradeButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_grade_input_table, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (nim, student, grade) = studentGradeList[position]
        holder.rowNumber.text = "${position + 1}."
        holder.studentName.text = student.name
        holder.studentNim.text = nim
        holder.gradeButton.text = grade?.grade ?: "—"
        holder.gradeButton.setOnClickListener {
            onGradeButtonClicked(nim, grade?.grade)
        }
    }

    override fun getItemCount() = studentGradeList.size
}