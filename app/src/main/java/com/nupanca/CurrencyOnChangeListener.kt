package com.nupanca

import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import androidx.core.math.MathUtils.clamp
import androidx.core.text.trimmedLength
import androidx.core.view.updateLayoutParams
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.ParseException

class CurrencyOnChangeListener(
    private val input: EditText,
    private val callback: (Double) -> Unit = {},
    private val isInt: Boolean = false
) : TextWatcher {
    private val df: DecimalFormat
    private var value = 0.0
    private var invalidDelete = false

    init {
        val symb = DecimalFormatSymbols()
        symb.decimalSeparator = ','
        symb.groupingSeparator = '.'
        df = if (!isInt)
            DecimalFormat("###,##0.00", symb)
        else DecimalFormat("###,##0", symb)
        input.setSelection(input.text.length)
    }

    override fun afterTextChanged(s: Editable?) {
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        val curText = s.toString()
        if (count == 1 && curText[start] == '.') {
            onTextChanged(
                curText.substring(0, start - 1) + curText.substring(start + 1, curText.length),
                start - 2, count, after
            )
            invalidDelete = true
        } else if (count == 1 && curText[start] == ',') {
            onTextChanged(
                curText.substring(0, start - 1) + "," + curText.substring(start + 1, curText.length),
                start - 1, count, after
            )
            invalidDelete = true
        }
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        if (invalidDelete) {
            invalidDelete = false
            return
        }

        val curText = s.toString()
        try {
            inputToValue(curText)
            callback(value)
        } catch (e: ParseException) {
        }
        valueToInput()
        fixSelection(curText, start, before)
    }

    private fun inputToValue(s: String) {
        value = if (s == "") 0.0 else df.parse(s)?.toDouble()!!
        if (!isInt) {
            if (s.length >= 4 && s[s.length - 4] == ',')
                value *= 10
            else if (s.length >= 2 && s[s.length - 2] == ',')
                value /= 10
        }
    }

    private fun valueToInput() {
        input.removeTextChangedListener(this)
        input.setText(df.format(value))
        input.addTextChangedListener(this)
    }

    private fun fixSelection(prevText: String, start: Int, before: Int) {
        val prevL = prevText.length
        val curL = input.text.length

        val sel: Int = when {
            curL > prevL -> start + 2
            curL < prevL -> {
                if (!isInt && prevText.length >= 4 && prevText[prevText.length - 4] == ',') start
                else if (prevText == "0" || (before == 0 && prevText[0] == '0')) 1
                else start - prevL + curL
            }
            else -> if (before > 0) start else start + 1
        }
        input.setSelection(clamp(sel, 0, curL))
    }
}
