package com.nupanca

import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.nupanca.db.AccountInfo
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
    val db = FirebaseDatabase.getInstance();
    val accountInfoRef = db.getReference("account_info")
    var accountInfo = AccountInfo()
    val df: DecimalFormat

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
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_control, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getView()?.let { ViewCompat.setTranslationZ(it, 2f) }
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
        }

        button_suggestions.setOnClickListener {
            val vis: Int
            val colorGreen = ColorStateList.valueOf(resources.getColor(R.color.colorGreen))
            val colorRed = ColorStateList.valueOf(resources.getColor(R.color.colorRed))
            val colorWhite = ColorStateList.valueOf(resources.getColor(android.R.color.white))
            showSuggestions = !showSuggestions
            if (showSuggestions) {
                button_suggestions_label.text = getString(R.string.suggestions_button_text_2)
                vis = View.VISIBLE
                if (df.parse(food_spendings.text.toString())?.toLong()!! >
                    df.parse(food_value.text.toString())?.toLong()!!) {
                    food_icon.imageTintList = colorRed
                    food_spendings.setTextColor(colorRed)
                }
                if (df.parse(transport_spendings.text.toString())?.toLong()!! >
                    df.parse(transport_value.text.toString())?.toLong()!!) {
                    transport_icon.imageTintList = colorRed
                    transport_spendings.setTextColor(colorRed)
                }
                if (df.parse(housing_spendings.text.toString())?.toLong()!! >
                    df.parse(housing_value.text.toString())?.toLong()!!) {
                    housing_icon.imageTintList = colorRed
                    housing_spendings.setTextColor(colorRed)
                }
                if (df.parse(shopping_spendings.text.toString())?.toLong()!! >
                    df.parse(shopping_value.text.toString())?.toLong()!!) {
                    shopping_icon.imageTintList = colorRed
                    shopping_spendings.setTextColor(colorRed)
                }
                if (df.parse(others_spendings.text.toString())?.toLong()!! >
                    df.parse(others_value.text.toString())?.toLong()!!) {
                    others_icon.imageTintList = colorRed
                    others_spendings.setTextColor(colorRed)
                }
            } else {
                button_suggestions_label.text = getString(R.string.suggestions_button_text)
                vis = View.INVISIBLE
                food_icon.imageTintList = colorGreen
                food_spendings.setTextColor(colorWhite)
                transport_icon.imageTintList = colorGreen
                transport_spendings.setTextColor(colorWhite)
                housing_icon.imageTintList = colorGreen
                housing_spendings.setTextColor(colorWhite)
                shopping_icon.imageTintList = colorGreen
                shopping_spendings.setTextColor(colorWhite)
                others_icon.imageTintList = colorGreen
                others_spendings.setTextColor(colorWhite)
            }

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

        var firstTime = 6

        savings_value.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val curText = savings_value.text.toString()
                try {
                    if (curText == "") {
                        accountInfo.savingsPlan = 0
                        savings_value.setText("0")
                    } else {
                        accountInfo.savingsPlan = df.parse(savings_value.text.toString())?.toLong()!!
                        if (curText != df.format(accountInfo.savingsPlan))
                            savings_value.setText(df.format(accountInfo.savingsPlan))
                    }
                    accountInfoRef.setValue(accountInfo)
                } catch (e: ParseException) {
                    savings_value.setText(df.format(accountInfo.savingsPlan))
                }
                val prevL = curText.length
                val curL = savings_value.text!!.length
                val sel: Int
                sel = when {
                    curL > prevL -> min(curL, start+2)
                    curL < prevL -> if (curText[0] == '0') start
                    else min(curL, max(0, start-1))
                    else -> min(curL, start+1)
                }
                Log.d("TAG", "$prevL, $curL, $sel")
                savings_value.setSelection(sel)
                if (firstTime == 0)
                    savings_value.requestFocus()
                else firstTime--
            }
        })

        others_value.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val curText = others_value.text.toString()
                try {
                    if (curText == "") {
                        accountInfo.othersPlan = 0
                        others_value.setText("0")
                    } else {
                        accountInfo.othersPlan = df.parse(others_value.text.toString())?.toLong()!!
                        if (curText != df.format(accountInfo.othersPlan))
                            others_value.setText(df.format(accountInfo.othersPlan))
                    }
                    accountInfoRef.setValue(accountInfo)
                } catch (e: ParseException) {
                    others_value.setText(df.format(accountInfo.othersPlan))
                }
                val prevL = curText.length
                val curL = others_value.text!!.length
                val sel: Int
                sel = when {
                    curL > prevL -> min(curL, start+2)
                    curL < prevL -> if (curText[0] == '0') start
                    else min(curL, max(0, start-1))
                    else -> min(curL, start+1)
                }
                Log.d("TAG", "$prevL, $curL, $sel")
                others_value.setSelection(sel)
                if (firstTime == 0)
                    others_value.requestFocus()
                else firstTime--
            }
        })

        shopping_value.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val curText = shopping_value.text.toString()
                try {
                    if (curText == "") {
                        accountInfo.shoppingPlan = 0
                        shopping_value.setText("0")
                    } else {
                        accountInfo.shoppingPlan = df.parse(shopping_value.text.toString())?.toLong()!!
                        if (curText != df.format(accountInfo.shoppingPlan))
                            shopping_value.setText(df.format(accountInfo.shoppingPlan))
                    }
                    accountInfoRef.setValue(accountInfo)
                } catch (e: ParseException) {
                    shopping_value.setText(df.format(accountInfo.shoppingPlan))
                }
                val prevL = curText.length
                val curL = shopping_value.text!!.length
                val sel: Int
                sel = when {
                    curL > prevL -> min(curL, start+2)
                    curL < prevL -> if (curText[0] == '0') start
                    else min(curL, max(0, start-1))
                    else -> min(curL, start+1)
                }
                Log.d("TAG", "$prevL, $curL, $sel")
                shopping_value.setSelection(sel)
                if (firstTime == 0)
                    shopping_value.requestFocus()
                else firstTime--
            }
        })

        food_value.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val curText = food_value.text.toString()
                try {
                    if (curText == "") {
                        accountInfo.foodPlan = 0
                        food_value.setText("0")
                    } else {
                        accountInfo.foodPlan = df.parse(food_value.text.toString())?.toLong()!!
                        if (curText != df.format(accountInfo.foodPlan))
                            food_value.setText(df.format(accountInfo.foodPlan))
                    }
                    accountInfoRef.setValue(accountInfo)
                } catch (e: ParseException) {
                    food_value.setText(df.format(accountInfo.foodPlan))
                }
                val prevL = curText.length
                val curL = food_value.text!!.length
                val sel: Int
                sel = when {
                    curL > prevL -> min(curL, start+2)
                    curL < prevL -> if (curText[0] == '0') start
                    else min(curL, max(0, start-1))
                    else -> min(curL, start+1)
                }
                Log.d("TAG", "$prevL, $curL, $sel")
                food_value.setSelection(sel)
                if (firstTime == 0)
                    food_value.requestFocus()
                else firstTime--
            }
        })

        transport_value.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val curText = transport_value.text.toString()
                try {
                    if (curText == "") {
                        accountInfo.transportPlan = 0
                        transport_value.setText("0")
                    } else {
                        accountInfo.transportPlan = df.parse(transport_value.text.toString())?.toLong()!!
                        if (curText != df.format(accountInfo.transportPlan))
                            transport_value.setText(df.format(accountInfo.transportPlan))
                    }
                    accountInfoRef.setValue(accountInfo)
                } catch (e: ParseException) {
                    transport_value.setText(df.format(accountInfo.transportPlan))
                }
                val prevL = curText.length
                val curL = transport_value.text!!.length
                val sel: Int
                sel = when {
                    curL > prevL -> min(curL, start+2)
                    curL < prevL -> if (curText[0] == '0') start
                    else min(curL, max(0, start-1))
                    else -> min(curL, start+1)
                }
                Log.d("TAG", "$prevL, $curL, $sel")
                transport_value.setSelection(sel)
                if (firstTime == 0)
                    transport_value.requestFocus()
                else firstTime--
            }
        })

        housing_value.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val curText = housing_value.text.toString()
                try {
                    if (curText == "") {
                        accountInfo.housingPlan = 0
                        housing_value.setText("0")
                    } else {
                        accountInfo.housingPlan = df.parse(housing_value.text.toString())?.toLong()!!
                        if (curText != df.format(accountInfo.housingPlan))
                            housing_value.setText(df.format(accountInfo.housingPlan))
                    }
                    accountInfoRef.setValue(accountInfo)
                } catch (e: ParseException) {
                    housing_value.setText(df.format(accountInfo.housingPlan))
                }
                val prevL = curText.length
                val curL = housing_value.text!!.length
                val sel: Int
                sel = when {
                    curL > prevL -> min(curL, start+2)
                    curL < prevL -> if (curText[0] == '0') start
                                    else min(curL, max(0, start-1))
                    else -> min(curL, start+1)
                }
                Log.d("TAG", "$prevL, $curL, $sel")
                housing_value.setSelection(sel)
                if (firstTime == 0)
                    housing_value.requestFocus()
                else firstTime--
            }
        })
    }

    fun handleFirebase() {
        accountInfoRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Log.d("TAG", "Reading in control frag")
                accountInfo = AccountInfo.fromMap(dataSnapshot.value as HashMap<String, Any>)
                Log.d("TAG", "Account Info is: $accountInfo")

                if (food_value != null &&
                    !((food_value.text.toString() == "" && accountInfo.foodPlan < 1e-3) ||
                      abs(df.parse(food_value.text.toString()).toLong() - accountInfo.foodPlan) <
                            1e-3))
                    food_value.setText(df.format(accountInfo.foodPlan))
                food_spendings?.text = df.format(accountInfo.foodThisMonth)

                if (transport_value != null &&
                    !((transport_value.text.toString() == "" && accountInfo.foodPlan < 1e-3) ||
                            abs(df.parse(transport_value.text.toString()).toLong() - accountInfo.transportPlan) <
                            1e-3))
                    transport_value?.setText(df.format(accountInfo.transportPlan))
                transport_spendings?.text = df.format(accountInfo.transportThisMonth)

                if (housing_value != null &&
                    !((housing_value.text.toString() == "" && accountInfo.foodPlan < 1e-3) ||
                            abs(df.parse(housing_value.text.toString()).toLong() - accountInfo.housingPlan) <
                            1e-3))
                    housing_value?.setText(df.format(accountInfo.housingPlan))
                housing_spendings?.text = df.format(accountInfo.housingThisMonth)

                if (shopping_value != null &&
                    !((shopping_value.text.toString() == "" && accountInfo.foodPlan < 1e-3) ||
                            abs(df.parse(shopping_value.text.toString()).toLong() - accountInfo.shoppingPlan) <
                            1e-3))
                    shopping_value?.setText(df.format(accountInfo.shoppingPlan))
                shopping_spendings?.text = df.format(accountInfo.shoppingThisMonth)

                if (others_value != null &&
                    !((others_value.text.toString() == "" && accountInfo.foodPlan < 1e-3) ||
                            abs(df.parse(others_value.text.toString()).toLong() - accountInfo.othersPlan) <
                            1e-3))
                    others_value?.setText(df.format(accountInfo.othersPlan))
                others_spendings?.text = df.format(accountInfo.othersThisMonth)

                if (savings_value != null &&
                    !((savings_value.text.toString() == "" && accountInfo.foodPlan < 1e-3) ||
                            abs(df.parse(savings_value.text.toString()).toLong() - accountInfo.savingsPlan) <
                            1e-3))
                    savings_value?.setText(df.format(accountInfo.savingsPlan))
                savings_spendings?.text = df.format(accountInfo.savingsThisMonth)

                if (accountInfo.foodPlan > 1e-3)
                    food_icon?.drawable?.level =
                        (7000 * accountInfo.foodThisMonth / accountInfo.foodPlan).toInt()
                else food_icon?.drawable?.level = 0
                if (accountInfo.transportPlan > 1e-3)
                    transport_icon?.drawable?.level =
                        (7000 * accountInfo.transportThisMonth / accountInfo.transportPlan).toInt()
                else transport_icon?.drawable?.level = 0
                if (accountInfo.housingPlan > 1e-3)
                    housing_icon?.drawable?.level =
                        (7000 * accountInfo.housingThisMonth / accountInfo.housingPlan).toInt()
                else housing_icon?.drawable?.level = 0
                if (accountInfo.shoppingPlan > 1e-3)
                    shopping_icon?.drawable?.level =
                        (7000 * accountInfo.shoppingThisMonth / accountInfo.shoppingPlan).toInt()
                else shopping_icon?.drawable?.level = 0
                if (accountInfo.othersPlan > 1e-3)
                    others_icon?.drawable?.level =
                        (7000 * accountInfo.othersThisMonth / accountInfo.othersPlan).toInt()
                else others_icon?.drawable?.level = 0
                if (accountInfo.savingsPlan > 1e-3)
                    savings_icon?.drawable?.level =
                        (7000 * accountInfo.savingsThisMonth / accountInfo.savingsPlan).toInt()
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
}
