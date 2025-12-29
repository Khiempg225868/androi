package com.example.studentmanager

import android.app.Application

class StudentApplication : Application() {
    val repository: StudentRepository by lazy {
        StudentRepository(this)
    }
}
