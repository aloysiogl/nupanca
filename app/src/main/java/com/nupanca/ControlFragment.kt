package com.nupanca

import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.annotation.RequiresApi
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.nupanca.db.AccountInfo
import com.nupanca.db.AccountInfoDBHandler
import kotlinx.android.synthetic.main.fragment_control.*

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class ControlFragment : BaseFragment() {
    var showSuggestions = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_control, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getView()?.let { ViewCompat.setTranslationZ(it, 2f) }

        val accountInfoDBHandler = context?.let { AccountInfoDBHandler(it, null) }
        var account = accountInfoDBHandler?.findAccount(1)
        if (account == null) {
            account = AccountInfo(1)
            accountInfoDBHandler?.addAccount(account)
        }

        button_return.setOnClickListener {
            button_return.startAnimation(
                AnimationUtils.loadAnimation(context, R.anim.alpha_reduction)
            )
            findNavController().navigate(R.id.action_ControlFragment_to_MainFragment)
        }

        button_info.setOnClickListener {
            button_info.startAnimation(
                AnimationUtils.loadAnimation(
                    context, R.anim.alpha_reduction
                )
            )
        }

        food_icon.drawable.level = 5000
        transport_icon.drawable.level = 5000
        housing_icon.drawable.level = 5000
        shopping_icon.drawable.level = 5000
        others_icon.drawable.level = 5000
        savings_icon.drawable.level = 5000

        button_suggestions.setOnClickListener {
            val vis: Int
            val colorGreen = ColorStateList.valueOf(resources.getColor(R.color.colorGreen))
            val colorRed = ColorStateList.valueOf(resources.getColor(R.color.colorRed))
            val colorWhite = ColorStateList.valueOf(resources.getColor(android.R.color.white))
            showSuggestions = !showSuggestions
            if (showSuggestions) {
                button_suggestions_label.text = getString(R.string.suggestions_button_text_2)
                vis = View.VISIBLE
                food_icon.imageTintList = colorRed
                food_spendings.setTextColor(colorRed)
                transport_icon.imageTintList = colorRed
                transport_spendings.setTextColor(colorRed)
                housing_icon.imageTintList = colorRed
                housing_spendings.setTextColor(colorRed)
                shopping_icon.imageTintList = colorRed
                shopping_spendings.setTextColor(colorRed)
                others_icon.imageTintList = colorRed
                others_spendings.setTextColor(colorRed)
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
    }

    override fun onBackPressed(): Boolean {
        button_return.performClick()
        return true
    }
}
