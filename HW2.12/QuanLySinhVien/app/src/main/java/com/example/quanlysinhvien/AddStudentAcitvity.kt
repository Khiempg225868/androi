package com.example.quanlysinhvien

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class AddStudentActivity : AppCompatActivity() {

    private lateinit var edtMssv: EditText
    private lateinit var edtName: EditText
    private lateinit var edtPhone: EditText
    private lateinit var edtAddress: EditText
    private lateinit var btnSave: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_student)

        supportActionBar?.title = "Thêm sinh viên"

        edtMssv = findViewById(R.id.edtMssv)
        edtName = findViewById(R.id.edtName)
        edtPhone = findViewById(R.id.edtPhone)
        edtAddress = findViewById(R.id.edtAddress)
        btnSave = findViewById(R.id.btnSave)

        btnSave.setOnClickListener { saveStudent() }
    }

    private fun saveStudent() {
        val mssv = edtMssv.text.toString().trim()
        val name = edtName.text.toString().trim()
        val phone = edtPhone.text.toString().trim()
        val address = edtAddress.text.toString().trim()

        if (mssv.isEmpty() || name.isEmpty()) {
            Toast.makeText(this, "Nhập MSSV và Họ tên", Toast.LENGTH_SHORT).show()
            return
        }

        StudentRepository.students.add(
            Student(mssv, name, phone, address)
        )

        Toast.makeText(this, "Đã thêm sinh viên", Toast.LENGTH_SHORT).show()
        finish() // quay lại MainActivity
    }
}