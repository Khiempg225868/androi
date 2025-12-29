package com.example.studentmanager

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class StudentViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: StudentRepository = (application as StudentApplication).repository

    val students: LiveData<List<Student>> = repository.allStudents

    fun addStudent(student: Student) {
        repository.insertStudent(student)
    }

    fun updateStudent(oldMssv: String, student: Student) {
        viewModelScope.launch {
            // Cập nhật bằng MSSV để đảm bảo đúng record trong database
            val updatedStudent = Student(
                mssv = oldMssv, // Giữ nguyên MSSV cũ (primary key)
                hoTen = student.hoTen,
                soDienThoai = student.soDienThoai,
                diaChi = student.diaChi
            )
            repository.updateStudent(updatedStudent)
        }
    }

    fun updateStudentByPosition(position: Int, student: Student) {
        viewModelScope.launch {
            val currentList = students.value ?: return@launch
            if (position in currentList.indices) {
                val oldStudent = currentList[position]
                updateStudent(oldStudent.mssv, student)
            }
        }
    }

    fun deleteStudent(position: Int) {
        viewModelScope.launch {
            val currentList = students.value ?: return@launch
            if (position in currentList.indices) {
                repository.deleteStudent(currentList[position])
            }
        }
    }

    fun getStudentByMssv(mssv: String, callback: (Student?) -> Unit) {
        viewModelScope.launch {
            val student = repository.getStudentByMssv(mssv)
            callback(student)
        }
    }
}
