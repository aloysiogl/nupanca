package com.nupanca.db

import java.time.LocalDate

data class Goal(
    var id: Int = 0,
    var title: String,
    var totalAmount: Double,
    var currentAmount: Double,
    var beginDate: LocalDate,
    var endDate: LocalDate,
    var predictedEndDate: LocalDate,
    var priority: Int
)