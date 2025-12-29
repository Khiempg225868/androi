package com.example.filemanagement

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class ViewFileActivity : AppCompatActivity() {

    private lateinit var textView: TextView
    private lateinit var imageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_file)

        textView = findViewById(R.id.textViewContent)
        imageView = findViewById(R.id.imageViewContent)

        val filePath = intent.getStringExtra("file_path") ?: return
        val fileType = intent.getStringExtra("file_type") ?: return

        val file = File(filePath)
        supportActionBar?.title = file.name

        when (fileType) {
            "text" -> {
                displayTextFile(file)
            }
            "image" -> {
                displayImageFile(file)
            }
        }
    }

    private fun displayTextFile(file: File) {
        textView.visibility = View.VISIBLE
        imageView.visibility = View.GONE

        try {
            val content = file.readText()
            textView.text = content
        } catch (e: Exception) {
            textView.text = "Lỗi đọc file: ${e.message}"
        }
    }

    private fun displayImageFile(file: File) {
        textView.visibility = View.GONE
        imageView.visibility = View.VISIBLE

        try {
            val bitmap = BitmapFactory.decodeFile(file.absolutePath)
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap)
                imageView.scaleType = ImageView.ScaleType.CENTER_INSIDE
                imageView.adjustViewBounds = true
            } else {
                textView.visibility = View.VISIBLE
                textView.text = "Không thể đọc file ảnh"
            }
        } catch (e: Exception) {
            textView.visibility = View.VISIBLE
            textView.text = "Lỗi đọc ảnh: ${e.message}"
        }
    }
}
