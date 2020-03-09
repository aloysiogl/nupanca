package com.nupanca

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import android.view.animation.AnimationUtils
import androidx.core.view.ViewCompat
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_transfers.*
import com.google.android.material.snackbar.Snackbar


/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class TransfersFragment : BaseFragment() {

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


        layout_store_money.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("mode", "Guardar dinheiro")
            findNavController().navigate(
                R.id.action_TransfersFragment_to_InsertMoneyFragment,
                bundle
            )
        }

        layout_recover_money.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("mode", "Resgatar dinheiro")
            findNavController().navigate(
                R.id.action_TransfersFragment_to_InsertMoneyFragment,
                bundle
            )
        }

        button_return.setOnClickListener {
            button_return.startAnimation(
                AnimationUtils.loadAnimation(context, R.anim.alpha_reduction)
            )
            findNavController().navigate(R.id.action_TransfersFragment_to_MainFragment)
        }

        button_info.setOnClickListener {
            button_info.startAnimation(
                AnimationUtils.loadAnimation(
                    context, R.anim.alpha_reduction
                )
            )
        }
    }

    override fun onBackPressed(): Boolean {
        button_return.performClick()
        return true
    }
}
