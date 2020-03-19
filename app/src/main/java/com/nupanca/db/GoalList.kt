package com.nupanca.db

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.firebase.database.*

class GoalList(context: Context) {
    private val db = FirebaseDatabase.getInstance();
    private val listRef = db.getReference("goal_list")
    var goals = hashMapOf<String?, Goal>()

    init {

        val childEventListener = object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {
                Log.d("TAG", "onChildAdded:" + dataSnapshot.key!!)
                goals = dataSnapshot.value as HashMap<String?, Goal>
                Log.d("TAG", "Goal size: ${goals.size}")
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, previousChildName: String?) {
                Log.d("TAG", "onChildChanged: ${dataSnapshot.key}")
                val newGoal = dataSnapshot.value as Goal
                goals[dataSnapshot.key] = newGoal
            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                Log.d("TAG", "onChildRemoved:" + dataSnapshot.key!!)
                val goal = dataSnapshot.value as Goal
                goals.remove(dataSnapshot.key)
            }

            override fun onChildMoved(dataSnapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("TAG", "postComments:onCancelled", databaseError.toException())
                Toast.makeText(context, "Erro ao carregar metas.",
                    Toast.LENGTH_SHORT).show()
            }
        }
        listRef.addChildEventListener(childEventListener)
    }

    fun addGoal(goal: Goal) {
        Log.d("TAG", "Goal list adding goal")
        val goalRef = listRef.push()
        goal.key = goalRef.key.toString()
        goals[goal.key] = goal
        goalRef.setValue(goal)
    }

    fun editGoal(goal: Goal) {
        val query = listRef.orderByKey().equalTo(goal.key)
        print(query)
    }

    fun deleteGoal(goal: Goal) {
        // TODO
    }
}