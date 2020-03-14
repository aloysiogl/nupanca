package com.nupanca

import android.content.Context
import android.os.Bundle
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

        //Show text input
        val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        text_edit_money_to_remove.requestFocus()
        imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_IMPLICIT_ONLY)

        arguments?.let {
            val safeArgs = InsertMoneyFragmentArgs.fromBundle(it)
            val mode = safeArgs.mode
            how_much_to_save.text = mode
        }

        button_confirm.setOnClickListener{
            imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_IMPLICIT_ONLY)
        }

        text_edit_money_to_remove.setOnKeyListener { v, keyCode, event ->
            if (event.action == ACTION_UP)
                text_edit_money_to_remove.setText(processKeyPress(event))

            false
        }

        button_return.setOnClickListener {
            button_return.startAnimation(
                AnimationUtils.loadAnimation(context, R.anim.alpha_reduction)
            )
            findNavController().navigate(R.id.action_InsertMoneyFragment_to_TransfersFragment)
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
