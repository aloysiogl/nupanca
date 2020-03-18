package com.nupanca

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.nupanca.db.Goal
import com.nupanca.db.GoalList
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

class GoalAdapter(private val goalList: GoalList):
        RecyclerView.Adapter<GoalAdapter.ViewHolder>() {
    var keyList = mutableListOf<String>()

    init {
        for (s in goalList.goals)
            s.key?.let { keyList.add(it) }
    }

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
        Log.d("TAG", "Position being viewed: $position")
        Log.d("TAG", "Key being viewed: ${keyList[position]}")
        Log.d("TAG", "Goal being viewed: ${goalList.goals[keyList[position]]}")
        val goal = goalList.goals[keyList[position]] as Goal
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

    override fun getItemCount() = keyList.size

    fun addGoal(goal: Goal) {
        Log.d("TAG", "Goal adapter adding goal")
        goalList.addGoal(goal)
        goal.key?.let { keyList.add(it) }
        Log.d("TAG", "KeyList: $keyList")
        Log.d("TAG", "Goal: $goal.key")
        this.notifyItemInserted(keyList.size)
    }
}
