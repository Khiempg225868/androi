package com.example.studentmanager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.studentmanager.databinding.FragmentUpdateStudentBinding

class UpdateStudentFragment : Fragment() {

    private val viewModel: StudentViewModel by activityViewModels()

    private lateinit var binding: FragmentUpdateStudentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_update_student, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val args = requireArguments()
        val student = args.getParcelable<Student>("student")
        val position = args.getInt("position", -1)

        if (student == null || position == -1) {
            Toast.makeText(requireContext(), "Dữ liệu sinh viên không hợp lệ", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
            return
        }

        binding.editTextMSSV.setText(student.mssv)
        binding.editTextName.setText(student.hoTen)
        binding.editTextPhone.setText(student.soDienThoai)
        binding.editTextAddress.setText(student.diaChi)

        binding.buttonUpdate.setOnClickListener {
            val mssv = binding.editTextMSSV.text.toString().trim()
            val hoTen = binding.editTextName.text.toString().trim()
            val soDienThoai = binding.editTextPhone.text.toString().trim()
            val diaChi = binding.editTextAddress.text.toString().trim()

            if (mssv.isEmpty() || hoTen.isEmpty() || soDienThoai.isEmpty() || diaChi.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val updatedStudent = Student(mssv, hoTen, soDienThoai, diaChi)
            viewModel.updateStudent(position, updatedStudent)
            findNavController().navigateUp()
        }
    }
}
