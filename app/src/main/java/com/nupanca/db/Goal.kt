package com.nupanca.db


data class Goal(
    // TODO change the nullable
    var key: String? = "",
    var title: String? = "",
    var totalAmount: Double = 0.0,
    var currentAmount: Double = 0.0,
    var beginDate: Long? = 0, // Saving as epoch. Use Date(long) to retrieve the date.
    var endDate: Long = 0,
    var predictedEndDate: Long? = 0,
    var priority: Int? = 0
) {
    companion object {
        fun fromMap(map: HashMap<String, Any>): Goal {
            val goal = Goal()
            goal.key = map["key"] as String
            goal.title = map["title"] as String
            if (map["totalAmount"] is Long)
                goal.totalAmount = (map["totalAmount"] as Long).toDouble()
            else goal.totalAmount = map["totalAmount"] as Double
            if (map["currentAmount"] is Long)
                goal.currentAmount = (map["currentAmount"] as Long).toDouble()
            else goal.currentAmount = map["currentAmount"] as Double
            goal.beginDate = map["beginData"] as Long?
            goal.endDate = map["endDate"] as Long
            goal.predictedEndDate = map["predictedEndDate"] as Long?
            goal.priority = (map["priority"] as Long).toInt()
            return goal
        }
    }
}