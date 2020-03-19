package com.nupanca

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.core.view.ViewCompat
import androidx.navigation.fragment.findNavController
import com.google.firebase.database.*
import com.nupanca.db.Goal
import kotlinx.android.synthetic.main.fragment_goal.*
import java.util.*

/**
 * A simple [Fragment] subclass.
 * Use the [GoalFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class GoalFragment : BaseFragment() {
    private var goalKey : String? = null
    private var goal: Goal? = null
    // TODO check if this is really a good way to do this
    private lateinit var database: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_goal, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getView()?.let { ViewCompat.setTranslationZ(it, 2f) }

        // Reading arguments
        arguments?.let {
            val safeArgs = GoalFragmentArgs.fromBundle(it)
            goalKey = safeArgs.goalKey
        }

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
                if (newGoal.key == goalKey){
                    goal = newGoal
                    goal_fragment_title.text = newGoal.title
                }
            }
        }

        database.addChildEventListener(childEventListener)

        button_return.setOnClickListener {
            button_return.startAnimation(
                AnimationUtils.loadAnimation(context, R.anim.alpha_reduction)
            )
            findNavController().navigate(R.id.action_GoalFragment_to_GoalsListFragment)
        }

        button_edit.setOnClickListener {
            button_edit.startAnimation(
                AnimationUtils.loadAnimation(
                    context, R.anim.alpha_reduction
                )
            )
            val bundle = Bundle()
            bundle.putInt("mode", 0)
            bundle.putString("goal_key", goal?.key)
            findNavController().navigate(
                R.id.action_GoalFragment_to_GoalEditFragment,
                bundle
            )
        }
    }

    override fun onBackPressed(): Boolean {
        button_return.performClick()
        return true
    }
}
