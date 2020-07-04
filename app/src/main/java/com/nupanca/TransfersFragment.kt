package com.nupanca

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.navigation.fragment.findNavController
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.nupanca.db.AccountInfo
import kotlinx.android.synthetic.main.fragment_transfers.*
import kotlinx.android.synthetic.main.fragment_transfers.button_edit
import kotlinx.android.synthetic.main.fragment_transfers.button_return
import kotlinx.android.synthetic.main.fragment_transfers.total_amount
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols


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
        handleFirebase()

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

        button_edit.setOnClickListener {
            button_edit.startAnimation(
                AnimationUtils.loadAnimation(
                    context, R.anim.alpha_reduction
                )
            )
            val builder: AlertDialog.Builder? = activity?.let {
                AlertDialog.Builder(it)
            }
            builder?.setMessage(R.string.transfers_info)
            val dialog: AlertDialog? = builder?.create()
            dialog?.show()

            val textView = dialog?.findViewById<View>(android.R.id.message) as TextView
            textView.typeface  = context?.let { it1 ->
                ResourcesCompat.getFont(
                    it1,
                    R.font.keep_calm_w01_book
                )
            }
        }
    }

    fun handleFirebase() {
        val db = FirebaseDatabase.getInstance();
        val accountInfoRef =
            db.getReference("${(activity as MainActivity).androidId}/account_info")

        accountInfoRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val accountInfo = AccountInfo.fromMap(dataSnapshot.value as HashMap<String, Any>)
                Log.d("TAG", "Account Info is: $accountInfo")

                // Updating total amount
                val symb = DecimalFormatSymbols()
                symb.decimalSeparator = ','
                symb.groupingSeparator = '.'
                val df = DecimalFormat("###,##0.00", symb)
                total_amount?.text = df.format(accountInfo.savingsBalance)
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
