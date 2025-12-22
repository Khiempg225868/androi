package com.example.studentmanager

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class StudentViewModel : ViewModel() {

    private val _students = MutableLiveData<MutableList<Student>>(mutableListOf())
    val students: LiveData<MutableList<Student>> = _students

    fun addStudent(student: Student) {
        val current = _students.value ?: mutableListOf()
        current.add(student)
        _students.value = current
    }

    fun updateStudent(position: Int, student: Student) {
        val current = _students.value ?: return
        if (position in current.indices) {
            current[position] = student
            _students.value = current
        }
    }

    fun deleteStudent(position: Int) {
        val current = _students.value ?: return
        if (position in current.indices) {
            current.removeAt(position)
            _students.value = current
        }
    }
}
