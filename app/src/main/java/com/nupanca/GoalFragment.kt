package com.nupanca

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.nupanca.db.Goal
import java.text.DecimalFormat


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
            R.layout.fragment_goal, parent, false)
        return ViewHolder(goalView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val goal = goals[position]
        val df = DecimalFormat("#,00")
        val text = "você já acumulou R$ " + df.format(goal.totalAmount) +
                " e deve \ncompletar sua meta no dia " + goal.predictedEndDate.toString()

        holder.titleTextView.text = goal.title
        holder.textTextView.text = text
        holder.barView.progress = (100 * goal.currentAmount / goal.totalAmount).toInt()
    }

    override fun getItemCount() = goals.size

    fun addGoal(goal: Goal) {
        Log.d("TAG", "adding goal")
        goals.add(goal)
        this.notifyItemInserted(goals.size)
    }
}

class GoalFragment : BaseFragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_goal, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getView()?.let { ViewCompat.setTranslationZ(it, 1f) }
    }

    override fun onBackPressed(): Boolean {
        return false
    }
}