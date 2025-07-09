package com.example.minisiasat

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.minisiasat.utils.DatabaseNodes
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.example.minisiasat.utils.Period

class PeriodsListFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var addFab: FloatingActionButton

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_periods_list, container, false)
        recyclerView = view.findViewById(R.id.periodsRecyclerView)
        addFab = view.findViewById(R.id.addPeriodFab)
        recyclerView.layoutManager = LinearLayoutManager(context)

        addFab.setOnClickListener { showCreatePeriodDialog() }
        loadPeriods()
        return view
    }

    private fun loadPeriods() {
        DatabaseNodes.periodsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Logika diubah untuk membuat List<Pair<String, Period>>
                val periods = snapshot.children.mapNotNull {
                    val id = it.key
                    val periodData = it.getValue(Period::class.java)
                    if (id != null && periodData != null) {
                        Pair(id, periodData)
                    } else {
                        null
                    }
                }.sortedByDescending { it.first } // Urutkan berdasarkan ID (string)

                recyclerView.adapter = PeriodsListAdapter(periods) { selectedPeriodId ->
                    // Buka halaman manage detail
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, ManagePeriodFragment.newInstance(selectedPeriodId))
                        .addToBackStack(null)
                        .commit()
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun showCreatePeriodDialog() {
        val context = requireContext()
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 24, 48, 24)
        }

        val yearInput = EditText(context).apply {
            hint = "Tahun Akademik (Contoh: 2024/2025)"
        }
        val radioGroup = RadioGroup(context)
        val semesters = mapOf("Ganjil" to 1, "Genap" to 2, "Antara" to 3)
        semesters.forEach { (text, id) ->
            radioGroup.addView(RadioButton(context).apply {
                this.text = text
                this.id = id
            })
        }
        radioGroup.check(1) // Default Ganjil

        layout.addView(yearInput)
        layout.addView(radioGroup)

        AlertDialog.Builder(context)
            .setTitle("Buat Periode Baru")
            .setView(layout)
            .setPositiveButton("Buat") { _, _ ->
                val year = yearInput.text.toString().trim()
                val semesterId = radioGroup.checkedRadioButtonId
                if (year.matches(Regex("\\d{4}/\\d{4}"))) {
                    val periodId = "$year-$semesterId"
                    val newPeriodData = mapOf("isRegistrationOpen" to false, "isLectureOpen" to false)
                    DatabaseNodes.periodsRef.child(periodId).setValue(newPeriodData)
                        .addOnSuccessListener { Toast.makeText(context, "Periode $periodId berhasil dibuat", Toast.LENGTH_SHORT).show() }
                } else {
                    Toast.makeText(context, "Format Tahun Akademik salah", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}