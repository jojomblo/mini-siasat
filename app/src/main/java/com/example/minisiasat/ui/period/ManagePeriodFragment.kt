package com.example.minisiasat.ui.period

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.minisiasat.R
import com.example.minisiasat.data.DatabaseNodes
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class ManagePeriodFragment : Fragment() {

    private lateinit var academicYear: String
    private lateinit var switches: Map<String, SwitchMaterial>

    companion object {
        fun newInstance(year: String) = ManagePeriodFragment().apply {
            arguments = Bundle().apply { putString("academicyear", year) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { academicYear = it.getString("academicyear")!! }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_manage_period, container, false)
        view.findViewById<TextView>(R.id.yearTitleTextView).text = "Kelola Periode ${academicYear.replace("-", "/")}"

        switches = mapOf(
            "1/isRegistrationOpen" to view.findViewById(R.id.switchRegGanjil),
            "1/isLectureOpen" to view.findViewById(R.id.switchLecGanjil),
            "2/isRegistrationOpen" to view.findViewById(R.id.switchRegGenap),
            "2/isLectureOpen" to view.findViewById(R.id.switchLecGenap),
            "3/isRegistrationOpen" to view.findViewById(R.id.switchRegAntara),
            "3/isLectureOpen" to view.findViewById(R.id.switchLecAntara)
        )

        loadAllStatuses()
        setupAllListeners()
        return view
    }

    private fun loadAllStatuses() {
        DatabaseNodes.periodsRef.child(academicYear).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                switches.forEach { (path, switch) ->
                    val pathParts = path.split("/")
                    val semesterId = pathParts[0]
                    val field = pathParts[1]
                    switch.isChecked = snapshot.child(semesterId).child(field).getValue(Boolean::class.java) ?: false
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun setupAllListeners() {
        switches.forEach { (path, switch) ->
            switch.setOnCheckedChangeListener { _, isChecked ->
                val pathParts = path.split("/")
                updatePeriodStatus(pathParts[0], pathParts[1], isChecked)
            }
        }
    }

    private fun updatePeriodStatus(semesterId: String, field: String, isEnabled: Boolean) {
        if (!isEnabled) {
            DatabaseNodes.periodsRef.child(academicYear).child(semesterId).child(field).setValue(false)
            return
        }

        DatabaseNodes.periodsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val updates = mutableMapOf<String, Any?>()
                snapshot.children.forEach { yearNode ->
                    yearNode.children.forEach { semesterNode ->
                        updates["${yearNode.key}/${semesterNode.key}/$field"] = false
                    }
                }
                val oppositeField = if (field == "isRegistrationOpen") "isLectureOpen" else "isRegistrationOpen"
                updates["$academicYear/$semesterId/$oppositeField"] = false
                updates["$academicYear/$semesterId/$field"] = true

                DatabaseNodes.periodsRef.updateChildren(updates)
                    .addOnSuccessListener { Toast.makeText(context, "Status berhasil diperbarui", Toast.LENGTH_SHORT).show() }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }
}