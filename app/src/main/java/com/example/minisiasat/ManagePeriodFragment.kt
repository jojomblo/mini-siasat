package com.example.minisiasat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.minisiasat.utils.DatabaseNodes
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class ManagePeriodFragment : Fragment() {

    private lateinit var switchRegistration: SwitchMaterial
    private lateinit var switchLecture: SwitchMaterial
    private lateinit var periodId: String

    companion object {
        fun newInstance(periodId: String) = ManagePeriodFragment().apply {
            arguments = Bundle().apply { putString("periodId", periodId) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { periodId = it.getString("periodId")!! }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_manage_period, container, false)
        switchRegistration = view.findViewById(R.id.switchRegistration)
        switchLecture = view.findViewById(R.id.switchLecture)

        loadCurrentStatus()
        setupListeners()

        return view
    }

    private fun loadCurrentStatus() {
        DatabaseNodes.periodsRef.child(periodId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Hapus listener sementara agar tidak memicu loop saat data di-load
                switchRegistration.setOnCheckedChangeListener(null)
                switchLecture.setOnCheckedChangeListener(null)

                switchRegistration.isChecked = snapshot.child("isRegistrationOpen").getValue(Boolean::class.java) ?: false
                switchLecture.isChecked = snapshot.child("isLectureOpen").getValue(Boolean::class.java) ?: false

                // Pasang kembali listener setelah selesai
                setupListeners()
            }
            override fun onCancelled(error: DatabaseError) { /* Handle error */ }
        })
    }

    private fun setupListeners() {
        switchRegistration.setOnCheckedChangeListener { _, isChecked ->
            updatePeriodStatus("isRegistrationOpen", isChecked)
        }
        switchLecture.setOnCheckedChangeListener { _, isChecked ->
            updatePeriodStatus("isLectureOpen", isChecked)
        }
    }

    // --- FUNGSI UTAMA YANG BARU ---
    private fun updatePeriodStatus(fieldToUpdate: String, isEnabled: Boolean) {
        // Jika user mematikan switch, cukup update field tersebut saja
        if (!isEnabled) {
            DatabaseNodes.periodsRef.child(periodId).child(fieldToUpdate).setValue(false)
            return
        }

        // Jika user menyalakan switch, lakukan deaktivasi otomatis
        DatabaseNodes.periodsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val updates = mutableMapOf<String, Any?>()

                // 1. Matikan semua periode untuk field yang sama
                snapshot.children.forEach { periodSnapshot ->
                    updates["/${periodSnapshot.key}/$fieldToUpdate"] = false
                }

                // 2. Nyalakan hanya untuk periode yang dipilih
                updates["/$periodId/$fieldToUpdate"] = true

                // 3. Lakukan multi-path update
                DatabaseNodes.periodsRef.root.updateChildren(updates)
                    .addOnSuccessListener {
                        val statusType = if (fieldToUpdate == "isRegistrationOpen") "Registrasi" else "Perkuliahan"
                        Toast.makeText(context, "Periode $statusType untuk $periodId DIBUKA", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Gagal memperbarui status", Toast.LENGTH_SHORT).show()
                    }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }
    // -----------------------------
}