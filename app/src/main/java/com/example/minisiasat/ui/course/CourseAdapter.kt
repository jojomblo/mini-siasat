package com.example.minisiasat.ui.course

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.minisiasat.R
import com.example.minisiasat.domain.model.CourseGroup


class CourseAdapter(
    private val courseGroups: List<CourseGroup>,
    private val onGroupClicked: (CourseGroup) -> Unit
) : RecyclerView.Adapter<CourseAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val groupCode: TextView = view.findViewById(R.id.courseGroupCodeTextView)
        val groupName: TextView = view.findViewById(R.id.courseGroupNameTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_course_group, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val group = courseGroups[position]
        holder.groupCode.text = group.prefix
        holder.groupName.text = group.representativeName
        holder.itemView.setOnClickListener {
            onGroupClicked(group)
        }
    }

    override fun getItemCount() = courseGroups.size
}