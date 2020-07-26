package com.nupanca

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Build
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.*
import com.nupanca.db.AccountInfo
import com.nupanca.db.Goal
import kotlinx.android.synthetic.main.fragment_goals_list.*
import java.util.*
import kotlin.collections.HashMap


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class GoalsListFragment : BaseFragment() {
    val goals = HashMap<String?, Goal>()
    var db = FirebaseDatabase.getInstance()
    var dbRef: DatabaseReference? = null
    var accontInfoRef: DatabaseReference? = null
    var goalListRef :DatabaseReference? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        @SuppressLint("HardwareIds")
        dbRef = db.getReference((activity as MainActivity).androidId.toString())
        accontInfoRef = db.getReference((activity as MainActivity).androidId.toString())
            .child("account_info")
        goalListRef = db.getReference((activity as MainActivity).androidId.toString())
            .child("goal_list")

        goalListRef?.addChildEventListener(object : ChildEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, p1: String?) {
                val newGoal = Goal.fromMap(dataSnapshot.value as HashMap<String, Any>)
                goals[newGoal.key] = newGoal
                val multi = goals.values.toMutableList()

                if (goals_list != null)
                    goals_list.adapter = GoalAdapter(multi)
            }

            override fun onChildRemoved(p0: DataSnapshot) {
            }

            override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {
                val newGoal = Goal.fromMap(dataSnapshot.value as HashMap<String, Any>)
                goals[newGoal.key] = newGoal
                val multi = goals.values.toMutableList()

                if (goals_list != null)
                    goals_list.adapter = GoalAdapter(multi)
            }
        })

        accontInfoRef?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                accountInfo = AccountInfo.fromMap(dataSnapshot.value as HashMap<String, Any>)
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })

        dbRef?.addChildEventListener(object : ChildEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildRemoved(p0: DataSnapshot) {
            }

            override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {
                if (updateLazyRequest)
                    updateGoals(dataSnapshot, activity as MainActivity)
            }
        })

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_goals_list, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getView()?.let { ViewCompat.setTranslationZ(it, 1f) }

        goals_list.adapter = GoalAdapter(mutableListOf())
        goals_list.layoutManager = LinearLayoutManager(context)

        button_return.setOnClickListener {
            button_return.startAnimation(
                AnimationUtils.loadAnimation(context, R.anim.alpha_reduction)
            )
            findNavController().navigate(R.id.action_GoalsListFragment_to_MainFragment)
        }

        button_edit.setOnClickListener {
            button_edit.startAnimation(
                AnimationUtils.loadAnimation(
                    context, R.anim.alpha_reduction
                )
            )
            val builder: AlertDialog.Builder? = activity?.let {
                AlertDialog.Builder(it)
            }
            builder?.setMessage(R.string.goalsList_info)
            val dialog: AlertDialog? = builder?.create()
            dialog?.show()

            val textView = dialog?.findViewById<View>(android.R.id.message) as TextView
            textView.typeface = context?.let { it1 ->
                ResourcesCompat.getFont(
                    it1,
                    R.font.keep_calm_w01_book
                )
            }
            textView.linksClickable = true
            textView.movementMethod = LinkMovementMethod.getInstance()
        }

        button_add_item.setOnClickListener {
            val bundle = Bundle()
            bundle.putInt("mode", -1)
            findNavController().navigate(
                R.id.action_GoalsListFragment_to_GoalEditFragment,
                bundle
            )
        }
    }

    override fun onBackPressed(): Boolean {
        button_return.performClick()
        return true
    }

    companion object {
        var updateLazyRequest = false
        var accountInfo = AccountInfo()
        private val mothInMillis = 2.628e+9

        fun updateGoals(dataSnapshot: DataSnapshot, activity: MainActivity, targetGoalKey: String? = null) :Goal? {
            // Getting goals from snapshot
            if (dataSnapshot.key != "goal_list") return null

            updateLazyRequest = false

            val goals = mutableListOf<Goal>()

            for (firebaseGoal in dataSnapshot.children){
                val newGoal = Goal.fromMap(firebaseGoal.value as HashMap<String, Any>)
                goals.add(newGoal)
            }
            val totalAmount = accountInfo.savingsBalance
            val savingsPerMonth = (accountInfo.savings30Days.toDouble() +
                    accountInfo.savingsPlan.toDouble()) / 2.0
            val prioritiesValsPerMonth = doubleArrayOf(0.0, 0.0, 0.0, 0.0, 0.0)
            val prioritiesVals = doubleArrayOf(0.0, 0.0, 0.0, 0.0, 0.0)

            val goalValPerMonth = HashMap<Goal, Double>()
            val goalPredictedEndDate = HashMap<Goal,Long>()
            val goalTimeToFinish = HashMap<Goal,Double>()
            val goalCurrentValue = HashMap<Goal, Double>()
            val effectiveAmount = HashMap<Goal, Double>()

            for (goal in goals) {
                val startDate = goal.beginDate
                val endDate = goal.endDate
                goalTimeToFinish[goal] = endDate.toDouble() - startDate.toDouble()
                prioritiesVals[goal.priority] += goal.totalAmount
            }

            var availableValuePerMonth = savingsPerMonth
            var availableValue = totalAmount

            val valueForCategory = HashMap<Int, Double>()

            for (priority in 4 downTo 0){
                var valueForThisCategory = prioritiesVals[priority]
                availableValue -= prioritiesVals[priority]

                if (availableValue < 0) {
                    valueForThisCategory += availableValue
                    if (valueForThisCategory <= 1e-13)
                        valueForThisCategory = 0.0
                }

                valueForCategory[priority] = valueForThisCategory

                for (goal in goals)
                    if (goal.priority == priority)
                        effectiveAmount[goal] = goal.totalAmount - goal.totalAmount/prioritiesVals[priority]*valueForThisCategory
            }

            for (goal in goals){
                goalValPerMonth[goal] = effectiveAmount[goal]!! / goalTimeToFinish[goal]!!.toDouble() * mothInMillis
                prioritiesValsPerMonth[goal.priority] += goalValPerMonth[goal]!!
            }

            for (priority in 4 downTo 0){
                var valueForThisCategoryPerMonth = prioritiesValsPerMonth[priority]
                availableValuePerMonth -= prioritiesValsPerMonth[priority]

                if (availableValuePerMonth < 0) {
                    valueForThisCategoryPerMonth += availableValuePerMonth
                    if (valueForThisCategoryPerMonth <= 1e-13)
                        valueForThisCategoryPerMonth = 0.0
                }

                for (goal in goals){
                    if (goal.priority == priority){
                        val goalGivenValuePerMonth = goalValPerMonth[goal]?.div(prioritiesValsPerMonth[priority])
                            ?.times(valueForThisCategoryPerMonth)
//                    if(goalGivenValuePerMonth!! <= 1e-13){
//                        goalPredictedEndDate[goal] = -1
//                        goalCurrentValue[goal] = 0.0
//                        continue
//                    }
                        goalPredictedEndDate[goal] = (effectiveAmount[goal]?.div((goalGivenValuePerMonth!! /mothInMillis))
                                )!!.toLong() + goal.beginDate
                        goalCurrentValue[goal] = goal.totalAmount/prioritiesVals[priority]* valueForCategory[priority]!!
                        +effectiveAmount[goal]!! *(Calendar.getInstance().timeInMillis - goal.beginDate).toDouble()/
                                (goalPredictedEndDate[goal]!! - goal.beginDate).toDouble()

                        Log.d("My", effectiveAmount.toString() + " goal " + goal.title)
//                    Log.d("My1", Date(goalPredictedEndDate[goal]!!).toString())
                        Log.d("My2", goalCurrentValue[goal]!!.toString())
                    }
                }

            }

            var targetGoal: Goal? = null

            for (goal in goals){
                val goalRef = FirebaseDatabase.getInstance()
                    .getReference("${activity.androidId}/goal_list/${goal.key}")
                val newGoal = goal.copy()
                newGoal.predictedEndDate = goalPredictedEndDate[goal]!!
                newGoal.currentAmount = goalCurrentValue[goal]!!
                if (targetGoalKey != null && newGoal.key == targetGoalKey)
                    targetGoal = newGoal.copy()
                goalRef.setValue(newGoal)
            }

            return targetGoal
        }
    }
}
