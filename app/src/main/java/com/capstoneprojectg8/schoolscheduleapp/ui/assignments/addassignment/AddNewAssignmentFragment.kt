package com.capstoneprojectg8.schoolscheduleapp.ui.assignments.addassignment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.capstoneprojectg8.schoolscheduleapp.R
import com.capstoneprojectg8.schoolscheduleapp.database.ClassesDatabase
import com.capstoneprojectg8.schoolscheduleapp.databinding.FragmentAddNewAssignmentBinding
import com.capstoneprojectg8.schoolscheduleapp.models.Assignment
import com.capstoneprojectg8.schoolscheduleapp.repository.ClassesRepository
import com.capstoneprojectg8.schoolscheduleapp.ui.settings.classes.ClassesViewModel
import com.capstoneprojectg8.schoolscheduleapp.ui.settings.classes.ClassesViewModelFactory

class AddNewAssignmentFragment : Fragment() {

    private var _binding: FragmentAddNewAssignmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var assignmentViewModel: AddNewAssignmentViewModel
    private lateinit var classesViewModel: ClassesViewModel


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddNewAssignmentBinding.inflate(inflater, container, false)

        setUpClassesViewModel()
        setUpAssignmentsViewModel()

        val autocomplete = binding.autoCompleteClass

        classesViewModel.getAllClasses().observe(viewLifecycleOwner) { classes ->
            val classNamesAndCode = classes.map { "${it.classCode} - ${it.className}" }
            val adapter = ArrayAdapter(requireContext(), R.layout.list_class_item, classNamesAndCode)
            autocomplete.setAdapter(adapter)
        }

        autocomplete.onItemClickListener = AdapterView.OnItemClickListener { adapterView, _, i, _ ->
                val itemSelected = adapterView.getItemAtPosition(i)


        }

        binding.addNewAssignmentBtn.setOnClickListener {
            addAssignment()
        }

        binding.cancelAddAssignmentBtn.setOnClickListener {
            closeFragment()
        }

        return binding.root
    }

    private fun addAssignment() {
        val assignmentTitle = binding.assignmentTitleInputText.text.toString().trim()
        val assignmentDetail = binding.detailsTextInput.text.toString().trim()
        val isPriority = binding.setPriorityCheckBox.isChecked

        if (assignmentTitle.isNotEmpty()){
            val assignment = Assignment(0, assignmentTitle, assignmentDetail, isPriority, 1)
            assignmentViewModel.addAssignment(assignment)
            Toast.makeText(context, "Assignment added", Toast.LENGTH_LONG).show()
            closeFragment()
        } else {
            Toast.makeText(context, "Insert assignment title", Toast.LENGTH_LONG).show()
        }
    }

    private fun closeFragment() {
        findNavController().navigateUp()
    }


    private fun setUpClassesViewModel() {
        val classesRepository = ClassesRepository(ClassesDatabase(requireContext()))
        val viewModelProviderFactory = ClassesViewModelFactory(requireActivity().application, classesRepository)
        classesViewModel = ViewModelProvider(this, viewModelProviderFactory)[ClassesViewModel::class.java]
    }

    private fun setUpAssignmentsViewModel() {
        val classesRepository = ClassesRepository(ClassesDatabase(requireContext()))
        val viewModelProviderFactory = AddNewAssignmentViewModelProvider(requireActivity().application, classesRepository)
        assignmentViewModel = ViewModelProvider(this, viewModelProviderFactory)[AddNewAssignmentViewModel::class.java]
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}