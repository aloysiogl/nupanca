package com.nupanca

import android.os.Bundle
import android.transition.*
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.TranslateAnimation
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.ViewCompat
import kotlinx.android.synthetic.main.fragment_goal.*
import kotlinx.android.synthetic.main.fragment_goal_edit.*
import kotlinx.android.synthetic.main.fragment_goal_edit.guideline2
import kotlinx.android.synthetic.main.fragment_goal.layout_top as layout_top1


class GoalEditFragment : Fragment() {

    enum class FOCUS {
        DATE, PRIORITY, GOAL_MONEY, TITLE, NONE
    }

    private var focus: FOCUS = FOCUS.NONE

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_goal_edit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getView()?.let { ViewCompat.setTranslationZ(it, 2f) }

        arguments?.let {
            val safeArgs = GoalEditFragmentArgs.fromBundle(it)
            val ola = safeArgs.mode
            title_goal_edit.setText(ola.toString())
        }

        view.addOnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            if (oldBottom > bottom) {
                // Resetting guideline to keyboard size
                val params = guideline_keyboard.layoutParams as ConstraintLayout.LayoutParams
                params.guideBegin = bottom - layout_top.height - button_confirm_edition.height
                guideline_keyboard.layoutParams = params

                // Setting focus variable
                view.findFocus()
                focus = when (view.findFocus()){
                    title_goal_edit -> FOCUS.TITLE
                    goal_priority -> FOCUS.PRIORITY
                    goal_end_date -> FOCUS.DATE
                    goal_final_value -> FOCUS.GOAL_MONEY
                    else -> FOCUS.NONE
                }

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
            else if (oldBottom < bottom) changeElementsOnKeyboardMovement(false)
        }
    }

    var oldSet: ConstraintSet = ConstraintSet()

    private fun changeElementsOnKeyboardMovement(keyboardSelected:Boolean) {
        if (keyboardSelected){
            when(focus){
                FOCUS.TITLE -> {
                    goal_priority.visibility = View.GONE
                    goal_final_value.visibility = View.GONE
                    goal_end_date.visibility = View.GONE

                    val constraintSet = ConstraintSet()
                    constraintSet.clone(edit_goal_main_screen)
                    constraintSet.connect(goal_edit_image.id, ConstraintSet.BOTTOM,
                        guideline_keyboard.id, ConstraintSet.TOP, 0)
                    val transition = AutoTransition()
                    transition.duration = 1000
                    TransitionManager.beginDelayedTransition(edit_goal_main_screen)
                    constraintSet.applyTo(edit_goal_main_screen)

                }
            }
        } else {
            // Resetting visibility
//            goal_edit_image.visibility = View.VISIBLE
//            goal_priority.visibility = View.VISIBLE
//            goal_final_value.visibility = View.VISIBLE
//            goal_end_date.visibility = View.VISIBLE

            // Resetting constraints

//            var params = goal_edit_image.layoutParams as ConstraintLayout.LayoutParams
//            params.topToTop = edit_goal_main_screen.id
//            params.bottomToBottom = guideline.id
//            goal_edit_image.layoutParams = params
            val constraintSet = ConstraintSet()
            constraintSet.clone(edit_goal_main_screen)
            constraintSet.connect(goal_edit_image.id, ConstraintSet.BOTTOM,
                guideline.id, ConstraintSet.BOTTOM, 0)
//            val transition: Transition =
//                TransitionInflater.from(activity)
//                    .inflateTransition(R.transition.my_transition)
//            val
//            val transition = TrasitionSet()
//            transition.
//            transition.duration = 1000
            TransitionManager.beginDelayedTransition(edit_goal_main_screen)
            constraintSet.applyTo(edit_goal_main_screen)
            goal_edit_image.visibility = View.VISIBLE
            goal_priority.visibility = View.VISIBLE
            goal_final_value.visibility = View.VISIBLE
            goal_end_date.visibility = View.VISIBLE


            var params = goal_final_value.layoutParams as ConstraintLayout.LayoutParams
            params.topToTop = guideline.id
            params.bottomToBottom = guideline1.id
            goal_final_value.layoutParams = params
            params = goal_end_date.layoutParams as ConstraintLayout.LayoutParams
            params.topToTop = guideline1.id
            params.bottomToBottom = guideline2.id
            goal_end_date.layoutParams = params
            params = goal_priority.layoutParams as ConstraintLayout.LayoutParams
            params.topToTop = guideline2.id
            params.bottomToBottom = edit_goal_main_screen.id
            goal_priority.layoutParams = params
        }

//        if (focus){
//            imageView7.visibility = View.GONE
//            goal_edit_message.visibility = View.GONE
//        }
//        else {
//            imageView7.visibility = View.VISIBLE
//                goal_edit_message.visibility = View.VISIBLE
//        }
    }

//    private fun loadGoalInformation (goalId: Int){
//        if (id == -1){
////            titleGoalEdit.text = ""
//            return
//        }
//    }
}
