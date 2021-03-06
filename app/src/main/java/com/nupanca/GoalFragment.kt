package com.nupanca

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.core.view.ViewCompat
import androidx.navigation.fragment.findNavController
import com.google.firebase.database.*
import com.nupanca.db.Goal
import kotlinx.android.synthetic.main.fragment_goal.*
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.*


class GoalFragment : BaseFragment() {
    private var goalKey: String? = null
    private var goal: Goal? = null

    private lateinit var dbRef: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_goal, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getView()?.let { ViewCompat.setTranslationZ(it, 2f) }

        // Reading arguments
        arguments?.let {
            val safeArgs = GoalFragmentArgs.fromBundle(it)
            goalKey = safeArgs.goalKey
        }

        // TODO make the database read more efficient
        dbRef = FirebaseDatabase.getInstance()
            .getReference((activity as MainActivity).androidId.toString())

        dbRef.addChildEventListener(object : ChildEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, p1: String?) {
            }

            override fun onChildRemoved(p0: DataSnapshot) {
            }

            override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {
                if (dataSnapshot.key != "goal_list") return

                if (GoalsListFragment.updateLazyRequest) {
                    goal = GoalsListFragment.updateGoals(
                        dataSnapshot,
                        activity as MainActivity,
                        goalKey
                    )
                    updateGoal(goal!!)
                } else {
                    for (firebaseGoal in dataSnapshot.children) {
                        val newGoal = Goal.fromMap(firebaseGoal.value as HashMap<String, Any>)
                        if (newGoal.key == goalKey)
                            updateGoal(newGoal)
                    }
                }
            }
        })

        button_return.setOnClickListener {
            button_return.startAnimation(
                AnimationUtils.loadAnimation(context, R.anim.alpha_reduction)
            )
            GoalsListFragment.updateLazyRequest = true
            findNavController().navigate(R.id.action_GoalFragment_to_GoalsListFragment)
        }

        button_edit.setOnClickListener {
            button_edit.startAnimation(
                AnimationUtils.loadAnimation(
                    context, R.anim.alpha_reduction
                )
            )
            val bundle = Bundle()
            bundle.putInt("mode", 0)
            bundle.putString("goal_key", goal?.key)
            findNavController().navigate(
                R.id.action_GoalFragment_to_GoalEditFragment,
                bundle
            )
        }
    }

    fun updateGoal(newGoal: Goal) {
        goal = newGoal
        goal_fragment_title.text = newGoal.title

        val symb = DecimalFormatSymbols()
        symb.decimalSeparator = ','
        symb.groupingSeparator = '.'
        val df = DecimalFormat("###,##0.00", symb)
        money_to_goal.text = "R$ ${df.format(newGoal.currentAmount)} " +
                "de R$ ${df.format(newGoal.totalAmount)}"

        if (newGoal.predictedEndDate >= 0)
            completion_date_information.text = "Esta meta tem prioridade " +
                    priorityIntToString(newGoal.priority) + " e a data prevista para término" +
                    " é " + SimpleDateFormat(
                "dd/MM/yyyy",
                Locale.US
            ).format(Date(newGoal.predictedEndDate).time)
        else completion_date_information.text = "Esta meta tem prioridade " +
                priorityIntToString(newGoal.priority) + " e não foi possível alocar " +
                "dinheiro para ela"

        // Progress bar
        var progress = (100 * newGoal.currentAmount / newGoal.totalAmount).toInt()
        if (progress > 100) progress = 100
        goal_progress_bar.progress = progress
        goal_progress_bar_text.text = progress.toString() + "%"


        button_add_item.setOnClickListener {
            if (MainFragment.accountInfo.foodPlan < 1e-3 && MainFragment.accountInfo.housingPlan < 1e-3 &&
                MainFragment.accountInfo.transportPlan < 1e-3 && MainFragment.accountInfo.shoppingPlan < 1e-3 &&
                MainFragment.accountInfo.othersPlan < 1e-3 && MainFragment.accountInfo.savingsPlan < 1e-3
            )
                findNavController().navigate(R.id.action_GoalFragment_to_PlanningStartFragment)
            else findNavController().navigate(R.id.action_GoalFragment_to_PlanningFragment)
        }

        when {
            newGoal.currentAmount >= newGoal.totalAmount -> {
                goal_success_message.text =
                    "Parabéns você cumpriu a sua meta! Agora você pode " +
                            "resgatar esse valor e deletar essa meta clicando no botão abaixo"
                goal_main_image.setImageResource(R.drawable.ic_goal_success)
                how_to_improve_button_text.text = "RESGATAR DINHEIRO"
                button_add_item.setOnClickListener {
                    val bundle = Bundle()
                    bundle.putString("mode", "Resgatar dinheiro")
                    bundle.putFloat("value", newGoal.totalAmount.toFloat())
                    val goalRef = FirebaseDatabase.getInstance()
                        .getReference("${(activity as MainActivity).androidId}/goal_list/$goalKey")
                    goalRef.removeValue()
                    findNavController().navigate(
                        R.id.action_GoalFragment_to_InsertMoneyFragment,
                        bundle
                    )
                }
            }
            newGoal.endDate >= newGoal.predictedEndDate && newGoal.predictedEndDate >= 0 -> {
                goal_main_image.setImageResource(R.drawable.ic_going_to_goal)
                goal_success_message.text =
                    "Parabéns, você está a caminho de cumprir essa meta" +
                            " até o prazo estabelecido de " + SimpleDateFormat(
                        "dd/MM/yyyy",
                        Locale.US
                    ).format(Date(newGoal.endDate).time) + "!"
            }
            else -> {
                goal_main_image.setImageResource(R.drawable.ic_falling)
                goal_success_message.text =
                    "Você não está economizando o suficiente para cumprir" +
                            " essa meta até o prazo estabelecido de " + SimpleDateFormat(
                        "dd/MM/yyyy",
                        Locale.US
                    ).format(Date(newGoal.endDate).time) + "! Clique abaixo para saber como melhorar"
            }
        }
    }

    override fun onBackPressed(): Boolean {
        button_return.performClick()
        return true
    }

    companion object {
        fun priorityIntToString(priority: Int): String {
            assert(priority in 0..2)
            when (priority) {
                0 -> return "Baixa"
                1 -> return "Média"
                2 -> return "Alta"
            }
            return ""
        }

        fun priorityStringToInt(priority: String): Int {
            when (priority) {
                "Baixa" -> return 0
                "Média" -> return 1
                "Alta" -> return 2
            }
            return -1
        }
    }
}
