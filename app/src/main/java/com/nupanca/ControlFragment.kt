package com.nupanca

import android.app.AlertDialog
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
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
import kotlinx.android.synthetic.main.fragment_control.*
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.ParseException
import kotlin.collections.HashMap
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.max


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class ControlFragment : BaseFragment() {
    var showSuggestions = false
    val db = FirebaseDatabase.getInstance()
    var accountInfoRef: DatabaseReference? = null
    var accountInfo = AccountInfo()
    val df: DecimalFormat
    val scaler = Scaler()
    val clusterPredictions = ClusterPredictions()
    var kmeansPredictor = KMeansPredictor()

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
        return inflater.inflate(R.layout.fragment_control, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getView()?.let { ViewCompat.setTranslationZ(it, 4f) }
        handleFirebase()

        button_return.setOnClickListener {
            button_return.startAnimation(
                AnimationUtils.loadAnimation(context, R.anim.alpha_reduction)
            )
            findNavController().navigate(R.id.action_ControlFragment_to_MainFragment)
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
            builder?.setMessage(R.string.control_info)
            val dialog: AlertDialog? = builder?.create()
            dialog?.show()

            val textView = dialog?.findViewById<View>(android.R.id.message) as TextView
            textView.typeface  = context?.let { it1 ->
                ResourcesCompat.getFont(
                    it1,
                    R.font.keep_calm_w01_book
                )
            }
            textView.linksClickable = true
            textView.movementMethod = LinkMovementMethod.getInstance()
        }

        button_suggestions.setOnClickListener {
            val vis: Int
            showSuggestions = !showSuggestions
            if (showSuggestions)
                vis = View.VISIBLE
            else vis = View.INVISIBLE
            updateIconsColors()

            food_suggestion_label.visibility = vis
            food_suggestion.visibility = vis
            transport_suggestion_label.visibility = vis
            transport_suggestion.visibility = vis
            housing_suggestion_label.visibility = vis
            housing_suggestion.visibility = vis
            shopping_suggestion_label.visibility = vis
            shopping_suggestion.visibility = vis
            others_suggestion_label.visibility = vis
            others_suggestion.visibility = vis
            savings_suggestion_label.visibility = vis
            savings_suggestion.visibility = vis
        }

        fun setHousing(v : Double) {
            accountInfo.housingPlan = v.toLong()
            accountInfoRef?.setValue(accountInfo)
        }
        housing_value.addTextChangedListener(
            CurrencyOnChangeListener(housing_value, ::setHousing, isInt = true))

        fun setTransport(v : Double) {
            accountInfo.transportPlan = v.toLong()
            accountInfoRef?.setValue(accountInfo)
        }
        transport_value.addTextChangedListener(
            CurrencyOnChangeListener(transport_value, ::setTransport, isInt = true))

        fun setFood(v : Double) {
            accountInfo.foodPlan = v.toLong()
            accountInfoRef?.setValue(accountInfo)
        }
        food_value.addTextChangedListener(
            CurrencyOnChangeListener(food_value, ::setFood, isInt = true))

        fun setShopping(v : Double) {
            accountInfo.shoppingPlan = v.toLong()
            accountInfoRef?.setValue(accountInfo)
        }
        shopping_value.addTextChangedListener(
            CurrencyOnChangeListener(shopping_value, ::setShopping, isInt = true))

        fun setOthers(v : Double) {
            accountInfo.othersPlan = v.toLong()
            accountInfoRef?.setValue(accountInfo)
        }
        others_value.addTextChangedListener(
            CurrencyOnChangeListener(others_value, ::setOthers, isInt = true))

        fun setSavings(v : Double) {
            accountInfo.savingsPlan = v.toLong()
            accountInfoRef?.setValue(accountInfo)
        }
        savings_value.addTextChangedListener(
            CurrencyOnChangeListener(savings_value, ::setSavings, isInt = true))
    }

    fun handleFirebase() {
        accountInfoRef?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Log.d("TAG", "Reading in control frag")
                accountInfo = AccountInfo.fromMap(dataSnapshot.value as HashMap<String, Any>)
                Log.d("TAG", "Account Info is: $accountInfo")
                updateIconsColors()

                if (food_value != null &&
                    !((food_value.text.toString() == "" && accountInfo.foodPlan < 1e-3) ||
                      abs(df.parse(food_value.text.toString()).toLong() - accountInfo.foodPlan) <
                            1e-3))
                    food_value.setText(df.format(accountInfo.foodPlan))
                food_spendings?.text = df.format(accountInfo.food30Days)

                if (transport_value != null &&
                    !((transport_value.text.toString() == "" && accountInfo.foodPlan < 1e-3) ||
                            abs(df.parse(transport_value.text.toString()).toLong() - accountInfo.transportPlan) <
                            1e-3))
                    transport_value?.setText(df.format(accountInfo.transportPlan))
                transport_spendings?.text = df.format(accountInfo.transport30Days)

                if (housing_value != null &&
                    !((housing_value.text.toString() == "" && accountInfo.foodPlan < 1e-3) ||
                            abs(df.parse(housing_value.text.toString()).toLong() - accountInfo.housingPlan) <
                            1e-3))
                    housing_value?.setText(df.format(accountInfo.housingPlan))
                housing_spendings?.text = df.format(accountInfo.housing30Days)

                if (shopping_value != null &&
                    !((shopping_value.text.toString() == "" && accountInfo.foodPlan < 1e-3) ||
                            abs(df.parse(shopping_value.text.toString()).toLong() - accountInfo.shoppingPlan) <
                            1e-3))
                    shopping_value?.setText(df.format(accountInfo.shoppingPlan))
                shopping_spendings?.text = df.format(accountInfo.shopping30Days)

                if (others_value != null &&
                    !((others_value.text.toString() == "" && accountInfo.foodPlan < 1e-3) ||
                            abs(df.parse(others_value.text.toString()).toLong() - accountInfo.othersPlan) <
                            1e-3))
                    others_value?.setText(df.format(accountInfo.othersPlan))
                others_spendings?.text = df.format(accountInfo.others30Days)

                if (savings_value != null &&
                    !((savings_value.text.toString() == "" && accountInfo.foodPlan < 1e-3) ||
                            abs(df.parse(savings_value.text.toString()).toLong() - accountInfo.savingsPlan) <
                            1e-3))
                    savings_value?.setText(df.format(accountInfo.savingsPlan))
                savings_spendings?.text = df.format(accountInfo.savings30Days)

                if (accountInfo.foodPlan > 1e-3)
                    food_icon?.drawable?.level =
                        (7000 * accountInfo.food30Days / accountInfo.foodPlan).toInt()
                else food_icon?.drawable?.level = 0
                if (accountInfo.transportPlan > 1e-3)
                    transport_icon?.drawable?.level =
                        (7000 * accountInfo.transport30Days / accountInfo.transportPlan).toInt()
                else transport_icon?.drawable?.level = 0
                if (accountInfo.housingPlan > 1e-3)
                    housing_icon?.drawable?.level =
                        (7000 * accountInfo.housing30Days / accountInfo.housingPlan).toInt()
                else housing_icon?.drawable?.level = 0
                if (accountInfo.shoppingPlan > 1e-3)
                    shopping_icon?.drawable?.level =
                        (7000 * accountInfo.shopping30Days / accountInfo.shoppingPlan).toInt()
                else shopping_icon?.drawable?.level = 0
                if (accountInfo.othersPlan > 1e-3)
                    others_icon?.drawable?.level =
                        (7000 * accountInfo.others30Days / accountInfo.othersPlan).toInt()
                else others_icon?.drawable?.level = 0
                if (accountInfo.savingsPlan > 1e-3)
                    savings_icon?.drawable?.level =
                        (7000 * accountInfo.savings30Days / accountInfo.savingsPlan).toInt()
                else savings_icon?.drawable?.level = 0
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w("TAG", "Failed to read account info.", error.toException())
            }
        })
    }

    override fun onBackPressed(): Boolean {
        button_return.performClick()
        return true
    }

    fun calculateSuggestions() {
        val input = scaler.scale(accountInfo)
        val cluster = kmeansPredictor.predict(input)
        val predictions = clusterPredictions.getPredictions(cluster)
        Log.d("TAG", "Predictions: $predictions")
        food_suggestion.text = predictions["FOOD"].toString()
        housing_suggestion.text = predictions["HOUSING"].toString()
        transport_suggestion.text = predictions["TRANSPORT"].toString()
        shopping_suggestion.text = predictions["SHOPPING"].toString()
        others_suggestion.text = predictions["OTHERS"].toString()
        savings_suggestion.text = predictions["SAVINGS"].toString()
    }

    fun updateIconsColors() {
        val colorGreen = ColorStateList.valueOf(resources.getColor(R.color.colorGreen))
        val colorRed = ColorStateList.valueOf(resources.getColor(R.color.colorRed))
        val colorWhite = ColorStateList.valueOf(resources.getColor(android.R.color.white))

        if (showSuggestions) {
            calculateSuggestions()
            button_suggestions_label.text = getString(R.string.suggestions_button_text_2)
        } else {
            button_suggestions_label?.text = getString(R.string.suggestions_button_text)
        }

        if (showSuggestions && df.parse(food_spendings.text.toString())?.toLong()!! >
            df.parse(food_value.text.toString())?.toLong()!!) {
            food_icon?.imageTintList = colorRed
            food_spendings?.setTextColor(colorRed)
        } else {
            food_icon?.imageTintList = colorGreen
            food_spendings?.setTextColor(colorWhite)
        }
        if (showSuggestions && df.parse(transport_spendings.text.toString())?.toLong()!! >
            df.parse(transport_value.text.toString())?.toLong()!!) {
            transport_icon?.imageTintList = colorRed
            transport_spendings?.setTextColor(colorRed)
        } else {
            transport_icon?.imageTintList = colorGreen
            transport_spendings?.setTextColor(colorWhite)
        }
        if (showSuggestions && df.parse(housing_spendings.text.toString())?.toLong()!! >
            df.parse(housing_value?.text.toString())?.toLong()!!) {
            housing_icon?.imageTintList = colorRed
            housing_spendings?.setTextColor(colorRed)
        } else {
            housing_icon?.imageTintList = colorGreen
            housing_spendings?.setTextColor(colorWhite)
        }
        if (showSuggestions && df.parse(shopping_spendings.text.toString())?.toLong()!! >
            df.parse(shopping_value.text.toString())?.toLong()!!) {
            shopping_icon?.imageTintList = colorRed
            shopping_spendings?.setTextColor(colorRed)
        } else {
            shopping_icon?.imageTintList = colorGreen
            shopping_spendings?.setTextColor(colorWhite)
        }
        if (showSuggestions && df.parse(others_spendings.text.toString())?.toLong()!! >
            df.parse(others_value.text.toString())?.toLong()!!) {
            others_icon?.imageTintList = colorRed
            others_spendings?.setTextColor(colorRed)
        } else {
            others_icon?.imageTintList = colorGreen
            others_spendings?.setTextColor(colorWhite)
        }
        if (showSuggestions && df.parse(savings_spendings.text.toString())?.toLong()!! <
            df.parse(savings_value.text.toString())?.toLong()!!) {
            savings_icon?.imageTintList = colorRed
            savings_spendings?.setTextColor(colorRed)
        } else {
            savings_icon?.imageTintList = colorGreen
            savings_spendings?.setTextColor(colorWhite)
        }
    }
}
