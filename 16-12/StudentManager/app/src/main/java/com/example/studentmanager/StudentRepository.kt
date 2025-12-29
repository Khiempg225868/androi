package com.example.studentmanager

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class StudentRepository(application: Application) {
    private val studentDao: StudentDao = StudentDatabase.getDatabase(application).studentDao()
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val allStudents: LiveData<List<Student>> = studentDao.getAllStudents().map { entities ->
        entities.map { it.toStudent() }
    }

    fun insertStudent(student: Student) {
        repositoryScope.launch {
            studentDao.insertStudent(StudentEntity.fromStudent(student))
        }
    }

    fun updateStudent(student: Student) {
        repositoryScope.launch {
            studentDao.updateStudent(StudentEntity.fromStudent(student))
        }
    }

    fun deleteStudent(student: Student) {
        repositoryScope.launch {
            studentDao.deleteStudent(StudentEntity.fromStudent(student))
        }
    }

    fun deleteStudentByMssv(mssv: String) {
        repositoryScope.launch {
            studentDao.deleteStudentByMssv(mssv)
        }
    }

    suspend fun getStudentByMssv(mssv: String): Student? {
        return studentDao.getStudentByMssv(mssv)?.toStudent()
    }
}
