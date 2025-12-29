package com.example.filemanagement

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.ContextMenu
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.appbar.MaterialToolbar
import java.io.File
import java.io.FileWriter
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var listView: ListView
    private lateinit var textCurrentPath: TextView
    private lateinit var currentDirectory: File
    private lateinit var adapter: FileListAdapter
    private val items = mutableListOf<FileListItem>()
    private lateinit var toolbar: MaterialToolbar

    companion object {
        private const val PERMISSION_REQUEST_CODE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = getString(R.string.title_file_manager)

        listView = findViewById(R.id.listViewFiles)
        textCurrentPath = findViewById(R.id.textCurrentPath)
        adapter = FileListAdapter(this, items)
        listView.adapter = adapter

        // Kiểm tra và yêu cầu quyền truy cập storage
        if (checkStoragePermission()) {
            initializeFileSystem()
        } else {
            requestStoragePermission()
        }

        // Click vào item để mở thư mục hoặc xem file
        listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val item = items.getOrNull(position) ?: return@OnItemClickListener
            when (item.kind) {
                FileListItem.Kind.UP,
                FileListItem.Kind.DIRECTORY -> navigateToDirectory(item.file)
                FileListItem.Kind.FILE -> openFile(item.file)
            }
        }

        // Đăng ký context menu
        registerForContextMenu(listView)
    }

    private fun checkStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestStoragePermission() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }
        ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initializeFileSystem()
            } else {
                Toast.makeText(this, "Cần quyền truy cập storage để sử dụng ứng dụng", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun initializeFileSystem() {
        // Lấy thư mục external storage (sdcard)
        val externalStorage = Environment.getExternalStorageDirectory()
        if (externalStorage != null && externalStorage.exists()) {
            currentDirectory = externalStorage
            loadFiles()
        } else {
            Toast.makeText(this, "Không thể truy cập external storage", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadFiles() {
        items.clear()

        // Thêm nút ".." để quay lại thư mục cha (nếu không phải root)
        if (currentDirectory.parent != null && currentDirectory.parentFile != null) {
            items.add(
                FileListItem(
                    title = "..",
                    file = currentDirectory.parentFile!!,
                    kind = FileListItem.Kind.UP
                )
            )
        }

        // Lấy danh sách file và thư mục
        val files = currentDirectory.listFiles()
        if (files != null) {
            // Sắp xếp: thư mục trước, file sau
            val directories = files.filter { it.isDirectory }.sortedBy { it.name.lowercase() }
            val fileList = files.filter { it.isFile }.sortedBy { it.name.lowercase() }

            // Thêm thư mục vào danh sách
            directories.forEach { dir ->
                items.add(
                    FileListItem(
                        title = dir.name,
                        file = dir,
                        kind = FileListItem.Kind.DIRECTORY
                    )
                )
            }

            // Thêm file vào danh sách
            fileList.forEach { file ->
                items.add(
                    FileListItem(
                        title = file.name,
                        file = file,
                        kind = FileListItem.Kind.FILE
                    )
                )
            }
        }

        // Cập nhật title với đường dẫn hiện tại
        textCurrentPath.text = currentDirectory.absolutePath

        adapter.notifyDataSetChanged()
    }

    private fun navigateToDirectory(directory: File) {
        currentDirectory = directory
        loadFiles()
    }

    private fun openFile(file: File) {
        val extension = file.extension.lowercase()
        when (extension) {
            "txt" -> {
                // Mở file text
                val intent = Intent(this, ViewFileActivity::class.java)
                intent.putExtra("file_path", file.absolutePath)
                intent.putExtra("file_type", "text")
                startActivity(intent)
            }
            "jpg", "jpeg", "png", "bmp" -> {
                // Mở file ảnh
                val intent = Intent(this, ViewFileActivity::class.java)
                intent.putExtra("file_path", file.absolutePath)
                intent.putExtra("file_type", "image")
                startActivity(intent)
            }
            else -> {
                Toast.makeText(this, "Không hỗ trợ xem loại file này", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_create_folder -> {
                showCreateFolderDialog()
                true
            }
            R.id.action_create_file -> {
                showCreateFileDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showCreateFolderDialog() {
        val input = android.widget.EditText(this)
        input.hint = "Tên thư mục"

        AlertDialog.Builder(this)
            .setTitle("Tạo thư mục mới")
            .setView(input)
            .setPositiveButton("Tạo") { _, _ ->
                val folderName = input.text.toString().trim()
                if (folderName.isNotEmpty()) {
                    createFolder(folderName)
                } else {
                    Toast.makeText(this, "Tên thư mục không được để trống", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun showCreateFileDialog() {
        val input = android.widget.EditText(this)
        input.hint = "Tên file (ví dụ: example.txt)"

        AlertDialog.Builder(this)
            .setTitle("Tạo file văn bản mới")
            .setView(input)
            .setPositiveButton("Tạo") { _, _ ->
                val fileName = input.text.toString().trim()
                if (fileName.isNotEmpty()) {
                    createTextFile(fileName)
                } else {
                    Toast.makeText(this, "Tên file không được để trống", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun createFolder(folderName: String) {
        val newFolder = File(currentDirectory, folderName)
        if (newFolder.exists()) {
            Toast.makeText(this, "Thư mục đã tồn tại", Toast.LENGTH_SHORT).show()
            return
        }

        if (newFolder.mkdirs()) {
            Toast.makeText(this, "Tạo thư mục thành công", Toast.LENGTH_SHORT).show()
            loadFiles()
        } else {
            Toast.makeText(this, "Không thể tạo thư mục", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createTextFile(fileName: String) {
        val newFile = File(currentDirectory, fileName)
        if (newFile.exists()) {
            Toast.makeText(this, "File đã tồn tại", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            if (newFile.createNewFile()) {
                // Ghi nội dung mặc định
                FileWriter(newFile).use { it.write("") }
                Toast.makeText(this, "Tạo file thành công", Toast.LENGTH_SHORT).show()
                loadFiles()
            } else {
                Toast.makeText(this, "Không thể tạo file", Toast.LENGTH_SHORT).show()
            }
        } catch (e: IOException) {
            Toast.makeText(this, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateContextMenu(
        menu: ContextMenu?,
        v: View?,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)

        val info = menuInfo as? AdapterView.AdapterContextMenuInfo ?: return
        val position = info.position
        val item = items.getOrNull(position) ?: return
        val file = item.file

        // Bỏ qua nút ".."
        if (item.kind == FileListItem.Kind.UP) return

        menuInflater.inflate(
            if (file.isDirectory) R.menu.context_menu_folder else R.menu.context_menu_file,
            menu
        )
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val info = item.menuInfo as? AdapterView.AdapterContextMenuInfo
        val position = info?.position ?: return false
        val selected = items.getOrNull(position) ?: return false
        if (selected.kind == FileListItem.Kind.UP) return false
        val file = selected.file

        return when (item.itemId) {
            R.id.action_rename -> {
                showRenameDialog(file)
                true
            }
            R.id.action_delete -> {
                showDeleteDialog(file)
                true
            }
            R.id.action_copy -> {
                if (!file.isDirectory) {
                    showCopyDialog(file)
                }
                true
            }
            else -> super.onContextItemSelected(item)
        }
    }

    private fun showRenameDialog(file: File) {
        val input = android.widget.EditText(this)
        input.setText(file.name)
        input.selectAll()

        AlertDialog.Builder(this)
            .setTitle("Đổi tên")
            .setView(input)
            .setPositiveButton("Đổi tên") { _, _ ->
                val newName = input.text.toString().trim()
                if (newName.isNotEmpty() && newName != file.name) {
                    renameFile(file, newName)
                } else if (newName == file.name) {
                    Toast.makeText(this, "Tên mới giống tên cũ", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Tên không được để trống", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun showDeleteDialog(file: File) {
        val message = if (file.isDirectory) {
            "Bạn có chắc chắn muốn xóa thư mục '${file.name}'?"
        } else {
            "Bạn có chắc chắn muốn xóa file '${file.name}'?"
        }

        AlertDialog.Builder(this)
            .setTitle("Xác nhận xóa")
            .setMessage(message)
            .setPositiveButton("Xóa") { _, _ ->
                deleteFile(file)
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun showCopyDialog(file: File) {
        val input = android.widget.EditText(this)
        input.hint = "Đường dẫn thư mục đích"

        AlertDialog.Builder(this)
            .setTitle("Sao chép file: ${file.name}")
            .setMessage("Nhập đường dẫn thư mục đích:")
            .setView(input)
            .setPositiveButton("Sao chép") { _, _ ->
                val destPath = input.text.toString().trim()
                if (destPath.isNotEmpty()) {
                    copyFile(file, destPath)
                } else {
                    Toast.makeText(this, "Đường dẫn không được để trống", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun renameFile(file: File, newName: String) {
        val newFile = File(file.parent, newName)
        if (newFile.exists()) {
            Toast.makeText(this, "Tên đã tồn tại", Toast.LENGTH_SHORT).show()
            return
        }

        if (file.renameTo(newFile)) {
            Toast.makeText(this, "Đổi tên thành công", Toast.LENGTH_SHORT).show()
            loadFiles()
        } else {
            Toast.makeText(this, "Không thể đổi tên", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteFile(file: File) {
        val success = if (file.isDirectory) {
            deleteDirectory(file)
        } else {
            file.delete()
        }

        if (success) {
            Toast.makeText(this, "Xóa thành công", Toast.LENGTH_SHORT).show()
            loadFiles()
        } else {
            Toast.makeText(this, "Không thể xóa", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteDirectory(directory: File): Boolean {
        if (directory.exists()) {
            val files = directory.listFiles()
            if (files != null) {
                for (file in files) {
                    if (file.isDirectory) {
                        deleteDirectory(file)
                    } else {
                        file.delete()
                    }
                }
            }
        }
        return directory.delete()
    }

    private fun copyFile(file: File, destPath: String) {
        try {
            val destDir = File(destPath)
            if (!destDir.exists() || !destDir.isDirectory) {
                Toast.makeText(this, "Thư mục đích không tồn tại", Toast.LENGTH_SHORT).show()
                return
            }

            val destFile = File(destDir, file.name)
            if (destFile.exists()) {
                Toast.makeText(this, "File đã tồn tại trong thư mục đích", Toast.LENGTH_SHORT).show()
                return
            }

            file.copyTo(destFile, overwrite = false)
            Toast.makeText(this, "Sao chép thành công", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
