package com.nupanca

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.database.*
import com.nupanca.db.AccountInfo
import com.nupanca.db.ClusterPredictions
import com.nupanca.db.KMeansPredictor
import com.nupanca.db.Scaler
import kotlinx.android.synthetic.main.fragment_planning_edit.*
import java.lang.Math.abs
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class PlanningEditFragment : BaseFragment() {
    private var category: String? = null
    private val db = FirebaseDatabase.getInstance()
    private var accountInfoRef: DatabaseReference? = null
    private var accountInfo = AccountInfo()
    private val df: DecimalFormat
    private val scaler = Scaler()
    private val clusterPredictions = ClusterPredictions()
    private var kmeansPredictor = KMeansPredictor()

    init {
        val symb = DecimalFormatSymbols()
        symb.decimalSeparator = ','
        symb.groupingSeparator = '.'
        df = DecimalFormat("###,##0", symb)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Getting reference to account info in the database
        accountInfoRef =
            db.getReference("${(activity as MainActivity).androidId}/account_info")
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_planning_edit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getView()?.let { ViewCompat.setTranslationZ(it, 5f) }

        // Reading arguments
        arguments?.let {
            val safeArgs = PlanningEditFragmentArgs.fromBundle(it)
            category = safeArgs.category
        }

        handleFirebase()

        planning_edit_button_return.setOnClickListener {
            planning_edit_button_return.startAnimation(
                AnimationUtils.loadAnimation(context, R.anim.alpha_reduction)
            )
            findNavController().navigate(R.id.action_PlanningEditFragment_to_PlanningFragment)
        }

        planning_edit_button_info.setOnClickListener {
            planning_edit_button_info.startAnimation(
                AnimationUtils.loadAnimation(
                    context, R.anim.alpha_reduction
                )
            )
            val builder: AlertDialog.Builder? = activity?.let {
                AlertDialog.Builder(it)
            }
            builder?.setMessage(R.string.suggestion_info)
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

        if (category == "savings")
            planning_edit_this_month_label.text =
                context?.getString(R.string.planning_edit_saving_label)
        else planning_edit_this_month_label.text =
            context?.getString(R.string.planning_edit_spending_label)

        when (category) {
            "savings" -> {
                planning_edit_title.text = context?.getString(R.string.savings_title)
                planning_edit_icon.setImageResource(R.drawable.ic_pig)
            }
            "housing" -> {
                planning_edit_title.text = context?.getString(R.string.housing_title)
                planning_edit_icon.setImageResource(R.drawable.ic_house)
            }
            "transport" -> {
                planning_edit_title.text = context?.getString(R.string.transport_title)
                planning_edit_icon.setImageResource(R.drawable.ic_car)
            }
            "food" -> {
                planning_edit_title.text = context?.getString(R.string.food_title)
                planning_edit_icon.setImageResource(R.drawable.ic_fork)
            }
            "shopping" -> {
                planning_edit_title.text = context?.getString(R.string.shopping_title)
                planning_edit_icon.setImageResource(R.drawable.ic_fashion)
            }
            "others" -> {
                planning_edit_title.text = context?.getString(R.string.others_title)
                planning_edit_icon.setImageResource(R.drawable.ic_puzzle)
            }
        }

        class MyFocusChangeListener : View.OnFocusChangeListener {
            override fun onFocusChange(v: View, hasFocus: Boolean) {
                if (!hasFocus) {
                    val imm =
                        context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(v.windowToken, 0)
                }
            }
        }

        val focusChangeListener: View.OnFocusChangeListener = MyFocusChangeListener()

        fun configureEditText(input: EditText, updateFunc: (Long) -> Unit) {
            fun updateDb(v: Double) {
                updateFunc(v.toLong())
                accountInfoRef?.setValue(accountInfo)
            }
            input.addTextChangedListener(
                CurrencyOnChangeListener(input, ::updateDb, isInt = true)
            )
            input.onFocusChangeListener = focusChangeListener
        }

        val updatePlan = when (category) {
            "savings" -> fun (it: Long) { accountInfo.savingsPlan = it }
            "housing" -> fun (it: Long) { accountInfo.housingPlan = it }
            "transport" -> fun (it: Long) { accountInfo.transportPlan = it }
            "food" -> fun (it: Long) { accountInfo.foodPlan = it }
            "shopping" -> fun (it: Long) { accountInfo.shoppingPlan = it }
            "others" -> fun (it: Long) { accountInfo.othersPlan = it }
            else -> fun (_: Long) {}
        }

        configureEditText(planning_edit_value, updatePlan)
    }

    fun handleFirebase() {
        accountInfoRef?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                accountInfo = AccountInfo.fromMap(dataSnapshot.value as HashMap<String, Any>)

                if (context == null || category == null || planning_edit_value == null)
                    return

                val plan: Long
                val thisMonth: Long
                when (category) {
                    "savings" -> {
                        plan = accountInfo.savingsPlan
                        thisMonth = accountInfo.savings30Days
                    }
                    "housing" -> {
                        plan = accountInfo.housingPlan
                        thisMonth = accountInfo.housing30Days
                    }
                    "transport" -> {
                        plan = accountInfo.transportPlan
                        thisMonth = accountInfo.transport30Days
                    }
                    "food" -> {
                        plan = accountInfo.foodPlan
                        thisMonth = accountInfo.food30Days
                    }
                    "shopping" -> {
                        plan = accountInfo.shoppingPlan
                        thisMonth = accountInfo.shopping30Days
                    }
                    "others" -> {
                        plan = accountInfo.othersPlan
                        thisMonth = accountInfo.others30Days
                    }
                    else -> {
                        plan = 0
                        thisMonth = 0
                    }
                }

                if (abs(df.parse(planning_edit_value?.text.toString())!!.toLong() - plan) > 1e-3)
                    planning_edit_value?.setText(df.format(plan))

                planning_edit_this_month?.text = df.format(thisMonth)

                val input = scaler.scale(accountInfo)
                val cluster = kmeansPredictor.predict(input)
                val predictions = clusterPredictions.getPredictions(cluster)
                planning_edit_suggestion?.text = predictions[category?.toUpperCase()].toString()
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w("TAG", "Failed to read account info.", error.toException())
            }
        })
    }

    override fun onBackPressed(): Boolean {
        planning_edit_button_return.performClick()
        return true
    }
}