package com.example.quanlysinhvien

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private lateinit var rcvStudent: RecyclerView
    private lateinit var adapter: StudentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        rcvStudent = findViewById(R.id.rcvStudent)

        adapter = StudentAdapter(StudentRepository.students) { position ->
            // click -> mở màn detail
            val intent = Intent(this, StudentDetailActivity::class.java)
            intent.putExtra("index", position)
            startActivity(intent)
        }

        rcvStudent.layoutManager = LinearLayoutManager(this)
        rcvStudent.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        // khi quay lại từ màn thêm / sửa thì refresh list
        adapter.notifyDataSetChanged()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menuAddStudent -> {
                startActivity(Intent(this, AddStudentActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}