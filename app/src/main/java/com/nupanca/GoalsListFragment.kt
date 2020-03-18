package com.nupanca

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.annotation.RequiresApi
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.nupanca.db.Goal
import com.nupanca.db.GoalList
import kotlinx.android.synthetic.main.fragment_goals_list.*
import java.time.LocalDate
import java.util.*


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class GoalsListFragment : BaseFragment() {
    var goalAdapter: GoalAdapter? = null
    var goalList: GoalList? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_goals_list, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getView()?.let { ViewCompat.setTranslationZ(it, 1f) }

        val goalList = context?.let { GoalList(it) }
        val goalAdapter = goalList?.let { GoalAdapter(it) }

        goals_list.adapter = goalAdapter
        goals_list.layoutManager = LinearLayoutManager(context)

        button_return.setOnClickListener {
            button_return.startAnimation(
                AnimationUtils.loadAnimation(context, R.anim.alpha_reduction)
            )
            findNavController().navigate(R.id.action_GoalsListFragment_to_MainFragment)
        }

        button_info.setOnClickListener {
            button_info.startAnimation(
                AnimationUtils.loadAnimation(
                    context, R.anim.alpha_reduction
                )
            )
        }

        button_add_item.setOnClickListener {
            val goal = Goal(
                title = "Carro Próprio",
                totalAmount = 3000.00,
                currentAmount = 1000.00,
                beginDate = System.currentTimeMillis(),
                endDate = System.currentTimeMillis(),
                predictedEndDate = System.currentTimeMillis(),
                priority = 1)
            goalAdapter?.addGoal(goal)
//            val bundle = Bundle()
//            bundle.putInt("mode", -1)
//            findNavController().navigate(
//                R.id.action_GoalsListFragment_to_goalEditFragment,
//                bundle
//            )
        }
    }

    override fun onBackPressed(): Boolean {
        button_return.performClick()
        return true
    }
}
