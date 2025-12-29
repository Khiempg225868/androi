# BÁO CÁO: TÍCH HỢP SQLITE (ROOM) VÀO ỨNG DỤNG QUẢN LÝ SINH VIÊN

## 1. MỤC TIÊU

Cập nhật ứng dụng Quản lý sinh viên để lưu trữ dữ liệu sinh viên vào cơ sở dữ liệu SQLite sử dụng Room Persistence Library, thay vì chỉ lưu trong bộ nhớ tạm thời. Điều này giúp dữ liệu được lưu vĩnh viễn và không bị mất khi đóng ứng dụng.

---

## 2. CÁC BƯỚC THỰC HIỆN

### 2.1. Thêm Dependencies

#### 2.1.1. Cập nhật file `gradle/libs.versions.toml`

Thêm version cho Room và Coroutines:

```toml
room = "2.6.1"
coroutines = "1.7.3"
```

Thêm các thư viện Room và Coroutines vào phần `[libraries]`:

```toml
androidx-room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
androidx-room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "room" }
androidx-room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }
kotlinx-coroutines-android = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-android", version.ref = "coroutines" }
```

#### 2.1.2. Cập nhật file `app/build.gradle.kts`

Thêm plugin `kotlin-kapt`:

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-parcelize")
    id("kotlin-kapt")  // Thêm plugin này để xử lý Room annotation processing
}
```

Thêm dependencies vào phần `dependencies`:

```kotlin
// Room Database
implementation(libs.androidx.room.runtime)
implementation(libs.androidx.room.ktx)
kapt(libs.androidx.room.compiler)

// Coroutines
implementation(libs.kotlinx.coroutines.android)
```

### 2.2. Tạo Entity (StudentEntity.kt)

Tạo file `StudentEntity.kt` để định nghĩa cấu trúc bảng trong database:

**File: `app/src/main/java/com/example/studentmanager/StudentEntity.kt`**

```kotlin
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
```

**Giải thích:**
- `@Entity(tableName = "students")`: Đánh dấu class này là một Entity với tên bảng là "students"
- `@PrimaryKey`: Đánh dấu trường `mssv` là khóa chính
- Hàm `toStudent()`: Chuyển đổi từ `StudentEntity` sang `Student` (data class gốc)
- Hàm `fromStudent()`: Chuyển đổi từ `Student` sang `StudentEntity`

### 2.3. Tạo DAO (Data Access Object)

Tạo file `StudentDao.kt` để định nghĩa các thao tác với database:

**File: `app/src/main/java/com/example/studentmanager/StudentDao.kt`**

```kotlin
package com.example.studentmanager

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface StudentDao {
    @Query("SELECT * FROM students ORDER BY mssv ASC")
    fun getAllStudents(): LiveData<List<StudentEntity>>

    @Query("SELECT * FROM students WHERE mssv = :mssv")
    suspend fun getStudentByMssv(mssv: String): StudentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: StudentEntity)

    @Update
    suspend fun updateStudent(student: StudentEntity)

    @Delete
    suspend fun deleteStudent(student: StudentEntity)

    @Query("DELETE FROM students WHERE mssv = :mssv")
    suspend fun deleteStudentByMssv(mssv: String)
}
```

**Giải thích:**
- `@Dao`: Đánh dấu interface này là DAO
- `getAllStudents()`: Lấy tất cả sinh viên, trả về `LiveData` để tự động cập nhật UI
- `getStudentByMssv()`: Tìm sinh viên theo MSSV
- `insertStudent()`: Thêm sinh viên mới, sử dụng `REPLACE` strategy để ghi đè nếu trùng MSSV
- `updateStudent()`: Cập nhật thông tin sinh viên
- `deleteStudent()`: Xóa sinh viên
- `deleteStudentByMssv()`: Xóa sinh viên theo MSSV

### 2.4. Tạo Database Class

Tạo file `StudentDatabase.kt` để khởi tạo và quản lý database:

**File: `app/src/main/java/com/example/studentmanager/StudentDatabase.kt`**

```kotlin
package com.example.studentmanager

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [StudentEntity::class], version = 1, exportSchema = false)
abstract class StudentDatabase : RoomDatabase() {
    abstract fun studentDao(): StudentDao

