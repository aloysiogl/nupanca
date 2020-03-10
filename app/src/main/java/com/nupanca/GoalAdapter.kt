package com.nupanca

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.navigation.Navigation.findNavController
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.nupanca.db.Goal
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

class GoalAdapter(private val goals: MutableList<Goal>):
        RecyclerView.Adapter<GoalAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView = itemView.findViewById<TextView>(R.id.goal_title)
        val textTextView = itemView.findViewById<TextView>(R.id.goal_text)
        val barView = itemView.findViewById<ProgressBar>(R.id.goal_progressBar)
    }

    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): GoalAdapter.ViewHolder {
        val goalView = LayoutInflater.from(parent.context).inflate(
            R.layout.fragment_goal_minimized, parent, false)

        goalView.setOnClickListener {
            parent.findNavController().navigate(R.id.action_GoalsListFragment_to_GoalFragment)
        }

        return ViewHolder(goalView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val goal = goals[position]
        val symb = DecimalFormatSymbols()
        symb.decimalSeparator = ','
        symb.groupingSeparator = '.'
        val df = DecimalFormat("###,##0.00", symb)
        val text = "você já acumulou R$ " + df.format(goal.totalAmount) +
                " e deve \ncompletar sua meta no dia " + goal.predictedEndDate.toString()

        holder.titleTextView.text = goal.title
        holder.textTextView.text = text
        holder.barView.progress = (100 * goal.currentAmount / goal.totalAmount).toInt()
    }

    override fun getItemCount() = goals.size

    fun addGoal(goal: Goal) {
        goals.add(goal)
        this.notifyItemInserted(goals.size)
    }
}
