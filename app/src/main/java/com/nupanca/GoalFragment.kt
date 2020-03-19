package com.nupanca

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
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
    private var goalKey : String? = null
    private var goal: Goal? = null
    // TODO check if this is really a good way to do this
    private lateinit var database: DatabaseReference

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

        database = FirebaseDatabase.getInstance().getReference("goal_list")

        val childEventListener = object : ChildEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildRemoved(p0: DataSnapshot) {
            }

            @SuppressLint("SetTextI18n")
            override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {
                val newGoal = Goal.fromMap(dataSnapshot.value as HashMap<String, Any>)
                if (newGoal.key == goalKey){
                    goal = newGoal
                    goal_fragment_title.text = newGoal.title

                    val symb = DecimalFormatSymbols()
                    symb.decimalSeparator = ','
                    symb.groupingSeparator = '.'
                    val df = DecimalFormat("###,##0.00", symb)
                    money_to_goal.text = "R$ ${df.format(newGoal.currentAmount)} " +
                            "de R$ ${df.format(newGoal.totalAmount)}"

                    completion_date_information.text = "Esta meta tem prioridade " +
                            priorityIntToString(newGoal.priority) + " e a data prevista para término" +
                            " é " + SimpleDateFormat("dd/MM/yyyy",
                        Locale.US).format(Date(newGoal.predictedEndDate).time)

                    // Progress bar
                    var progress = (100 * newGoal.currentAmount / newGoal.totalAmount).toInt()
                    if (progress > 100) progress = 100
                    goal_progress_bar.progress = progress
                    goal_progress_bar_text.text = progress.toString() + "%"


                    button_add_item.setOnClickListener {
                        if (MainFragment.accountInfo.foodPlan < 1e-3 && MainFragment.accountInfo.housingPlan < 1e-3 &&
                            MainFragment.accountInfo.transportPlan < 1e-3 && MainFragment.accountInfo.shoppingPlan < 1e-3 &&
                            MainFragment.accountInfo.othersPlan < 1e-3 && MainFragment.accountInfo.savingsPlan < 1e-3)
                            findNavController().navigate(R.id.action_GoalFragment_to_ControlStartFragment)
                        else findNavController().navigate(R.id.action_GoalFragment_to_ControlFragment)
                    }

                    when {
                        newGoal.currentAmount >= newGoal.totalAmount -> {
                            goal_success_message.text = "Parabéns você cumpriu a sua meta! Agora você pode " +
                                    "resgatar esse valor e deletar essa meta clicando no botão abaixo"
                            goal_main_image.setImageResource(R.drawable.ic_goal_success)
                            how_to_improve_button_text.text = "RESGATAR DINHEIRO"
                            button_add_item.setOnClickListener {
                                val bundle = Bundle()
                                bundle.putString("mode", "Guardar dinheiro")
                                bundle.putFloat("value", newGoal.totalAmount.toFloat())
                                val goalRef = FirebaseDatabase.getInstance()
                                    .getReference("goal_list/$goalKey")
                                goalRef.removeValue()
                                findNavController().navigate(R.id.action_GoalFragment_to_InsertMoneyFragment,
                                    bundle)
                            }
                        }
                        newGoal.endDate >= newGoal.predictedEndDate -> {
                            goal_main_image.setImageResource(R.drawable.ic_going_to_goal)
                            goal_success_message.text = "Parabéns, você está a caminho de cumprir essa meta" +
                                    " até o prazo estabelecido de " + SimpleDateFormat("dd/MM/yyyy",
                                Locale.US).format(Date(newGoal.endDate).time) + "!"
                        }
                        else -> {
                            goal_main_image.setImageResource(R.drawable.ic_falling)
                            goal_success_message.text = "Você não está economizando o suficiente para cumprir" +
                                    " essa meta até o prazo estabelecido de " + SimpleDateFormat("dd/MM/yyyy",
                                Locale.US).format(Date(newGoal.endDate).time) + "! Clique abaixo para saber como melhorar"
                        }
                    }
                }
            }
        }

        database.addChildEventListener(childEventListener)

        button_return.setOnClickListener {
            button_return.startAnimation(
                AnimationUtils.loadAnimation(context, R.anim.alpha_reduction)
            )
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

    override fun onBackPressed(): Boolean {
        button_return.performClick()
        return true
    }

    companion object {
        fun priorityIntToString(priority: Int): String {
            assert(priority in 0..4)
            when (priority){
                0 -> return "Muito Baixa"
                1 -> return "Baixa"
                2 -> return "Média"
                3 -> return "Alta"
                4 -> return "Muito Alta"
            }
            return ""
        }
        fun priorityStringToInt(priority: String): Int {
            when(priority){
                "Muito Baixa" -> return 0
                "Baixa" -> return 1
                "Média" -> return 2
                "Alta" -> return 3
                "Muito Alta" -> return 4
            }
            return -1
        }
    }
}
