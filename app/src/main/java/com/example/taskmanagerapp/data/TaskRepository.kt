package com.example.taskmanagerapp.data

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.taskmanagerapp.models.Task
import com.example.taskmanagerapp.utils.ReminderWorker
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

class TaskRepository {

    private val db = FirebaseFirestore.getInstance()
    private val taskCollection = db.collection("tasks")

    fun addTask(task: Task, context: Context) {
        db.collection("tasks")
            .add(task)
            .addOnSuccessListener {
                val id = it.id
                val updatedTask = task.copy(id = id)

                taskCollection.document(id).set(updatedTask)
                scheduleReminder(updatedTask, context)
            }
            .addOnFailureListener {
                Log.e("TaskRepository", "Error adding task", it)
            }
    }

    private fun scheduleReminder(task: Task, context: Context) {
        val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val reminderDate = try {
            formatter.parse(task.reminder)
        } catch (e: Exception) {
            e.printStackTrace()
            return
        }

        val delay = reminderDate.time - System.currentTimeMillis()
        if (delay <= 0) return

        val data = Data.Builder()
            .putString("title", task.title)
            .putString("description", task.description)
            .build()

        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .build()

        WorkManager.getInstance(context).enqueue(request)
    }


    fun updateTask(task: Task) {
        db.collection("tasks").document(task.id)
            .set(task)
            .addOnSuccessListener {
                Log.d("TaskRepository", "Task updated successfully")
            }
            .addOnFailureListener {
                Log.e("TaskRepository", "Error updating task", it)
            }
    }

    fun deleteTask(taskId: String, onResult: (Boolean) -> Unit) {
        taskCollection.document(taskId)
            .delete()
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }

    fun getTasks(): LiveData<List<Task>> {
        val taskListLiveData = MutableLiveData<List<Task>>()
        val tasks: MutableList<Task> = ArrayList()
        taskCollection.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w("TaskRepository", "Listen failed.", e)
                return@addSnapshotListener
            }

            tasks.clear()
            for (doc in snapshot!!) {
                val task = doc.toObject(Task::class.java)
                tasks.add(task)
            }
            taskListLiveData.postValue(tasks)
        }
        return taskListLiveData
    }
}
