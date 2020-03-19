package com.nupanca.db

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlin.math.pow
import kotlin.math.sqrt

class KMeansPredictor {
    val db = FirebaseDatabase.getInstance();
    val centroidsRef = db.getReference("kmeans_centroids")
    var centroids: ArrayList<ArrayList<Double>> = arrayListOf()

    init {
        centroidsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                centroids = dataSnapshot.value as ArrayList<ArrayList<Double>>
                Log.d("TAG", "Centroids: $centroids")
                Log.d("TAG", "Centroids size: ${centroids.size}")
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w("TAG", "Failed to read account info.", error.toException())
            }
        })
    }
    fun predict(input: IntArray): Int {
        var bestCluster = 0
        var difBestCluster = Double.MAX_VALUE

        for (c in 1 until centroids.size) {
            var diff = 0.0
            for (i in 1 until 11)
                diff += (input[i] - centroids[c][i]).pow(2)
            diff = sqrt(diff)
            Log.d("TAG", "Dif cluster $c: $diff")
            if (diff < difBestCluster) {
                difBestCluster = diff
                bestCluster = c
            }
        }
        Log.d("TAG", "Best cluster: $bestCluster")
        return bestCluster
    }
}