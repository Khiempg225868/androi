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
