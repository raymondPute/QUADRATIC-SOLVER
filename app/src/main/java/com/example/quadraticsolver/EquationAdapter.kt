package com.example.quadraticsolver

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class EquationAdapter : ListAdapter<Equation, EquationAdapter.EquationViewHolder>(EquationDiffCallback()) {

    class EquationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val equationText: TextView = itemView.findViewById(android.R.id.text1)
        fun bind(equation: Equation) {
            equationText.text = "${equation.a}x² + ${equation.b}x + ${equation.c} = 0\n${equation.result}\n\nSteps:\n${equation.steps}"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EquationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_1, parent, false)
        return EquationViewHolder(view)
    }

    override fun onBindViewHolder(holder: EquationViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class EquationDiffCallback : DiffUtil.ItemCallback<Equation>() {
    override fun areItemsTheSame(oldItem: Equation, newItem: Equation): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Equation, newItem: Equation): Boolean {
        return oldItem == newItem
    }
}