package com.nupanca

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.core.view.ViewCompat
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_transfers.*

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class TransfersFragment : Fragment()/*, IOnBackPressed*/ {

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_transfers, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getView()?.let { ViewCompat.setTranslationZ(it, 1f) }

        button_return.setOnClickListener {
            button_return.startAnimation(
                AnimationUtils.loadAnimation(context, R.anim.alpha_reduction))
            findNavController().navigate(R.id.action_TransfersFragment_to_MainFragment)
        }

        button_info.setOnClickListener {
            button_info.startAnimation(AnimationUtils.loadAnimation(
                context, R.anim.alpha_reduction))
        }
    }
//
//    override fun onBackPressed(): Boolean {
//        view?.let {
//            Snackbar.make(it, "Replace with this", Snackbar.LENGTH_LONG)
//                .setAction("Action", null).show()
//        }
//        button_return.performClick()
//        button_return.callOnClick()
//        return true
//    }
}
