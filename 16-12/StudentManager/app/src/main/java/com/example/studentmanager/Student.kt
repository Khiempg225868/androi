package com.example.studentmanager

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Student(
    val mssv: String,
    val hoTen: String,
    val soDienThoai: String,
    val diaChi: String
) : Parcelable

