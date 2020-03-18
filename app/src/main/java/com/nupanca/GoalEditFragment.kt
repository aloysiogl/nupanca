package com.nupanca

import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.content.Context
import android.content.DialogInterface
import android.graphics.Rect
import android.os.Bundle
import android.transition.Transition
import android.transition.TransitionInflater
import android.transition.TransitionManager
import android.util.DisplayMetrics
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.ViewCompat
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_goal_edit.*
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import java.util.regex.Pattern


class GoalEditFragment : BaseFragment() {

    enum class FOCUS {
        DATE, PRIORITY, GOAL_MONEY, TITLE, NONE
    }

    private var displaySizeWithoutStatusBar: Int? = 0
    private var focus: FOCUS = FOCUS.NONE
    private var isKeyboardSelected: Boolean  = false
    private var transition: Transition? = null
    private var isActionSelected: Boolean = false
    private var currentSelection: String? = null
    private var disappearText: Boolean = false
    private var fragmentMode = 0
    private var validValues = true
    private var selectionValue = 0.0
    private var selectionCalendar: Calendar = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_goal_edit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set depth for animations
        getView()?.let { ViewCompat.setTranslationZ(it, 2f) }

        // Reading arguments
        arguments?.let {
            val safeArgs = GoalEditFragmentArgs.fromBundle(it)
            fragmentMode = safeArgs.mode
        }

        // Setting default transition
        transition = TransitionInflater
            .from(activity).inflateTransition(R.transition.change_bounds_fade)

        // Getting display size without status bar
        val rectangle = Rect()
        activity?.window?.decorView?.getWindowVisibleDisplayFrame(rectangle)
        val metrics = DisplayMetrics()
        view.display.getMetrics(metrics)
        displaySizeWithoutStatusBar = metrics.heightPixels - rectangle.top

        // Setting texts
        when(fragmentMode){
            -2 -> {
                setupStrings("INSIRA O NOME DA SUA META",
                    "0,00", SimpleDateFormat("dd/MM/yyyy",
                        Locale.US).format(selectionCalendar.time))

                button_delete.visibility = View.INVISIBLE
                disappearText = true

                button_return.setOnClickListener{
                    button_return.startAnimation(
                        AnimationUtils.loadAnimation(context, R.anim.alpha_reduction)
                    )
                    findNavController().navigate(R.id.action_GoalEditFragment_to_MainFragment)
                }
            }
            -1 -> {
                setupStrings("INSIRA O NOME DA SUA META",
                    "0,00", SimpleDateFormat("dd/MM/yyyy",
                        Locale.US).format(selectionCalendar.time))

                button_delete.visibility = View.INVISIBLE
                disappearText = true

                button_return.setOnClickListener{
                    button_return.startAnimation(
                        AnimationUtils.loadAnimation(context, R.anim.alpha_reduction)
                    )
                    findNavController().navigate(R.id.action_GoalEditFragment_to_GoalsListFragment)
                }
            }
        }

        goal_priority.setOnClickListener {
            focus = FOCUS.PRIORITY
            changeElementsToFocusMode(true)
        }


        // Setting up goal title

