package com.nupanca

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
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
    var goalsList: GoalAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_control, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getView()?.let { ViewCompat.setTranslationZ(it, 1f) }

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

//        button_suggestions.setOnClickListener {
//
//        }
    }

    override fun onBackPressed(): Boolean {
        button_return.performClick()
        return true
    }
}
