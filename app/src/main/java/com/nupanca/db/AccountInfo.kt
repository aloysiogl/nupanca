package com.nupanca.db

data class AccountInfo(
    var id: Int,
    var accountBalance: Double = 0.0,
    var savingsBalance: Double = 0.0,
    var foodPlan: Double = 0.0,
    var transportPlan: Double = 0.0,
    var shoppingPlan: Double = 0.0,
    var housingPlan: Double = 0.0,
    var othersPlan: Double = 0.0,
    var savingsPlan: Double = 0.0
)
