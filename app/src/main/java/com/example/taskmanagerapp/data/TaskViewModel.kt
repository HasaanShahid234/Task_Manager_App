package com.example.taskmanagerapp.data

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.taskmanagerapp.models.Task

class TaskViewModel : ViewModel() {

    private val repository = TaskRepository()
    val taskList: LiveData<List<Task>> = repository.getTasks()

    fun loadTasks() : LiveData<List<Task>> {
        return repository.getTasks()
    }

    fun addTask(task: Task,context: Context) {
        repository.addTask(task,context)
    }

    fun updateTask(task: Task) {
        repository.updateTask(task)
    }

    fun deleteTask(taskId: String) {
        repository.deleteTask(taskId) {
            if (it) loadTasks()
        }
    }
}