        title_goal_edit.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus){
                focus = FOCUS.TITLE
                changeElementsToFocusMode(true)
            }
        }

        title_goal_edit.setOnKeyListener { _, keyCode, _ ->
            if (keyCode == KeyEvent.KEYCODE_ENTER)
                button_confirm_edition.performClick()
            false
        }

        // Setting up goal final value

        goal_final_value.setOnClickListener {
            focus = FOCUS.GOAL_MONEY
            goal_final_value.requestFocus()
            toggleKeyboard(true)
            changeElementsToFocusMode(true)
        }

        goal_final_value_text_edit.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus){
                focus = FOCUS.GOAL_MONEY
                changeElementsToFocusMode(true)
            }
        }

        goal_final_value_text_edit.setOnKeyListener { _, keyCode, _ ->
            if (keyCode == KeyEvent.KEYCODE_ENTER)
                button_confirm_edition.performClick()
            false
        }

        // Setting up goal_date

        goal_end_date.setOnClickListener {
            val date =
            OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                selectionCalendar.set(Calendar.YEAR, year)
                selectionCalendar.set(Calendar.MONTH, monthOfYear)
                selectionCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                goal_date_text_edit.setText(SimpleDateFormat("dd/MM/yyyy", Locale.US)
                    .format(selectionCalendar.time))
                changeElementsToFocusMode(false)
            }

            val dialog = DatePickerDialog(
                view.context, date, selectionCalendar
                    .get(Calendar.YEAR), selectionCalendar.get(Calendar.MONTH),
                selectionCalendar.get(Calendar.DAY_OF_MONTH)
            )

            dialog.setButton(
                DialogInterface.BUTTON_NEGATIVE,
                "cancel"
            ) { _, which ->
                if (which == DialogInterface.BUTTON_NEGATIVE)
                    changeElementsToFocusMode(false)
            }

            dialog.show()

            focus = FOCUS.DATE

            // Changing view
            changeElementsToFocusMode(true)
        }

        // Setting up goal_priority

        val items = arrayOf<String?>("Muito Baixa", "Baixa", "Média", "Alta", "Muito Alta")
        currentSelection = items[0]

        goal_priority_dropdown.adapter = ArrayAdapter(view.context, R.layout.dropdown_item, items)

        goal_priority_dropdown?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{

            override fun onNothingSelected(parent: AdapterView<*>?) {
                changeElementsToFocusMode(true)
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selected = parent?.getItemAtPosition(position).toString();
                if (selected != currentSelection){
                    currentSelection = selected
                    changeElementsToFocusMode(false)
                }
            }
        }

        // Confirm button detection
        button_confirm_edition.setOnClickListener {
            if (isActionSelected){
                toggleKeyboard(false)
                changeElementsToFocusMode(false)
            }
            else if (validValues) {
                findNavController().navigate(
                    R.id.action_GoalEditFragment_to_GoalsListFragment
                )
            }
        }

        // Keyboard detection
        view.addOnLayoutChangeListener { v, _, _, _, bottom, _, _, _, oldBottom ->
            if (oldBottom > bottom) {
                isKeyboardSelected  = true

                // Resetting guideline to keyboard size
                val params = guideline_keyboard.layoutParams as ConstraintLayout.LayoutParams
                params.guideBegin = bottom - layout_top.height - button_confirm_edition.height
                guideline_keyboard.layoutParams = params
            }
            else if (oldBottom < bottom && bottom == displaySizeWithoutStatusBar) {
                isKeyboardSelected  = false
                v.clearFocus()
                goal_edit_image.requestFocus()
                changeElementsToFocusMode(false)
            }
        }
    }

    private fun changeElementsToFocusMode(keyboardSelected:Boolean) {
        if (keyboardSelected){
            val constraintSet = ConstraintSet()
            isActionSelected = true
            change_goal_button_text.setText(R.string.confirm_goal_edit)
            change_goal_button_text.setTextColor(resources.getColor(R.color.colorPrimary))
            when(focus){
                FOCUS.TITLE -> {
                    goal_priority.visibility = View.GONE
                    goal_final_value.visibility = View.GONE
                    goal_end_date.visibility = View.GONE
                    goal_edit_image.visibility = View.VISIBLE
                    if (disappearText){
                        title_goal_edit.setText("")
                        disappearText = false
                    }

                    constraintSet.clone(edit_goal_main_screen)
                    constraintSet.connect(goal_edit_image.id, ConstraintSet.BOTTOM,
                        guideline_keyboard.id, ConstraintSet.TOP, 0)
                    TransitionManager.beginDelayedTransition(edit_goal_main_screen)
                    constraintSet.applyTo(edit_goal_main_screen)
                }
                FOCUS.GOAL_MONEY -> {
                    goal_priority.visibility = View.GONE
                    goal_edit_image.visibility = View.GONE
                    goal_end_date.visibility = View.GONE

                    constraintSet.clone(edit_goal_main_screen)
                    constraintSet.connect(goal_final_value.id, ConstraintSet.TOP,
                        ConstraintLayout.LayoutParams.PARENT_ID, ConstraintSet.TOP, 0)
                    constraintSet.connect(goal_final_value.id, ConstraintSet.BOTTOM,
                        guideline_keyboard.id, ConstraintSet.TOP, 0)
                }
                FOCUS.DATE -> {
                    goal_priority.visibility = View.GONE
                    goal_edit_image.visibility = View.GONE
                    goal_final_value.visibility = View.GONE

                    constraintSet.clone(edit_goal_main_screen)
                    constraintSet.connect(goal_end_date.id, ConstraintSet.TOP,
                        ConstraintLayout.LayoutParams.PARENT_ID, ConstraintSet.TOP, 0)
                    constraintSet.connect(goal_end_date.id, ConstraintSet.BOTTOM,
                        ConstraintLayout.LayoutParams.PARENT_ID, ConstraintSet.BOTTOM, 0)
                }
                FOCUS.PRIORITY -> {
                    goal_final_value.visibility = View.GONE
                    goal_edit_image.visibility = View.GONE
                    goal_end_date.visibility = View.GONE
//                    goal_edit_dropdown_arrow.visibility = View.VISIBLE
                    val params = dropdown_layout.layoutParams as ConstraintLayout.LayoutParams
                    params.verticalBias = 0.5F
                    dropdown_layout.layoutParams = params

                    constraintSet.clone(edit_goal_main_screen)
                    constraintSet.connect(goal_priority.id, ConstraintSet.TOP,
                        ConstraintLayout.LayoutParams.PARENT_ID, ConstraintSet.TOP, 0)
                    constraintSet.connect(goal_priority.id, ConstraintSet.BOTTOM,
                        ConstraintLayout.LayoutParams.PARENT_ID, ConstraintSet.BOTTOM, 0)

                    priority_text_box.visibility = View.GONE
                    goal_priority_dropdown.visibility = View.VISIBLE
                    goal_priority_dropdown.isEnabled = true
                    goal_priority_dropdown.performClick()
                }
            }
            TransitionManager.beginDelayedTransition(edit_goal_main_screen)
            constraintSet.applyTo(edit_goal_main_screen)
        } else {
            isActionSelected = false

            // Animating upper image
            val constraintSet = ConstraintSet()
            constraintSet.clone(edit_goal_main_screen)

            // Title
            if (title_goal_edit.text.toString() == ""){
                disappearText = true

                when(fragmentMode){
                    -2, -1 -> title_goal_edit.setText(R.string.placeholder_edit_text)
                }
            }

            // Image
            constraintSet.connect(goal_edit_image.id, ConstraintSet.BOTTOM,
                guideline.id, ConstraintSet.BOTTOM, 0)
            // Goal final value
            constraintSet.connect(goal_final_value.id, ConstraintSet.TOP,
                guideline.id, ConstraintSet.BOTTOM, 0)
            constraintSet.connect(goal_final_value.id, ConstraintSet.BOTTOM,
                guideline1.id, ConstraintSet.BOTTOM, 0)
            // Goal date
            constraintSet.connect(goal_end_date.id, ConstraintSet.TOP,
                guideline1.id, ConstraintSet.BOTTOM, 0)
            constraintSet.connect(goal_end_date.id, ConstraintSet.BOTTOM,
                guideline2.id, ConstraintSet.BOTTOM, 0)
            // Goal priority
            constraintSet.connect(goal_priority.id, ConstraintSet.TOP,
                guideline2.id, ConstraintSet.BOTTOM, 0)
            constraintSet.connect(goal_priority.id, ConstraintSet.BOTTOM,
                ConstraintLayout.LayoutParams.PARENT_ID, ConstraintSet.BOTTOM, 0)
            val params = dropdown_layout.layoutParams as ConstraintLayout.LayoutParams
            params.verticalBias = 0.7F
            dropdown_layout.layoutParams = params
            // Transition
            TransitionManager.beginDelayedTransition(edit_goal_main_screen, transition)
            constraintSet.applyTo(edit_goal_main_screen)

            // Resetting visibility
            goal_edit_image.visibility = View.VISIBLE
            goal_priority.visibility = View.VISIBLE
            goal_final_value.visibility = View.VISIBLE
            goal_end_date.visibility = View.VISIBLE
            priority_text_box.visibility = View.VISIBLE
            goal_priority_dropdown.visibility = View.GONE
            goal_edit_dropdown_arrow.visibility = View.GONE

            priority_text_box.text = currentSelection
            goal_priority_dropdown.isEnabled = false

            // Validity check
            validValues = true
            val pattern = Pattern.compile("^(\\d*\\,)?\\d+\$")
            if (!pattern.matcher(goal_final_value_text_edit.text).matches() ||
                goal_final_value_text_edit.text.toString()
                    .replace(",", ".").toDouble() <= 0){
                validValues = false
                goal_final_value_text_edit.setTextColor(resources.getColor(R.color.colorRed))
            }
            else {
                selectionValue = goal_final_value_text_edit.text.
                toString().replace(",", ".").toDouble()
                goal_final_value_text_edit.setTextColor(resources.getColor(R.color.colorPrimary))
            }
            if (selectionCalendar.time < Calendar.getInstance().time){
                validValues = false
                goal_date_text_edit.setTextColor(resources.getColor(R.color.colorRed))
            }
            else goal_date_text_edit.setTextColor(resources.getColor(R.color.colorPrimary))

            // Values which depend on validity
            if (validValues){
                when(fragmentMode){
                    -2, -1 -> change_goal_button_text.setText(R.string.add_goal_edit)
                }
            }

            else {
                change_goal_button_text.setTextColor(resources.getColor(R.color.colorRed))
                change_goal_button_text.setText(R.string.invalid_entries)
            }

        }
    }

    private fun toggleKeyboard(toggle: Boolean){
        val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if (toggle)
            // Show text input
            imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_IMPLICIT_ONLY)
        else
            // Hide text input
            imm.hideSoftInputFromWindow(view?.windowToken, 0)

    }

    private fun setupStrings(goal_title_string: String, goal_final_value_string: String,
                             goal_date_string: String){
        title_goal_edit.setText(goal_title_string)
        goal_final_value_text_edit.setText(goal_final_value_string)
        goal_date_text_edit.text = goal_date_string
    }

    override fun onBackPressed(): Boolean {
        if (isActionSelected) {
            changeElementsToFocusMode(false)
            return true
        }
        when(fragmentMode) {
            -2 -> findNavController().navigate(R.id.action_GoalEditFragment_to_MainFragment)
            -1 -> findNavController().navigate(R.id.action_GoalEditFragment_to_GoalsListFragment)
        }
        return true
    }
}