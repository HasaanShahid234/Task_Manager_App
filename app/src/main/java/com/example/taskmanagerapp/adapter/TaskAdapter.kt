package com.example.taskmanagerapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.taskmanagerapp.databinding.ItemTaskBinding
import com.example.taskmanagerapp.models.Task

class TaskAdapter(
    private val onEdit: (Task) -> Unit,
    private val onDelete: (String) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    private val tasks = mutableListOf<Task>()

    inner class TaskViewHolder(val binding: ItemTaskBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]
        with(holder.binding) {
            tvTitle.text = task.title
            tvDesc.text = task.description
            time.text = task.reminder
            btnEdit.setOnClickListener { onEdit(task) }
            btnDelete.setOnClickListener { onDelete(task.id) }
        }
    }

    override fun getItemCount(): Int = tasks.size

    fun setTasks(newTasks: List<Task>) {
        tasks.clear()
        tasks.addAll(newTasks)
        notifyDataSetChanged()
    }

    fun getTask(position: Int): Task = tasks[position]
}
