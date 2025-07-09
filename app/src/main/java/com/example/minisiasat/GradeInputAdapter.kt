package com.example.minisiasat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.minisiasat.utils.Grade
import com.example.minisiasat.utils.Students

class GradeInputAdapter(
    private val onGradeButtonClicked: (studentId: String, currentGrade: String?) -> Unit
) : RecyclerView.Adapter<GradeInputAdapter.ViewHolder>() {

    private var studentGradeList = mutableListOf<Triple<String, Students, Grade?>>()

    fun submitList(list: List<Triple<String, Students, Grade?>>) {
        studentGradeList = list.toMutableList()
        notifyDataSetChanged()
    }

    // Ubah ViewHolder untuk mencocokkan layout tabel baru
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val rowNumber: TextView = view.findViewById(R.id.rowNumberTextView)
        val studentName: TextView = view.findViewById(R.id.studentNameTextView)
        val studentNim: TextView = view.findViewById(R.id.studentNimTextView)
        val gradeButton: Button = view.findViewById(R.id.gradeButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Ganti layout ke item_grade_input_table
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_grade_input_table, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (nim, student, grade) = studentGradeList[position]

        holder.rowNumber.text = "${position + 1}."
        holder.studentName.text = student.name
        holder.studentNim.text = nim

        // --- INI PERUBAHANNYA ---
        // Jika nilai null, tampilkan "—". Jika ada, tampilkan nilainya.
        holder.gradeButton.text = grade?.grade ?: "—"
        // ------------------------

        holder.gradeButton.setOnClickListener {
            onGradeButtonClicked(nim, grade?.grade)
        }
    }

    override fun getItemCount() = studentGradeList.size
}