package com.example.studentmanager

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "students")
data class StudentEntity(
    @PrimaryKey
    val mssv: String,
    val hoTen: String,
    val soDienThoai: String,
    val diaChi: String
) {
    fun toStudent(): Student {
        return Student(mssv, hoTen, soDienThoai, diaChi)
    }

    companion object {
        fun fromStudent(student: Student): StudentEntity {
            return StudentEntity(
                student.mssv,
                student.hoTen,
                student.soDienThoai,
                student.diaChi
            )
        }
    }
}
