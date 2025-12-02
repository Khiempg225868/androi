package com.example.quanlysinhvien

object StudentRepository {
    val students: MutableList<Student> = mutableListOf(
        Student("20200001", "Nguyễn Văn A", "0123456789", "Hà Nội"),
        Student("20200002", "Trần Thị B", "0987654321", "TP.HCM")
    )
}