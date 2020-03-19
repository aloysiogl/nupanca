package com.nupanca.db


data class AccountInfo(
    var id: Int = 0,
    var accountBalance: Double = 0.0,
    var savingsBalance: Double = 0.0,
    var foodPlan: Long = 0,
    var food30Days: Long = 0,
    var transportPlan: Long = 0,
    var transport30Days: Long = 0,
    var shoppingPlan: Long = 0,
    var shopping30Days: Long = 0,
    var housingPlan: Long = 0,
    var housing30Days: Long = 0,
    var othersPlan: Long = 0,
    var others30Days: Long = 0,
    var savingsPlan: Long = 0,
    var savings30Days: Long = 0
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
            ai.foodPlan = map["foodPlan"] as Long
            ai.food30Days = map["food30Days"] as Long
            ai.transportPlan = map["transportPlan"] as Long
            ai.transport30Days = map["transport30Days"] as Long
            ai.shoppingPlan = map["shoppingPlan"] as Long
            ai.shopping30Days = map["shopping30Days"] as Long
            ai.housingPlan = map["housingPlan"] as Long
            ai.housing30Days = map["housing30Days"] as Long
            ai.othersPlan = map["othersPlan"] as Long
            ai.others30Days = map["others30Days"] as Long
            ai.savingsPlan = map["savingsPlan"] as Long
            ai.savings30Days = map["savings30Days"] as Long
            return ai
        }
    }
}
