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
import java.lang.Math.pow
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min


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

        val childEventListener = object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {
                Log.d("TAG", "onChildAdded:" + dataSnapshot.key!!)
//                goals = dataSnapshot.value as HashMap<String?, Goal>
                updatePigHappiness()
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, previousChildName: String?) {
                Log.d("TAG", "onChildChanged: ${dataSnapshot.key}")
//                val newGoal = dataSnapshot.value as Goal
//                goals[dataSnapshot.key] = newGoal
                updatePigHappiness()
            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                Log.d("TAG", "onChildRemoved:" + dataSnapshot.key!!)
//                val goal = dataSnapshot.value as Goal
//                goals.remove(dataSnapshot.key)
                updatePigHappiness()
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

                updatePigHappiness()
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w("TAG", "Failed to read account info.", error.toException())
            }
        })
    }

    fun updatePigHappiness() {
        var factor = 1.0

        // TODO: Goals factor

        // Control factor
        if (accountInfo.foodPlan != 0L && accountInfo.food30Days != 0L)
            factor *= max(0.8, min(1.2,
                1.0 * accountInfo.foodPlan / accountInfo.food30Days))
        if (accountInfo.transportPlan != 0L && accountInfo.transport30Days != 0L)
            factor *= max(0.8, min(1.2,
                1.0 * accountInfo.transportPlan / accountInfo.transport30Days))
        if (accountInfo.housingPlan != 0L && accountInfo.housing30Days != 0L)
            factor *= max(0.8, min(1.2,
                1.0 * accountInfo.housingPlan / accountInfo.housing30Days))
        if (accountInfo.shoppingPlan != 0L && accountInfo.shopping30Days != 0L)
            factor *= max(0.8, min(1.2,
                1.0 * accountInfo.shoppingPlan / accountInfo.shopping30Days))
        if (accountInfo.othersPlan != 0L && accountInfo.others30Days != 0L)
            factor *= max(0.8, min(1.2,
                1.0 * accountInfo.othersPlan / accountInfo.others30Days))
        if (accountInfo.savings30Days != 0L && accountInfo.savingsPlan != 0L)
            factor *= max(0.8, min(1.2,
                pow(1.0 * accountInfo.savings30Days / accountInfo.savingsPlan, 4.0)))
        Log.d("TAG", "Happiness factor: $factor")

        if (abs(factor - 1.0) < 1e-6) // No changes
            factor = 0.5

        if (pig_happiness != null) {
            pig_happiness.progress = (100 * factor).toInt()
            when {
                pig_happiness.progress <= 25 ->
                    image_pig.setImageDrawable(context?.getDrawable(R.drawable.ic_bigpig_sad))
                pig_happiness.progress <= 75 ->
                    image_pig.setImageDrawable(context?.getDrawable(R.drawable.ic_bigpig))
                else ->
                    image_pig.setImageDrawable(context?.getDrawable(R.drawable.ic_bigpig_happy))
            }
        }
    }
}
