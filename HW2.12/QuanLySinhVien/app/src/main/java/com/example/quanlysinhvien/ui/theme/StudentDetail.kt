package com.example.quanlysinhvien

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class StudentDetailActivity : AppCompatActivity() {

    private lateinit var edtMssv: EditText
    private lateinit var edtName: EditText
    private lateinit var edtPhone: EditText
    private lateinit var edtAddress: EditText
    private lateinit var btnUpdate: Button

    private var index: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_detail)

        supportActionBar?.title = "Chi tiết sinh viên"

        edtMssv = findViewById(R.id.edtMssv)
        edtName = findViewById(R.id.edtName)
        edtPhone = findViewById(R.id.edtPhone)
        edtAddress = findViewById(R.id.edtAddress)
        btnUpdate = findViewById(R.id.btnUpdate)

        index = intent.getIntExtra("index", -1)
        if (index == -1 || index >= StudentRepository.students.size) {
            Toast.makeText(this, "Lỗi dữ liệu", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val st = StudentRepository.students[index]
        edtMssv.setText(st.mssv)
        edtName.setText(st.name)
        edtPhone.setText(st.phone)
        edtAddress.setText(st.address)

        btnUpdate.setOnClickListener { updateStudent() }
    }

    private fun updateStudent() {
        val mssv = edtMssv.text.toString().trim()
        val name = edtName.text.toString().trim()
        val phone = edtPhone.text.toString().trim()
        val address = edtAddress.text.toString().trim()

        if (mssv.isEmpty() || name.isEmpty()) {
            Toast.makeText(this, "Nhập MSSV và Họ tên", Toast.LENGTH_SHORT).show()
            return
        }

        val st = StudentRepository.students[index]
        st.mssv = mssv
        st.name = name
        st.phone = phone
        st.address = address

        Toast.makeText(this, "Đã cập nhật", Toast.LENGTH_SHORT).show()
        finish()
    }
}