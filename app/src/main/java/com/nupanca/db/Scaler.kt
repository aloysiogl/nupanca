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

    fun scale(accountInfo: AccountInfo): DoubleArray {
        val v = doubleArrayOf(
            accountInfo.income, accountInfo.region.toDouble(), accountInfo.sex.toDouble(),
            accountInfo.age.toDouble(), accountInfo.marriage.toDouble(),
            accountInfo.savings30Days.toDouble(), accountInfo.food30Days.toDouble(),
            accountInfo.shopping30Days.toDouble(), accountInfo.housing30Days.toDouble(),
            accountInfo.transport30Days.toDouble(), accountInfo.others30Days.toDouble()
            )
        for (i in 0..v.size) {
            v[i] = (v[i] - mean[i]) / variance[i]
        }
        return v
    }
}