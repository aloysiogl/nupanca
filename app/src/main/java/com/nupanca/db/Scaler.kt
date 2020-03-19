package com.nupanca.db

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class Scaler {
    val db = FirebaseDatabase.getInstance();
    val scalerRef = db.getReference("scaler")
    var mean: DoubleArray = doubleArrayOf()
    var variance: DoubleArray = doubleArrayOf()

    init {
        scalerRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val scaler = dataSnapshot.value as HashMap<String, ArrayList<Double>>
                Log.d("TAG", "$scaler")
                Log.d("TAG", "${scaler["mean"]}")
                mean = scaler["mean"]?.toDoubleArray()!!
                variance = scaler["std"]?.toDoubleArray()!!
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w("TAG", "Failed to read account info.", error.toException())
            }
        })
    }

    fun scale(accountInfo: AccountInfo): IntArray {
        val v = intArrayOf(
            accountInfo.income.toInt(), accountInfo.region.toInt(), accountInfo.sex.toInt(),
            accountInfo.age.toInt(), accountInfo.marriage.toInt(),
            accountInfo.savings30Days.toInt(), accountInfo.food30Days.toInt(),
            accountInfo.shopping30Days.toInt(), accountInfo.housing30Days.toInt(),
            accountInfo.transport30Days.toInt(), accountInfo.others30Days.toInt()
            )
        for (i in v.indices) {
            v[i] = (1.0 * (v[i] - mean[i]) / variance[i]).toInt()
        }
        return v
    }
}