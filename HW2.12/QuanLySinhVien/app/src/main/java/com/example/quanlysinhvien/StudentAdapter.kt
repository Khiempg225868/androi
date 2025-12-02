package com.example.quanlysinhvien

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class StudentAdapter(
    private val items: List<Student>,
    private val onItemClick: (position: Int) -> Unit
) : RecyclerView.Adapter<StudentAdapter.StudentViewHolder>() {

    class StudentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtName: TextView = itemView.findViewById(R.id.txtName)
        val txtMssv: TextView = itemView.findViewById(R.id.txtMssv)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_student, parent, false)
        return StudentViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: StudentViewHolder, position: Int) {
        val st = items[position]
        holder.txtName.text = st.name
        holder.txtMssv.text = st.mssv

        holder.itemView.setOnClickListener {
            onItemClick(position)
        }
    }
}