    companion object {
        @Volatile
        private var INSTANCE: StudentDatabase? = null

        fun getDatabase(context: Context): StudentDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    StudentDatabase::class.java,
                    "student_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
```

**Giải thích:**
- `@Database`: Đánh dấu class này là Database, khai báo entities và version
- Singleton pattern: Đảm bảo chỉ có một instance database trong toàn bộ ứng dụng
- `getDatabase()`: Tạo hoặc trả về instance database đã tồn tại
- Tên database: `"student_database"`

### 2.5. Tạo Repository

Tạo file `StudentRepository.kt` để quản lý dữ liệu và xử lý coroutines:

**File: `app/src/main/java/com/example/studentmanager/StudentRepository.kt`**

```kotlin
package com.example.studentmanager

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class StudentRepository(application: Application) {
    private val studentDao: StudentDao = StudentDatabase.getDatabase(application).studentDao()
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val allStudents: LiveData<List<Student>> = studentDao.getAllStudents().map { entities ->
        entities.map { it.toStudent() }
    }

    fun insertStudent(student: Student) {
        repositoryScope.launch {
            studentDao.insertStudent(StudentEntity.fromStudent(student))
        }
    }

    fun updateStudent(student: Student) {
        repositoryScope.launch {
            studentDao.updateStudent(StudentEntity.fromStudent(student))
        }
    }

    fun deleteStudent(student: Student) {
        repositoryScope.launch {
            studentDao.deleteStudent(StudentEntity.fromStudent(student))
        }
    }

    fun deleteStudentByMssv(mssv: String) {
        repositoryScope.launch {
            studentDao.deleteStudentByMssv(mssv)
        }
    }

    suspend fun getStudentByMssv(mssv: String): Student? {
        return studentDao.getStudentByMssv(mssv)?.toStudent()
    }
}
```

**Giải thích:**
- Repository pattern: Tách biệt logic truy cập dữ liệu khỏi ViewModel
- `repositoryScope`: Coroutine scope riêng để xử lý các thao tác database trên background thread
- `allStudents`: Expose `LiveData<List<Student>>` để ViewModel quan sát
- Tất cả thao tác insert/update/delete chạy trên `Dispatchers.IO` để không block UI thread

### 2.6. Tạo Application Class

Tạo file `StudentApplication.kt` để khởi tạo Repository:

**File: `app/src/main/java/com/example/studentmanager/StudentApplication.kt`**

```kotlin
package com.example.studentmanager

import android.app.Application

class StudentApplication : Application() {
    val repository: StudentRepository by lazy {
        StudentRepository(this)
    }
}
```

**Giải thích:**
- `by lazy`: Khởi tạo Repository chỉ khi cần thiết
- Application class được khai báo trong `AndroidManifest.xml`

### 2.7. Cập nhật AndroidManifest.xml

Thêm `android:name=".StudentApplication"` vào thẻ `<application>`:

**File: `app/src/main/AndroidManifest.xml`**

```xml
<application
    android:name=".StudentApplication"
    android:allowBackup="true"
    android:dataExtractionRules="@xml/data_extraction_rules"
    android:fullBackupContent="@xml/backup_rules"
    android:icon="@mipmap/ic_launcher"
    android:label="@string/app_name"
    android:roundIcon="@mipmap/ic_launcher_round"
    android:supportsRtl="true"
    android:theme="@style/Theme.StudentManager"
    tools:targetApi="31">
    ...
</application>
```

### 2.8. Tạo ViewModelFactory

Tạo file `StudentViewModelFactory.kt` để tạo ViewModel với Application context:

**File: `app/src/main/java/com/example/studentmanager/StudentViewModelFactory.kt`**

```kotlin
package com.example.studentmanager

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class StudentViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StudentViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StudentViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
```

### 2.9. Cập nhật StudentViewModel

Cập nhật `StudentViewModel.kt` để sử dụng Repository thay vì lưu trong memory:

**File: `app/src/main/java/com/example/studentmanager/StudentViewModel.kt`**

```kotlin
package com.example.studentmanager

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class StudentViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: StudentRepository = (application as StudentApplication).repository

    val students: LiveData<List<Student>> = repository.allStudents

    fun addStudent(student: Student) {
        repository.insertStudent(student)
    }

    fun updateStudent(oldMssv: String, student: Student) {
        viewModelScope.launch {
            // Cập nhật bằng MSSV để đảm bảo đúng record trong database
            val updatedStudent = Student(
                mssv = oldMssv, // Giữ nguyên MSSV cũ (primary key)
                hoTen = student.hoTen,
                soDienThoai = student.soDienThoai,
                diaChi = student.diaChi
            )
            repository.updateStudent(updatedStudent)
        }
    }

    fun updateStudentByPosition(position: Int, student: Student) {
        viewModelScope.launch {
            val currentList = students.value ?: return@launch
            if (position in currentList.indices) {
                val oldStudent = currentList[position]
                updateStudent(oldStudent.mssv, student)
            }
        }
    }

    fun deleteStudent(position: Int) {
        viewModelScope.launch {
            val currentList = students.value ?: return@launch
            if (position in currentList.indices) {
                repository.deleteStudent(currentList[position])
            }
        }
    }

    fun getStudentByMssv(mssv: String, callback: (Student?) -> Unit) {
        viewModelScope.launch {
            val student = repository.getStudentByMssv(mssv)
            callback(student)
        }
    }
}
```

**Thay đổi chính:**
- Kế thừa `AndroidViewModel` thay vì `ViewModel` để có Application context
- Sử dụng `repository.allStudents` thay vì `MutableLiveData` trong memory
- Tất cả thao tác gọi qua Repository
- Sử dụng `viewModelScope` để quản lý coroutines

### 2.10. Cập nhật các Fragment

Cập nhật các Fragment để sử dụng `StudentViewModelFactory`:

**File: `app/src/main/java/com/example/studentmanager/StudentListFragment.kt`**

```kotlin
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.core.os.bundleOf

class StudentListFragment : Fragment() {

