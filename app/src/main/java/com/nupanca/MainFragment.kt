package com.nupanca

import android.app.AlertDialog
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat.getFont
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.database.*
import com.nupanca.db.AccountInfo
import com.nupanca.db.Goal
import kotlinx.android.synthetic.main.fragment_main.*
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class MainFragment : BaseFragment() {
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

            val builder: AlertDialog.Builder? = activity?.let {
                AlertDialog.Builder(it)
            }
            builder?.setMessage(R.string.main_info)
            val dialog: AlertDialog? = builder?.create()
            dialog?.show()

            val textView = dialog?.findViewById<View>(android.R.id.message) as TextView
            textView.typeface  = context?.let { it1 -> getFont(it1, R.font.keep_calm_w01_book) }
            textView.linksClickable = true
            textView.movementMethod = LinkMovementMethod.getInstance()
        }

        total_amount_layout.setOnClickListener {
            findNavController().navigate(R.id.action_MainFragment_to_TransfersFragment)
        }

        goals_layout.setOnClickListener {
            if (goals.isEmpty())
                findNavController().navigate(R.id.action_MainFragment_to_GoalStartFragment)
            else {
                GoalsListFragment.updateLazyRequest = true
                findNavController().navigate(R.id.action_MainFragment_to_GoalsListFragment)
            }
        }

        control_layout.setOnClickListener {
            if (accountInfo.foodPlan < 1e-3 && accountInfo.housingPlan < 1e-3 &&
                accountInfo.transportPlan < 1e-3 && accountInfo.shoppingPlan < 1e-3 &&
                accountInfo.othersPlan < 1e-3 && accountInfo.savingsPlan < 1e-3)
                findNavController().navigate(R.id.action_MainFragment_to_PlanningStartFragment)
            else findNavController().navigate(R.id.action_MainFragment_to_PlanningFragment)
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
        val goalListRef =
            db.getReference("${(activity as MainActivity).androidId}/goal_list")
        val accountInfoRef =
            db.getReference("${(activity as MainActivity).androidId}/account_info")

        fun startDataBase(){
            val accountInfo = AccountInfo(
                accountBalance = getString(R.string.default_account_balance).toDouble(),
                age = getString(R.string.default_account_age).toLong(),
                food30Days = getString(R.string.default_account_food_30_days).toLong(),
                foodPlan = 0,
                housing30Days = getString(R.string.default_account_housing_30_days).toLong(),
                housingPlan = 0,
                income = getString(R.string.default_account_income).toDouble(),
                marriage = getString(R.string.default_account_marriage).toLong(),
                others30Days = getString(R.string.default_account_others_30_days).toLong(),
                othersPlan = 0,
                region = getString(R.string.default_account_region).toLong(),
                savings30Days = 0,
                savingsBalance = 0.00,
                savingsPlan = 0,
                sex = getString(R.string.default_account_sex).toLong(),
                transport30Days = getString(R.string.default_account_transport_30_days).toLong(),
                transportPlan = 0
            )
            accountInfoRef.setValue(accountInfo)
        }

        val childEventListener = object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {
                Log.d("TAG", "onChildAdded:" + dataSnapshot.key!!)
                goals[dataSnapshot.key] = Goal.fromMap(dataSnapshot.value as HashMap<String, Any>)
                updatePigHappiness()
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, previousChildName: String?) {
                Log.d("TAG", "onChildChanged: ${dataSnapshot.key}")
                goals[dataSnapshot.key] = Goal.fromMap(dataSnapshot.value as HashMap<String, Any>)
                updatePigHappiness()
            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                Log.d("TAG", "onChildRemoved:" + dataSnapshot.key!!)
                goals.remove(dataSnapshot.key)
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
                if (!dataSnapshot.exists()){
                    startDataBase()
                    return
                }
                accountInfo = AccountInfo.fromMap(dataSnapshot.value as HashMap<String, Any>)

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
        var factor = accountInfo.savingsBalance / 1000

        // Goals factor
        for (k in goals) {
            if (k.value.beginDate != null) {
                val dateRatio = 1.0 * (k.value.endDate - System.currentTimeMillis()) /
                        (k.value.endDate - k.value.beginDate!!)
                val amountRatio = 1.0 * (k.value.totalAmount - k.value.currentAmount) /
                        k.value.totalAmount
                factor *= max(0.8, min(1.2, amountRatio / dateRatio))
            }
        }

        Log.d("TAG", "Pig happiness short: $factor")

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
        if (accountInfo.savings30Days != 0L && accountInfo.savingsPlan != 0L) {
            factor *= if (accountInfo.savings30Days < 0)
                0.8.pow(2.0)
            else max(0.8, min(1.2,
                (1.0 * accountInfo.savings30Days / accountInfo.savingsPlan)
            )).pow(2.0)
        }
        Log.d("TAG", "Happiness factor: $factor")

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

    companion object {
        var goals = hashMapOf<String?, Goal>()
        var accountInfo = AccountInfo()
    }
}
