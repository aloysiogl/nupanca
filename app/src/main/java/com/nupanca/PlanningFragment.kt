package com.nupanca

import android.app.AlertDialog
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.database.*
import com.nupanca.db.AccountInfo
import kotlinx.android.synthetic.main.fragment_planning.*
import kotlinx.android.synthetic.main.fragment_planning_category.view.*
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class PlanningFragment : BaseFragment() {
    private val db = FirebaseDatabase.getInstance()
    private var accountInfoRef: DatabaseReference? = null
    private var accountInfo = AccountInfo()
    private val df: DecimalFormat

    init {
        val symb = DecimalFormatSymbols()
        symb.decimalSeparator = ','
        symb.groupingSeparator = '.'
        df = DecimalFormat("###,##0", symb)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Getting reference to account info in the database
        accountInfoRef =
            db.getReference("${(activity as MainActivity).androidId}/account_info")
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_planning, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getView()?.let { ViewCompat.setTranslationZ(it, 4f) }
        handleFirebase()

        layout_scroll.post { layout_scroll.scrollTo(0, (activity as MainActivity).planningScroll) }

        button_return.setOnClickListener {
            button_return.startAnimation(
                AnimationUtils.loadAnimation(context, R.anim.alpha_reduction)
            )
            (activity as MainActivity).planningScroll = 0
            findNavController().navigate(R.id.action_PlanningFragment_to_MainFragment)
        }

        button_info.setOnClickListener {
            button_info.startAnimation(
                AnimationUtils.loadAnimation(
                    context, R.anim.alpha_reduction
                )
            )
            val builder: AlertDialog.Builder? = activity?.let {
                AlertDialog.Builder(it)
            }
            builder?.setMessage(R.string.planning_info)
            val dialog: AlertDialog? = builder?.create()
            dialog?.show()

            val textView = dialog?.findViewById<View>(android.R.id.message) as TextView
            textView.typeface = context?.let { it1 ->
                ResourcesCompat.getFont(
                    it1,
                    R.font.keep_calm_w01_book
                )
            }
            textView.linksClickable = true
            textView.movementMethod = LinkMovementMethod.getInstance()
        }

        fun listener(category: String) {
            val bundle = Bundle()
            bundle.putString("category", category)

            (activity as MainActivity).planningScroll = layout_scroll.scrollY

            findNavController().navigate(
                R.id.action_PlanningFragment_to_PlanningEditFragment,
                bundle
            )
        }

        savings_category.title.text = context?.getString(R.string.savings_title)
        savings_category.icon.setImageResource(R.drawable.ic_pig)
        savings_category.layout_card.setOnClickListener { listener("savings") }

        housing_category.title.text = context?.getString(R.string.housing_title)
        housing_category.icon.setImageResource(R.drawable.ic_house)
        housing_category.layout_card.setOnClickListener { listener("housing") }

        transport_category.title.text = context?.getString(R.string.transport_title)
        transport_category.icon.setImageResource(R.drawable.ic_car)
        transport_category.layout_card.setOnClickListener { listener("transport") }

        food_category.title.text = context?.getString(R.string.food_title)
        food_category.icon.setImageResource(R.drawable.ic_fork)
        food_category.layout_card.setOnClickListener { listener("food") }

        shopping_category.title.text = context?.getString(R.string.shopping_title)
        shopping_category.icon.setImageResource(R.drawable.ic_fashion)
        shopping_category.layout_card.setOnClickListener { listener("shopping") }

        others_category.title.text = context?.getString(R.string.others_title)
        others_category.icon.setImageResource(R.drawable.ic_puzzle)
        others_category.layout_card.setOnClickListener { listener("others") }
    }

    fun handleFirebase() {
        accountInfoRef?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                accountInfo = AccountInfo.fromMap(dataSnapshot.value as HashMap<String, Any>)

                if (context == null || savings_category == null || housing_category == null ||
                    transport_category == null || food_category == null ||
                    shopping_category == null || others_category == null
                )
                    return

                savings_category?.plan?.text = df.format(accountInfo.savingsPlan)
                savings_category?.spendings?.text = df.format(accountInfo.savings30Days)

                housing_category?.plan?.text = df.format(accountInfo.housingPlan)
                housing_category?.spendings?.text = df.format(accountInfo.housing30Days)

                transport_category?.plan?.text = df.format(accountInfo.transportPlan)
                transport_category?.spendings?.text = df.format(accountInfo.transport30Days)

                food_category?.plan?.text = df.format(accountInfo.foodPlan)
                food_category?.spendings?.text = df.format(accountInfo.food30Days)

                shopping_category?.plan?.text = df.format(accountInfo.shoppingPlan)
                shopping_category?.spendings?.text = df.format(accountInfo.shopping30Days)

                others_category?.plan?.text = df.format(accountInfo.othersPlan)
                others_category?.spendings?.text = df.format(accountInfo.others30Days)
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w("TAG", "Failed to read account info.", error.toException())
            }
        })
    }

    override fun onBackPressed(): Boolean {
        button_return.performClick()
        return true
    }
}