    private val viewModel: StudentViewModel by activityViewModels {
        StudentViewModelFactory(requireActivity().application)
    }
    
    // ... phần còn lại giữ nguyên
}
```

**File: `app/src/main/java/com/example/studentmanager/AddStudentFragment.kt`**

```kotlin
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.studentmanager.databinding.FragmentAddStudentBinding

class AddStudentFragment : Fragment() {

    private val viewModel: StudentViewModel by activityViewModels {
        StudentViewModelFactory(requireActivity().application)
    }
    
    // ... phần còn lại giữ nguyên
}
```

**File: `app/src/main/java/com/example/studentmanager/UpdateStudentFragment.kt`**

```kotlin
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.studentmanager.databinding.FragmentUpdateStudentBinding

class UpdateStudentFragment : Fragment() {

    private val viewModel: StudentViewModel by activityViewModels {
        StudentViewModelFactory(requireActivity().application)
    }
    
    // Cập nhật logic để MSSV không được chỉnh sửa
    binding.editTextMSSV.isEnabled = false
    
    // ... phần còn lại
}
```

---

## 3. KIẾN TRÚC TỔNG QUAN

### 3.1. Sơ đồ kiến trúc

```
┌─────────────────┐
│   Fragments     │
│ (UI Layer)      │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  ViewModel      │
│ (Logic Layer)   │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  Repository     │
│ (Data Layer)    │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│      DAO        │
│ (Database API)  │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│   Room Database │
│   (SQLite)      │
└─────────────────┘
```

### 3.2. Luồng dữ liệu

1. **Thêm sinh viên:**
   - User nhập thông tin → `AddStudentFragment`
   - Gọi `viewModel.addStudent()` → `repository.insertStudent()`
   - DAO thực hiện INSERT vào SQLite
   - `LiveData` tự động cập nhật → UI hiển thị sinh viên mới

2. **Cập nhật sinh viên:**
   - User chọn sinh viên → `UpdateStudentFragment`
   - Gọi `viewModel.updateStudentByPosition()` → `repository.updateStudent()`
   - DAO thực hiện UPDATE trong SQLite
   - `LiveData` tự động cập nhật → UI hiển thị thông tin mới

3. **Xóa sinh viên:**
   - User bấm nút xóa → `StudentListFragment`
   - Gọi `viewModel.deleteStudent()` → `repository.deleteStudent()`
   - DAO thực hiện DELETE trong SQLite
   - `LiveData` tự động cập nhật → UI xóa sinh viên khỏi danh sách

4. **Load dữ liệu khi mở app:**
   - `StudentViewModel` quan sát `repository.allStudents`
   - Repository lấy dữ liệu từ DAO
   - DAO query từ SQLite
   - `LiveData` tự động emit dữ liệu → UI hiển thị danh sách

---

## 4. CÁC FILE ĐÃ TẠO/CẬP NHẬT

### 4.1. Các file mới được tạo:

1. `StudentEntity.kt` - Entity class cho Room
2. `StudentDao.kt` - DAO interface
3. `StudentDatabase.kt` - Database class
4. `StudentRepository.kt` - Repository class
5. `StudentApplication.kt` - Application class
6. `StudentViewModelFactory.kt` - ViewModelFactory

### 4.2. Các file đã cập nhật:

1. `gradle/libs.versions.toml` - Thêm Room và Coroutines dependencies (Room sử dụng SQLite phía dưới)
2. `app/build.gradle.kts` - Thêm plugin `kotlin-kapt`, bật `dataBinding`, thêm dependencies Room
3. `Student.kt` - Model `Parcelable` để truyền dữ liệu giữa các màn hình (NavArgs/Bundle)
4. `StudentViewModel.kt` - Chuyển sang sử dụng Repository + `LiveData` để UI tự cập nhật
5. `StudentListFragment.kt` - Màn hình danh sách, quan sát `viewModel.students` và xóa/cập nhật
6. `AddStudentFragment.kt` - Màn hình thêm, gọi `viewModel.addStudent()` để INSERT vào SQLite
7. `UpdateStudentFragment.kt` - Màn hình sửa, MSSV khóa (primary key), gọi `viewModel.updateStudent...()` để UPDATE
8. `MainActivity.kt` + `activity_main.xml` - Host Navigation (NavHostFragment)
9. `res/navigation/nav_graph.xml` - Khai báo luồng điều hướng giữa các Fragment
10. `res/layout/fragment_student_list.xml` - Layout danh sách (ListView)
11. `res/layout/item_student.xml` - Layout từng dòng sinh viên (tên/MSSV + nút sửa/xóa)
12. `res/layout/fragment_add_student.xml` - Layout màn hình thêm (DataBinding)
13. `res/layout/fragment_update_student.xml` - Layout màn hình cập nhật (DataBinding)
14. `AndroidManifest.xml` - Khai báo `android:name=".StudentApplication"` để khởi tạo Repository/Room

### 4.3. Ghi chú về các file “legacy” (không nằm trong luồng Room hiện tại)

- `AddStudentActivity.kt` + `activity_add_student.xml`: luồng thêm sinh viên bằng `Intent/setResult` (không ghi DB)
- `DetailActivity.kt` + `activity_detail.xml`: luồng sửa sinh viên bằng `Intent/setResult` (không ghi DB)

> Ứng dụng hiện tại đang dùng **Navigation + Fragment** (`StudentListFragment`/`AddStudentFragment`/`UpdateStudentFragment`) để thao tác trực tiếp với **SQLite (Room)**, nên 2 Activity trên có thể coi là code cũ để tham khảo.

---

## 5. KẾT QUẢ ĐẠT ĐƯỢC

### 5.1. Lợi ích:

✅ **Dữ liệu lưu vĩnh viễn:** Dữ liệu sinh viên được lưu trong SQLite, không bị mất khi đóng app

✅ **Tự động cập nhật UI:** Sử dụng `LiveData` để tự động cập nhật giao diện khi dữ liệu thay đổi

✅ **Xử lý bất đồng bộ:** Sử dụng Coroutines để xử lý database trên background thread, không block UI

✅ **Kiến trúc rõ ràng:** Tách biệt các layer (UI, Logic, Data) theo mô hình MVVM + Repository

✅ **Dễ bảo trì:** Code được tổ chức tốt, dễ mở rộng và bảo trì

### 5.2. Chức năng hoạt động:

- ✅ Thêm sinh viên mới → Lưu vào database
- ✅ Cập nhật thông tin sinh viên → Cập nhật trong database
- ✅ Xóa sinh viên → Xóa khỏi database
- ✅ Hiển thị danh sách sinh viên → Tự động load từ database khi mở app
- ✅ MSSV không được chỉnh sửa (primary key)

---

## 6. KẾT LUẬN

Việc tích hợp SQLite (Room) vào ứng dụng Quản lý sinh viên đã được hoàn thành thành công. Ứng dụng giờ đây có khả năng lưu trữ dữ liệu vĩnh viễn, tự động cập nhật giao diện, và xử lý dữ liệu một cách hiệu quả thông qua kiến trúc MVVM + Repository pattern.

Dữ liệu sinh viên sẽ được lưu trong file database SQLite trên thiết bị, đảm bảo tính bền vững và khả năng phục hồi dữ liệu khi người dùng mở lại ứng dụng.

---

## 7. TÀI LIỆU THAM KHẢO

- [Android Room Persistence Library](https://developer.android.com/training/data-storage/room)
- [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html)
- [Android Architecture Components](https://developer.android.com/topic/architecture)
