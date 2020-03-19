package com.nupanca

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.database.*
import com.nupanca.db.AccountInfo
import com.nupanca.db.Goal
import kotlinx.android.synthetic.main.fragment_main.*
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class MainFragment : BaseFragment() {
    var goals = hashMapOf<String?, Goal>()
    var accountInfo = AccountInfo()

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        handleFirebase()

        button_return.setOnClickListener {
            button_return.startAnimation(AnimationUtils.loadAnimation(
                context, R.anim.alpha_reduction))
        }

        button_edit.setOnClickListener {
            button_edit.startAnimation(AnimationUtils.loadAnimation(
                context, R.anim.alpha_reduction))
        }

        total_amount_layout.setOnClickListener {
            findNavController().navigate(R.id.action_MainFragment_to_TransfersFragment)
        }

        // TODO change back to goal start
        goals_layout.setOnClickListener {
            if (goals.isEmpty())
                findNavController().navigate(R.id.action_MainFragment_to_GoalStartFragment)
            else findNavController().navigate(R.id.action_MainFragment_to_GoalsListFragment)
        }

        control_layout.setOnClickListener {
            if (accountInfo.foodPlan < 1e-3 && accountInfo.housingPlan < 1e-3 &&
                accountInfo.transportPlan < 1e-3 && accountInfo.shoppingPlan < 1e-3 &&
                accountInfo.othersPlan < 1e-3 && accountInfo.savingsPlan < 1e-3)
                findNavController().navigate(R.id.action_MainFragment_to_ControlStartFragment)
            else findNavController().navigate(R.id.action_MainFragment_to_ControlFragment)
        }

        pig_happiness.setOnTouchListener { _, _ -> true }
    }

    override fun onBackPressed(): Boolean {
        val fm = activity!!.supportFragmentManager
        while (fm.popBackStackImmediate());
        return false
    }

    fun handleFirebase() {
        val db = FirebaseDatabase.getInstance()
        val goalListRef = db.getReference("goal_list")
        val accountInfoRef = db.getReference("account_info")
        //        pig_happiness.progress = 25
        //        image_pig.setImageDrawable(context?.getDrawable(R.drawable.ic_bigpig_sad))

        val childEventListener = object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {
                Log.d("TAG", "onChildAdded:" + dataSnapshot.key!!)
                goals = dataSnapshot.value as HashMap<String?, Goal>
                Log.d("TAG", "Goal size: ${goals.size}")
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, previousChildName: String?) {
                Log.d("TAG", "onChildChanged: ${dataSnapshot.key}")
                val newGoal = dataSnapshot.value as Goal
                goals[dataSnapshot.key] = newGoal
            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                // TODO verify this IGOR
//                Log.d("TAG", "onChildRemoved:" + dataSnapshot.key!!)
//                val goal = dataSnapshot.value as Goal
//                goals.remove(dataSnapshot.key)
            }

            override fun onChildMoved(dataSnapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("TAG", "postComments:onCancelled", databaseError.toException())
                Toast.makeText(context, "Erro ao carregar metas.",
                    Toast.LENGTH_SHORT).show()
            }
        }
        goalListRef.addChildEventListener(childEventListener)

        accountInfoRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Log.d("TAG", "Account Info is: ${dataSnapshot.value}")
                accountInfo = AccountInfo.fromMap(dataSnapshot.value as HashMap<String, Any>)
                Log.d("TAG", "Account Info is: $accountInfo")

                // Updating total amount
                val symb = DecimalFormatSymbols()
                symb.decimalSeparator = ','
                symb.groupingSeparator = '.'
                val df = DecimalFormat("###,##0.00", symb)
                total_amount?.text = df.format(accountInfo.savingsBalance)

                // TODO: Updating pig happiness
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w("TAG", "Failed to read account info.", error.toException())
            }
        })
    }
}
