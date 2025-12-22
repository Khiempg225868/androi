package com.example.studentmanager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.ImageButton
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.core.os.bundleOf

class StudentListFragment : Fragment() {

    private val viewModel: StudentViewModel by activityViewModels()

    private lateinit var listView: ListView
    private lateinit var adapter: StudentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_student_list, container, false)
        listView = view.findViewById(R.id.listViewStudents)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = StudentAdapter(
            mutableListOf(),
            onEditClick = { position ->
                navigateToEdit(position)
            },
            onDeleteClick = { position ->
                viewModel.deleteStudent(position)
            }
        )
        listView.adapter = adapter

        viewModel.students.observe(viewLifecycleOwner) { students ->
            adapter.updateData(students)
        }

        listView.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                navigateToEdit(position)
            }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_add -> {
                findNavController().navigate(R.id.addStudentFragment)
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun navigateToEdit(position: Int) {
        val students = viewModel.students.value ?: return
        if (position !in students.indices) return
        val student = students[position]
        val bundle = bundleOf(
            "position" to position,
            "student" to student
        )
        findNavController().navigate(R.id.updateStudentFragment, bundle)
    }

    private class StudentAdapter(
        private val students: MutableList<Student>,
        private val onEditClick: (Int) -> Unit,
        private val onDeleteClick: (Int) -> Unit
    ) : BaseAdapter() {

        fun updateData(newStudents: List<Student>) {
            students.clear()
            students.addAll(newStudents)
            notifyDataSetChanged()
        }

        override fun getCount(): Int = students.size

        override fun getItem(position: Int): Student = students[position]

        override fun getItemId(position: Int): Long = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val view = convertView ?: LayoutInflater.from(parent?.context)
                .inflate(R.layout.item_student, parent, false)

            val student = students[position]
            val textViewName = view.findViewById<TextView>(R.id.textViewName)
            val textViewMssv = view.findViewById<TextView>(R.id.textViewMssv)
            val buttonEdit = view.findViewById<ImageButton>(R.id.buttonEdit)
            val buttonDelete = view.findViewById<ImageButton>(R.id.buttonDelete)

            textViewName.text = student.hoTen
            textViewMssv.text = student.mssv

            buttonEdit.setOnClickListener {
                onEditClick(position)
            }

            buttonDelete.setOnClickListener {
                onDeleteClick(position)
            }

            return view
        }
    }
}
