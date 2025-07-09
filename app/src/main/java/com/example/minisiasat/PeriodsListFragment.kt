package com.example.minisiasat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.minisiasat.utils.DatabaseNodes
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class PeriodsListFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_period_years, container, false)
        recyclerView = view.findViewById(R.id.yearsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        loadAcademicYears()
        return view
    }

    private fun loadAcademicYears() {
        DatabaseNodes.periodsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val years = snapshot.children.mapNotNull { it.key }.sortedDescending()
                recyclerView.adapter = AcademicYearAdapter(years) { selectedYear ->
                    // Buka halaman manage semester untuk tahun yang dipilih
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, ManagePeriodFragment.newInstance(selectedYear))
                        .addToBackStack(null)
                        .commit()
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }
}