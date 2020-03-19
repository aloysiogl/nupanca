package com.nupanca.db

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ClusterPredictions {
    val db = FirebaseDatabase.getInstance();
    val clusterPredictionsRef = db.getReference("cluster_predictions")
    var predictions: MutableList<HashMap<String, Int>> = mutableListOf()

    init {
        clusterPredictionsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val p = dataSnapshot.value as HashMap<String, String>
                predictions = mutableListOf()
                for (k in p) {
                    val map = k.value.split(",").associateTo(HashMap()) {
                        val (left, right) = it.split(":")
                        left.substring(1 until left.length) to right.toInt()
                    }
                    predictions.add(map)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w("TAG", "Failed to read account info.", error.toException())
            }
        })
    }

    fun getPredictions(cluster: Int): HashMap<String, Int> {
        return predictions[cluster]
    }
}