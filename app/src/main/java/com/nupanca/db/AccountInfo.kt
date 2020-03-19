package com.nupanca.db


data class AccountInfo(
    var id: Int = 0,
    var accountBalance: Double = 0.0,
    var savingsBalance: Double = 0.0,
    var foodPlan: Long = 0,
    var foodThisMonth: Long = 0,
    var transportPlan: Long = 0,
    var transportThisMonth: Long = 0,
    var shoppingPlan: Long = 0,
    var shoppingThisMonth: Long = 0,
    var housingPlan: Long = 0,
    var housingThisMonth: Long = 0,
    var othersPlan: Long = 0,
    var othersThisMonth: Long = 0,
    var savingsPlan: Long = 0,
    var savingsThisMonth: Long = 0
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
            ai.foodThisMonth = map["foodThisMonth"] as Long
            ai.transportPlan = map["transportPlan"] as Long
            ai.transportThisMonth = map["transportThisMonth"] as Long
            ai.shoppingPlan = map["shoppingPlan"] as Long
            ai.shoppingThisMonth = map["shoppingThisMonth"] as Long
            ai.housingPlan = map["housingPlan"] as Long
            ai.housingThisMonth = map["housingThisMonth"] as Long
            ai.othersPlan = map["othersPlan"] as Long
            ai.othersThisMonth = map["othersThisMonth"] as Long
            ai.savingsPlan = map["savingsPlan"] as Long
            ai.savingsThisMonth = map["savingsThisMonth"] as Long
            return ai
        }
    }
}
