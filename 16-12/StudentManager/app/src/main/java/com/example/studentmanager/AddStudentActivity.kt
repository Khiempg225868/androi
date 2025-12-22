package com.example.studentmanager

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class AddStudentActivity : AppCompatActivity() {
    private lateinit var editTextMSSV: EditText
    private lateinit var editTextName: EditText
    private lateinit var editTextPhone: EditText
    private lateinit var editTextAddress: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_student)

        editTextMSSV = findViewById(R.id.editTextMSSV)
        editTextName = findViewById(R.id.editTextName)
        editTextPhone = findViewById(R.id.editTextPhone)
        editTextAddress = findViewById(R.id.editTextAddress)

        findViewById<Button>(R.id.buttonSave).setOnClickListener {
            val mssv = editTextMSSV.text.toString().trim()
            val hoTen = editTextName.text.toString().trim()
            val soDienThoai = editTextPhone.text.toString().trim()
            val diaChi = editTextAddress.text.toString().trim()

            // Kiểm tra dữ liệu đầu vào
            if (mssv.isEmpty() || hoTen.isEmpty() || soDienThoai.isEmpty() || diaChi.isEmpty()) {
                return@setOnClickListener
            }

            val student = Student(mssv, hoTen, soDienThoai, diaChi)
            val intent = Intent()
            intent.putExtra("student", student)
            setResult(RESULT_OK, intent)
            finish()
        }
    }
}