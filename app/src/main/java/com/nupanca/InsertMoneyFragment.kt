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
import com.google.firebase.database.*
import com.nupanca.db.AccountInfo
import kotlinx.android.synthetic.main.fragment_control.*
import kotlinx.android.synthetic.main.fragment_insert_money.*
import kotlinx.android.synthetic.main.fragment_insert_money.button_return
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols


class InsertMoneyFragment() : BaseFragment() {
    private var valueInTextBox: Long = 0
    private val db = FirebaseDatabase.getInstance()
    private var accountInfoRef: DatabaseReference? = null
    private var accountInfo: AccountInfo? = null

    //TODO finish this method
    private fun analyseCorrectness(): Boolean {
        return valueInTextBox >= 0.01
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Getting database reference for the account
        accountInfoRef = db.getReference("${(activity as MainActivity).androidId}/account_info")
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_insert_money, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getView()?.let { ViewCompat.setTranslationZ(it, 3f) }
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
            valueInTextBox = (safeArgs.value.toDouble() * 100).toLong()
            text_edit_money_to_remove.setText(
                processDoubleAsPortugueseString(valueInTextBox.toDouble() / 100.00)
            )
        }

        button_confirm.setOnClickListener {
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
                    accountInfo?.savings30Days = accountInfo?.savings30Days?.plus(amount.toInt())!!
                } else {
                    accountInfo?.savingsBalance = accountInfo?.savingsBalance?.minus(amount)!!
                    accountInfo?.accountBalance = accountInfo?.accountBalance?.plus(amount)!!
                    accountInfo?.savings30Days = accountInfo?.savings30Days?.minus(amount.toInt())!!
                }
            }
            Log.d("TAG", "New account info: $accountInfo")
            accountInfoRef?.setValue(accountInfo)
            imm.toggleSoftInput(
                InputMethodManager.SHOW_IMPLICIT,
                InputMethodManager.HIDE_IMPLICIT_ONLY
            )
            findNavController().navigate(R.id.action_InsertMoneyFragment_to_TransfersFragment)
        }

        text_edit_money_to_remove.addTextChangedListener(
            CurrencyOnChangeListener(text_edit_money_to_remove, ::updateAvailableMoneyText)
        )

        button_return.setOnClickListener {
            button_return.startAnimation(
                AnimationUtils.loadAnimation(context, R.anim.alpha_reduction)
            )
            findNavController().navigate(R.id.action_InsertMoneyFragment_to_TransfersFragment)
        }
    }

    fun handleFirebase() {
        accountInfoRef?.addValueEventListener(object : ValueEventListener {
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

    private fun updateAvailableMoneyText(value: Double? = null) {
        val symb = DecimalFormatSymbols()
        symb.decimalSeparator = ','
        symb.groupingSeparator = '.'
        val df = DecimalFormat("###,##0.00", symb)
        var amount = value ?: df.parse(text_edit_money_to_remove?.text.toString())?.toDouble()

        var mode = ""
        arguments?.let {
            val safeArgs = InsertMoneyFragmentArgs.fromBundle(it)
            mode = safeArgs.mode
        }

        var transactionPossible = false
        if (amount != null) {
            amount = (amount * 100).toInt() / 100.0
            transactionPossible = if (mode == "Guardar dinheiro")
                amount <= accountInfo?.accountBalance!!
            else amount <= accountInfo?.savingsBalance!!
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
            textView4?.text = "Saldo insuficiente!"
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
        fun processDoubleAsPortugueseString(doubleValue: Double): String {
            val symb = DecimalFormatSymbols()
            symb.decimalSeparator = ','
            symb.groupingSeparator = '.'
            val df = DecimalFormat("###,##0.00", symb)
            return df.format(doubleValue)
        }
    }
}
