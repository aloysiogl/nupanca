package com.nupanca

import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.ParseException
import kotlin.math.max
import kotlin.math.min

class CurrencyOnChangeListener(
    _input: EditText, _setDbValue: (Double) -> Unit,
    isInt: Boolean = false
) :
    TextWatcher {
    private val input = _input
    private val setDbValue = _setDbValue
    private val df: DecimalFormat
    private var value = 0.0
    private var firstTime = true

    init {
        val symb = DecimalFormatSymbols()
        symb.decimalSeparator = ','
        symb.groupingSeparator = '.'
        df = if (!isInt)
            DecimalFormat("###,###.00", symb)
        else DecimalFormat("###,##0", symb)
    }

    override fun afterTextChanged(s: Editable?) {
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        val curText = input.text.toString()
        try {
            if (curText == "") {
                value = 0.0
                valueToInput()
            } else {
                inputToValue()
                if (curText != df.format(value))
                    valueToInput()
            }

            setDbValue(value)
        } catch (e: ParseException) {
            valueToInput()
        }

        val prevL = curText.length
        val curL = input.text!!.length
        val sel: Int
        sel = when {
            curL > prevL -> min(curL, start + 2)
            curL < prevL -> if (curText[0] == '0') start
            else min(curL, max(0, start - 1))
            else -> min(curL, start + 1)
        }

        input.setSelection(sel)
        if (!firstTime)
            input.requestFocus()
        else firstTime = false
    }

    private fun inputToValue() {
        value = df.parse(input.text.toString())?.toDouble()!!
    }

    private fun valueToInput() {
        input.setText(df.format(value))
    }
}
