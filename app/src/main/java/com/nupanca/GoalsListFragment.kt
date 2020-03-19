package com.nupanca

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.annotation.RequiresApi
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.*
import com.nupanca.db.Goal
import kotlinx.android.synthetic.main.fragment_goals_list.*
import kotlin.collections.HashMap


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class GoalsListFragment : BaseFragment() {
    var goalAdapter: GoalAdapter? = null
    val goals =  HashMap<String?, Goal>()
    private lateinit var database: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        database = FirebaseDatabase.getInstance().getReference("goal_list")

        val childEventListener = object : ChildEventListener {
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
        }
        database.addChildEventListener(childEventListener)

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_goals_list, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getView()?.let { ViewCompat.setTranslationZ(it, 1f) }
        //TODO fix no adapter attached
//        val goalAdapter = goalList?.let { GoalAdapter(it) }
//
//        goals_list.adapter = goalAdapter
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
}
