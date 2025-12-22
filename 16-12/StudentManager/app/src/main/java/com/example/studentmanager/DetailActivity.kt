package com.example.studentmanager

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class DetailActivity : AppCompatActivity() {
    private lateinit var editTextMSSV: EditText
    private lateinit var editTextName: EditText
    private lateinit var editTextPhone: EditText
    private lateinit var editTextAddress: EditText
    private var position: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        editTextMSSV = findViewById(R.id.editTextMSSV)
        editTextName = findViewById(R.id.editTextName)
        editTextPhone = findViewById(R.id.editTextPhone)
        editTextAddress = findViewById(R.id.editTextAddress)

        // Nhận dữ liệu sinh viên từ Intent
        val student = intent.getParcelableExtra<Student>("student")
        position = intent.getIntExtra("position", -1)

        if (student != null) {
            // Hiển thị thông tin sinh viên
            editTextMSSV.setText(student.mssv)
            editTextName.setText(student.hoTen)
            editTextPhone.setText(student.soDienThoai)
            editTextAddress.setText(student.diaChi)
        }

        // Nút cập nhật
        findViewById<Button>(R.id.buttonUpdate).setOnClickListener {
            val mssv = editTextMSSV.text.toString().trim()
            val hoTen = editTextName.text.toString().trim()
            val soDienThoai = editTextPhone.text.toString().trim()
            val diaChi = editTextAddress.text.toString().trim()

            // Kiểm tra dữ liệu đầu vào
            if (mssv.isEmpty() || hoTen.isEmpty() || soDienThoai.isEmpty() || diaChi.isEmpty()) {
                return@setOnClickListener
            }

            val updatedStudent = Student(mssv, hoTen, soDienThoai, diaChi)
            val intent = Intent()
            intent.putExtra("student", updatedStudent)
            intent.putExtra("position", position)
            setResult(RESULT_OK, intent)
            finish()
        }
    }
}

