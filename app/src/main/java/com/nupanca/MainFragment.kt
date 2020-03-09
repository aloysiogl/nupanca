package com.nupanca

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.Animation.AnimationListener
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_main.*

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class MainFragment : Fragment() {

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        button_return.setOnClickListener {
            button_return.startAnimation(AnimationUtils.loadAnimation(
                context, R.anim.alpha_reduction))
        }

        button_info.setOnClickListener {
            button_info.startAnimation(AnimationUtils.loadAnimation(
                context, R.anim.alpha_reduction))
        }

        total_amount_layout.setOnClickListener {
            findNavController().navigate(R.id.action_MainFragment_to_TransfersFragment)
        }

        goals_layout.setOnClickListener {
            Snackbar.make(it, "Replace with this", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }

        control_layout.setOnClickListener {
            Snackbar.make(it, "Replace with this", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
    }
}
