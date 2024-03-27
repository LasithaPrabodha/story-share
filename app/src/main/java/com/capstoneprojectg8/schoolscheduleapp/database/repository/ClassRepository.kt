package com.capstoneprojectg8.schoolscheduleapp.database.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.capstoneprojectg8.schoolscheduleapp.database.ClassesDatabase
import com.capstoneprojectg8.schoolscheduleapp.database.dao.ClassDao
import com.capstoneprojectg8.schoolscheduleapp.database.dao.ClassSlotDao
import com.capstoneprojectg8.schoolscheduleapp.database.entities.AssignmentEntity
import com.capstoneprojectg8.schoolscheduleapp.database.entities.ClassEntity
import com.capstoneprojectg8.schoolscheduleapp.database.entities.ClassSlotEntity
import com.capstoneprojectg8.schoolscheduleapp.models.Assignment
import com.capstoneprojectg8.schoolscheduleapp.models.ClassSlot
import com.capstoneprojectg8.schoolscheduleapp.models.Class
import com.capstoneprojectg8.schoolscheduleapp.utils.fromEntity
import javax.inject.Inject

class ClassRepository @Inject constructor(
    private val classesDao: ClassDao,
    private val classSlotDao: ClassSlotDao
) {

    suspend fun addClass(classes: ClassEntity) = classesDao.addClass(classes)

    suspend fun editClass(classes: ClassEntity) = classesDao.editClass(classes)

    suspend fun deleteClass(classes: ClassEntity) = classesDao.deleteClass(classes)

    suspend fun addAssignment(assignmentEntity: AssignmentEntity) =
        classesDao.addAssignment(assignmentEntity)

    suspend fun editAssignment(assignmentEntity: AssignmentEntity) =
        classesDao.editAssignment(assignmentEntity)

    suspend fun deleteAssignment(assignmentEntity: AssignmentEntity) =
        classesDao.deleteAssignment(assignmentEntity)

    fun getAllClasses(): LiveData<List<Class>> =
        classesDao.getAllClasses().map { list -> list.map { it.fromEntity() } }

    fun getAssignmentListByClass(id: Int): LiveData<List<Assignment>> =
        classesDao.getAssignmentListByClass(id).map { list -> list.map { it.fromEntity() } }

    fun getDefaultListValue(id: Int): LiveData<ClassSlot> =
        classesDao.getDefaultListValue(id).map { list -> list.fromEntity() }

    suspend fun addClassSlot(classSlot: ClassSlotEntity) =
        classSlotDao.addClassSlot(classSlot)

    suspend fun editClassSlot(classSlot: ClassSlotEntity) =
        classSlotDao.editClassSlot(classSlot)

    suspend fun deleteClassSlot(classSlot: ClassSlotEntity) =
        classSlotDao.deleteClassSlot(classSlot)

    fun getAllClassSlots() =
        classSlotDao.getAllClassSlots().map { list -> list.map { it.fromEntity() } }
}