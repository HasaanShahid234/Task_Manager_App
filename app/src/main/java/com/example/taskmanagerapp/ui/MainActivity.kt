package com.example.taskmanagerapp.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.taskmanagerapp.adapter.TaskAdapter
import com.example.taskmanagerapp.data.TaskViewModel
import com.example.taskmanagerapp.databinding.ActivityMainBinding
import com.example.taskmanagerapp.databinding.DialogAddTaskBinding
import com.example.taskmanagerapp.models.Task
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: TaskViewModel
    private lateinit var adapter: TaskAdapter
    private var selectedTimestamp: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[TaskViewModel::class.java]

        adapter = TaskAdapter(
            onEdit = { task -> showEditDialog(task) },
            onDelete = { taskId -> viewModel.deleteTask(taskId) }
        )

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        viewModel.taskList.observe(this) { tasks ->
            adapter.submitList(tasks)
        }

        binding.fabAdd.setOnClickListener {
            showAddDialog()
        }
    }

    private fun showAddDialog() {
        val dialogBinding = DialogAddTaskBinding.inflate(layoutInflater)
        selectedTimestamp = null

        val dialog = AlertDialog.Builder(this)
            .setTitle("Add Task")
            .setView(dialogBinding.root)
            .setPositiveButton("Add", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            val button = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            button.setOnClickListener {
                val title = dialogBinding.etTitle.text.toString().trim()
                val desc = dialogBinding.etDescription.text.toString().trim()

                if (title.isEmpty()) {
                    dialogBinding.etTitle.error = "Title required"
                    return@setOnClickListener
                }

                if (selectedTimestamp.isNullOrEmpty()) {
                    dialogBinding.btnPickDateTime.error = "Pick date & time"
                    return@setOnClickListener
                }

                val task = Task(
                    title = title,
                    description = desc,
                    reminder = selectedTimestamp!!
                )

                viewModel.addTask(task,this)
                dialog.dismiss()
            }
        }

        dialogBinding.btnPickDateTime.setOnClickListener {
            pickDateTime { timestamp ->
                selectedTimestamp = timestamp
                dialogBinding.btnPickDateTime.text = "Reminder: $timestamp"
                dialogBinding.btnPickDateTime.error = null
            }
        }

        dialog.show()
    }

    private fun showEditDialog(task: Task) {
        val dialogBinding = DialogAddTaskBinding.inflate(layoutInflater)
        dialogBinding.etTitle.setText(task.title)
        dialogBinding.etDescription.setText(task.description)
        selectedTimestamp = task.reminder

        dialogBinding.btnPickDateTime.text = "Reminder: ${task.reminder}"

        val dialog = AlertDialog.Builder(this)
            .setTitle("Edit Task")
            .setView(dialogBinding.root)
            .setPositiveButton("Update", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            val button = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            button.setOnClickListener {
                val updatedTitle = dialogBinding.etTitle.text.toString().trim()
                val updatedDesc = dialogBinding.etDescription.text.toString().trim()

                if (updatedTitle.isEmpty()) {
                    dialogBinding.etTitle.error = "Title required"
                    return@setOnClickListener
                }

                val updatedTask = task.copy(
                    title = updatedTitle,
                    description = updatedDesc,
                    reminder = selectedTimestamp ?: ""
                )

                viewModel.updateTask(updatedTask)
                dialog.dismiss()
            }
        }

        dialogBinding.btnPickDateTime.setOnClickListener {
            pickDateTime { timestamp ->
                selectedTimestamp = timestamp
                dialogBinding.btnPickDateTime.text = "Reminder: $timestamp"
            }
        }

        dialog.show()
    }

    private fun pickDateTime(onPicked: (String) -> Unit) {
        val now = Calendar.getInstance()

        DatePickerDialog(this, { _, year, month, day ->
            TimePickerDialog(this, { _, hour, minute ->
                val cal = Calendar.getInstance()
                cal.set(year, month, day, hour, minute, 0)
                val formatted = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                    .format(cal.time).toString()
                onPicked(formatted)
            }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), false).show()
        }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH)).show()
    }
}
