package com.nupanca.db

import android.accounts.Account

data class AccountInfo(
    var id: Int = 0,
    var accountBalance: Double = 0.0,
    var savingsBalance: Double = 0.0,
    var foodPlan: Double = 0.0,
    var transportPlan: Double = 0.0,
    var shoppingPlan: Double = 0.0,
    var housingPlan: Double = 0.0,
    var othersPlan: Double = 0.0,
    var savingsPlan: Double = 0.0
) {
    companion object {
        fun fromMap(map: HashMap<String, Any>): AccountInfo {
            val ai = AccountInfo()
            ai.id = (map["id"] as Long).toInt()
            if (map["accountBalance"] is Long)
                ai.accountBalance = (map["accountBalance"] as Long).toDouble()
            else ai.accountBalance = map["accountBalance"] as Double
            if (map["savingsBalance"] is Long)
                ai.savingsBalance = (map["savingsBalance"] as Long).toDouble()
            else ai.savingsBalance = map["savingsBalance"] as Double
            if (map["foodPlan"] is Long)
                ai.foodPlan = (map["foodPlan"] as Long).toDouble()
            else ai.foodPlan = map["foodPlan"] as Double
            if (map["transportPlan"] is Long)
                ai.transportPlan = (map["transportPlan"] as Long).toDouble()
            else ai.transportPlan = map["transportPlan"] as Double
            if (map["shoppingPlan"] is Long)
                ai.shoppingPlan = (map["shoppingPlan"] as Long).toDouble()
            else ai.shoppingPlan = map["shoppingPlan"] as Double
            if (map["housingPlan"] is Long)
                ai.housingPlan = (map["housingPlan"] as Long).toDouble()
            else ai.housingPlan = map["housingPlan"] as Double
            if (map["othersPlan"] is Long)
                ai.othersPlan = (map["othersPlan"] as Long).toDouble()
            else ai.othersPlan = map["othersPlan"] as Double
            if (map["savingsPlan"] is Long)
                ai.savingsPlan = (map["savingsPlan"] as Long).toDouble()
            else ai.savingsPlan = map["savingsPlan"] as Double
            return ai
        }
    }
}
