package com.nupanca

import android.content.Context
import android.os.Bundle
import android.transition.Transition
import android.transition.TransitionInflater
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_goal_edit.*


class GoalEditFragment : Fragment() {

    enum class FOCUS {
        DATE, PRIORITY, GOAL_MONEY, TITLE, NONE
    }

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
        arguments?.let {
            val safeArgs = GoalEditFragmentArgs.fromBundle(it)
            val ola = safeArgs.mode
            title_goal_edit.setText(ola.toString())
        }

        // Setting default transition
        transition = TransitionInflater
            .from(activity).inflateTransition(R.transition.change_bounds_fade)

        goal_final_value.setOnClickListener {
            focus = FOCUS.GOAL_MONEY

            // Toggling keyboard
            toggleKeyboard()
            view.clearFocus()
        }

        title_goal_edit.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus){
                focus = FOCUS.TITLE
            }
        }

        view.addOnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            if (oldBottom > bottom) {
                isKeyboardSelected  = true

                // Resetting guideline to keyboard size
                val params = guideline_keyboard.layoutParams as ConstraintLayout.LayoutParams
                params.guideBegin = bottom - layout_top.height - button_confirm_edition.height
                guideline_keyboard.layoutParams = params

                // Setting focus variable
                view.findFocus()
//                focus = when (view.findFocus()){
//                    title_goal_edit -> FOCUS.TITLE
////                    goal_priority -> FOCUS.PRIORITY
////                    goal_end_date -> FOCUS.DATE
////                    goal_final_value -> FOCUS.GOAL_MONEY
//                    else -> FOCUS.NONE
//                }

                // Setting animation and selected view
                changeElementsOnKeyboardMovement(true)

//                how_much_to_save.setConstraintSet()
//                val constraintSet = ConstraintSet()
//                constraintSet.clone(constraintLayout)
//                constraintSet.connect(
//                    R.id.how,
//                    ConstraintSet.RIGHT,
//                    R.id.check_answer2,
//                    ConstraintSet.RIGHT,
//                    0
//                )
//                constraintSet.connect(
//                    R.id.imageView,
//                    ConstraintSet.TOP,
//                    R.id.check_answer2,
//                    ConstraintSet.TOP,
//                    0
//                )
//                val params = goal_priority.layoutParams as ConstraintLayout.LayoutParams
//                params.topToTop = edit_goal_main_screen.id
//                params.bottomToBottom = edit_goal_main_screen.id
//                goal_priority.layoutParams = params
            }
            else if (oldBottom < bottom) {
                isKeyboardSelected  = false
                changeElementsOnKeyboardMovement(false)
            }
        }
    }

    var oldSet: ConstraintSet = ConstraintSet()

    private fun changeElementsOnKeyboardMovement(keyboardSelected:Boolean) {
        if (keyboardSelected){
            val constraintSet = ConstraintSet()

            when(focus){
                FOCUS.TITLE -> {
                    goal_priority.visibility = View.GONE
                    goal_final_value.visibility = View.GONE
                    goal_end_date.visibility = View.GONE

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
}
