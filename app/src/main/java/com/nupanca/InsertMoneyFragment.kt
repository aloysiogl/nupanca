package com.nupanca

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.KeyEvent.ACTION_UP
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import androidx.core.view.ViewCompat
import androidx.navigation.fragment.findNavController
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.nupanca.db.AccountInfo
import kotlinx.android.synthetic.main.fragment_insert_money.*
import kotlinx.android.synthetic.main.fragment_insert_money.button_return
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [insert_money.newInstance] factory method to
 * create an instance of this fragment.
 */
class InsertMoneyFragment() : BaseFragment() {
    private var param1: String? = null
    private var param2: String? = null
    private var valueInTextBox: Double = 0.0
    private val db = FirebaseDatabase.getInstance()
    private val accountInfoRef = db.getReference("account_info")
    private var accountInfo: AccountInfo? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    private fun processKeyPress(event: KeyEvent): String {
        fun processDoubleAsPortugueseString(doubleValue: Double): String {
            val symb = DecimalFormatSymbols()
            symb.decimalSeparator = ','
            symb.groupingSeparator = '.'
            val df = DecimalFormat("###,##0.00", symb)
            return df.format(doubleValue)
        }

        // Getting keypress
        if (KeyEvent.KEYCODE_0 <= event.keyCode && event.keyCode <= KeyEvent.KEYCODE_9)
            valueInTextBox = valueInTextBox * 10 +
                    ((event.keyCode - KeyEvent.KEYCODE_0).toDouble()) / 100
        else if (event.keyCode == KeyEvent.KEYCODE_DEL)
            valueInTextBox /= 10
        valueInTextBox = (valueInTextBox * 100).toInt() / 100.0

        // Showing errors
        if (analyseCorrectness()) confirm_button_text.setTextColor(resources.getColor(R.color.colorPrimary))
        else confirm_button_text.setTextColor(resources.getColor(R.color.colorGray))

        return processDoubleAsPortugueseString(valueInTextBox)
    }

    //TODO finish this method
    private fun analyseCorrectness() :Boolean{
        return valueInTextBox >= 0.01
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_insert_money, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getView()?.let { ViewCompat.setTranslationZ(it, 2f) }
        handleFirebase()

        //Show text input
        val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        text_edit_money_to_remove.requestFocus()
        imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_IMPLICIT_ONLY)

        var mode = ""
        arguments?.let {
            val safeArgs = InsertMoneyFragmentArgs.fromBundle(it)
            mode = safeArgs.mode
            how_much_to_save.text = mode
        }

        button_confirm.setOnClickListener{
            val symb = DecimalFormatSymbols()
            symb.decimalSeparator = ','
            symb.groupingSeparator = '.'
            val df = DecimalFormat("###,##0.00", symb)
            var amount = df.parse(text_edit_money_to_remove.text.toString())?.toDouble()
            if (amount != null)
                amount = (amount * 100).toInt() / 100.0

            if (amount != null) {
                if (mode == "Guardar dinheiro") {
                    accountInfo?.savingsBalance = accountInfo?.savingsBalance?.plus(amount)!!
                    accountInfo?.accountBalance = accountInfo?.accountBalance?.minus(amount)!!
                } else {
                    accountInfo?.savingsBalance = accountInfo?.savingsBalance?.minus(amount)!!
                    accountInfo?.accountBalance = accountInfo?.accountBalance?.plus(amount)!!
                }
            }
            Log.d("TAG", "New account info: $accountInfo")
            accountInfoRef.setValue(accountInfo)
            imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_IMPLICIT_ONLY)
            findNavController().navigate(R.id.action_InsertMoneyFragment_to_TransfersFragment)
        }

        text_edit_money_to_remove.setOnKeyListener { v, keyCode, event ->
            if (event.action == ACTION_UP)
                text_edit_money_to_remove.setText(processKeyPress(event))
            updateAvailableMoneyText()

            false
        }

        button_return.setOnClickListener {
            button_return.startAnimation(
                AnimationUtils.loadAnimation(context, R.anim.alpha_reduction)
            )
            findNavController().navigate(R.id.action_InsertMoneyFragment_to_TransfersFragment)
        }
    }

    fun handleFirebase() {
        accountInfoRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                accountInfo = AccountInfo.fromMap(dataSnapshot.value as HashMap<String, Any>)
                Log.d("TAG", "Account Info is: $accountInfo")
                updateAvailableMoneyText()
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w("TAG", "Failed to read account info.", error.toException())
            }
        })
    }

    private fun updateAvailableMoneyText() {
        val symb = DecimalFormatSymbols()
        symb.decimalSeparator = ','
        symb.groupingSeparator = '.'
        val df = DecimalFormat("###,##0.00", symb)

        var mode = ""
        arguments?.let {
            val safeArgs = InsertMoneyFragmentArgs.fromBundle(it)
            mode = safeArgs.mode
        }

        var transactionPossible = false
        if (text_edit_money_to_remove != null) {
            var amount = df.parse(text_edit_money_to_remove.text.toString()).toDouble()
            amount = (amount * 100).toInt() / 100.0
            transactionPossible = if (mode == "Guardar dinheiro")
                amount < accountInfo?.accountBalance!!
            else amount < accountInfo?.savingsBalance!!
        }

        if (transactionPossible) {
            // Updating total amount
            var txt = "Saldo disponível: R$ "
            if (mode == "Guardar dinheiro")
                txt += df.format(accountInfo!!.accountBalance)
            else txt += df.format(accountInfo!!.savingsBalance)

            textView4?.text = txt
            textView4?.setTextColor(getResources().getColor(R.color.colorGray))
            button_confirm?.isClickable = true
            confirm_button_text?.setTextColor(getResources().getColor(R.color.colorPrimary))
        } else {
            textView4?.text = "Você não possui saldo suficiente!"
            textView4?.setTextColor(getResources().getColor(R.color.colorRed))
            button_confirm?.isClickable = false
            confirm_button_text?.setTextColor(getResources().getColor(R.color.colorGray))
        }
    }

    override fun onBackPressed(): Boolean {
        button_return.performClick()
        return true
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment insert_money.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            InsertMoneyFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
