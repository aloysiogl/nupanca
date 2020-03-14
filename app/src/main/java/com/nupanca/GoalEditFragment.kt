package com.nupanca

import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.transition.Transition
import android.transition.TransitionInflater
import android.transition.TransitionManager
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_goal_edit.*
import kotlinx.android.synthetic.main.fragment_goal_edit.button_return
import kotlinx.android.synthetic.main.fragment_goal_minimized.*
import kotlinx.android.synthetic.main.fragment_goals_start.*

class GoalEditFragment : Fragment() {

    enum class FOCUS {
        DATE, PRIORITY, GOAL_MONEY, TITLE, NONE
    }

    private var displaySizeWithoutStatusBar: Int = 0
    private var focus: FOCUS = FOCUS.NONE
    private var isKeyboardSelected: Boolean  = false
    private var transition: Transition? = null

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
        var fragmentMode = 0
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
            -2 -> setupStrings("INSIRA O NOME DA SUA META",
                "0,00", "00/00/0000")
        }

        if (fragmentMode == -2){
            button_return.setOnClickListener{
                button_return.startAnimation(
                    AnimationUtils.loadAnimation(context, R.anim.alpha_reduction)
                )
                findNavController().navigate(R.id.action_GoalEditFragment_to_MainFragment)
            }
            button_delete.setOnClickListener{
                button_delete.startAnimation(
                    AnimationUtils.loadAnimation(context, R.anim.alpha_reduction)
                )
                findNavController().navigate(R.id.action_GoalEditFragment_to_MainFragment)
            }
        }

        goal_final_value.setOnClickListener {
            // Toggling keyboard
            toggleKeyboard()
            goal_final_value_text_edit.requestFocus()
        }

        goal_end_date.setOnClickListener {
            focus = FOCUS.DATE

            // Toggling keyboard
            toggleKeyboard()
            changeElementsOnKeyboardMovement(true)
        }

        goal_priority.setOnClickListener {
            focus = FOCUS.PRIORITY

            // Toggling keyboard
            toggleKeyboard()
            changeElementsOnKeyboardMovement(true)
        }

        goal_final_value_text_edit.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus){
                focus = FOCUS.GOAL_MONEY
                changeElementsOnKeyboardMovement(true)
            }
        }

        title_goal_edit.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus){
                focus = FOCUS.TITLE
                changeElementsOnKeyboardMovement(true)
            }
        }

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
                changeElementsOnKeyboardMovement(false)
                v.clearFocus()
            }
        }
    }

    private fun changeElementsOnKeyboardMovement(keyboardSelected:Boolean) {
        if (keyboardSelected){
            val constraintSet = ConstraintSet()

            when(focus){
                FOCUS.TITLE -> {
//                    goal_final_value_text_edit.requestFocus()

                    goal_priority.visibility = View.GONE
                    goal_final_value.visibility = View.GONE
                    goal_end_date.visibility = View.GONE
                    goal_edit_image.visibility = View.VISIBLE

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
                        guideline_keyboard.id, ConstraintSet.TOP, 0)
                }
                FOCUS.PRIORITY -> {
                    goal_final_value.visibility = View.GONE
                    goal_edit_image.visibility = View.GONE
                    goal_end_date.visibility = View.GONE

                    constraintSet.clone(edit_goal_main_screen)
                    constraintSet.connect(goal_priority.id, ConstraintSet.TOP,
                        ConstraintLayout.LayoutParams.PARENT_ID, ConstraintSet.TOP, 0)
                    constraintSet.connect(goal_priority.id, ConstraintSet.BOTTOM,
                        guideline_keyboard.id, ConstraintSet.TOP, 0)
                }
            }
            TransitionManager.beginDelayedTransition(edit_goal_main_screen)
            constraintSet.applyTo(edit_goal_main_screen)
        } else {
            // Animating upper image
            val constraintSet = ConstraintSet()
            constraintSet.clone(edit_goal_main_screen)

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
            // Transition
            TransitionManager.beginDelayedTransition(edit_goal_main_screen, transition)
            constraintSet.applyTo(edit_goal_main_screen)

            // Resetting visibility
            goal_edit_image.visibility = View.VISIBLE
            goal_priority.visibility = View.VISIBLE
            goal_final_value.visibility = View.VISIBLE
            goal_end_date.visibility = View.VISIBLE
        }
    }

    private fun toggleKeyboard(){
        //Show text input
        val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_IMPLICIT_ONLY)
    }

    private fun setupStrings(goal_title_string: String, goal_final_value_string: String,
                             goal_date_string: String){
        title_goal_edit.setText(goal_title_string)
        goal_final_value_text_edit.setText(goal_final_value_string)
        goal_date_text_edit.setText(goal_date_string)
    }
}
