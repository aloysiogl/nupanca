package com.nupanca

import android.app.AlertDialog
import android.os.Build
import android.os.Bundle
import android.text.method.LinkMovementMethod
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
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class GoalsListFragment : BaseFragment() {
    var goalAdapter: GoalAdapter? = null
    val goals = HashMap<String?, Goal>()
    var accountInfo = AccountInfo()
    var db = FirebaseDatabase.getInstance()
    var goalListRef = db.getReference("goal_list")
    var accontInfoRef = db.getReference("account_info")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        goalListRef.addChildEventListener(object : ChildEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
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

        accontInfoRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                accountInfo = AccountInfo.fromMap(dataSnapshot.value as HashMap<String, Any>)
            }

            override fun onCancelled(p0: DatabaseError) {

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

    fun updateGoals() {
        val totalAmount = accountInfo.savingsBalance
        val savingsPerMonth = (accountInfo.savings30Days + accountInfo.savingsPlan) / 2
        val prioritiesValsPerMonth = doubleArrayOf(0.0, 0.0, 0.0, 0.0, 0.0)

        for (k in goals) {
            var startDate = k.value.beginDate?.let { Date(it) }
            var endDate = Date(k.value.endDate)
            val monthsToFinish = endDate.compareTo(startDate)  // TODO: Make this be an int
            prioritiesValsPerMonth[k.value.priority] += k.value.totalAmount / monthsToFinish
        }
    }
}
