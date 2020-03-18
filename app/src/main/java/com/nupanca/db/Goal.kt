package com.nupanca.db


data class Goal(
    var key: String? = "",
    var title: String,
    var totalAmount: Double,
    var currentAmount: Double,
    var beginDate: Long, // Saving as epoch. Use Date(long) to retrieve the date.
    var endDate: Long,
    var predictedEndDate: Long,
    var priority: Int
)