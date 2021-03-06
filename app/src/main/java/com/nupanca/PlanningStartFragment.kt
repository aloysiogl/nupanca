package com.nupanca

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_planning_start.*

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class PlanningStartFragment : BaseFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_planning_start, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getView()?.let { ViewCompat.setTranslationZ(it, 3f) }

        button_return.setOnClickListener {
            button_return.startAnimation(
                AnimationUtils.loadAnimation(context, R.anim.alpha_reduction)
            )
            findNavController().navigate(R.id.action_PlanningStartFragment_to_MainFragment)
        }

        button_start.setOnClickListener {
            findNavController().navigate(R.id.action_PlanningStartFragment_to_PlanningFragment)
        }
    }

    override fun onBackPressed(): Boolean {
        button_return.performClick()
        return true
    }
}
