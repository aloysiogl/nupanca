package com.nupanca.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class GoalsDBHandler(context: Context, name: String?,
                     factory: SQLiteDatabase.CursorFactory?, version: Int) :
        SQLiteOpenHelper(context, DATABASE_NAME, factory, DATABASE_VERSION) {

    companion object {
        private var id_counter = 0

        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "goalsDB.db"
        const val TABLE_GOALS = "goals"

        const val COLUMN_ID = "_id"
        const val COLUMN_TITLE = "title"
        const val COLUMN_TOTAL_AMOUNT = "total_amount"
        const val COLUMN_CURRENT_AMOUNT = "current_amount"
        const val COLUMN_BEGIN_DATE = "begin_date"
        const val COLUMN_END_DATE = "end_date"
        const val COLUMNS_PREDICTED_END_DATE = "predicted_end_date"
        const val COLUMN_PRIORITY = "priority"
    }

    override fun onCreate(db: SQLiteDatabase) {
        Log.d("TAG", "creating db")

        val create_goals_table = ("CREATE TABLE " +
                TABLE_GOALS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY," +
                COLUMN_TITLE + " TEXT," +
                COLUMN_TOTAL_AMOUNT + " DOUBLE," +
                COLUMN_CURRENT_AMOUNT + " DOUBLE," +
                COLUMN_BEGIN_DATE + " STRING," +
                COLUMN_END_DATE + " STRING," +
                COLUMNS_PREDICTED_END_DATE + " STRING," +
                COLUMN_PRIORITY + " INTEGER" +
                ")")
        db.execSQL(create_goals_table)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_GOALS")
        onCreate(db)
    }

    fun addGoal(goal: Goal) {
        val values = ContentValues()
        goal.id = id_counter++
        values.put(COLUMN_ID, goal.id)
        values.put(COLUMN_TITLE, goal.title)
        values.put(COLUMN_TOTAL_AMOUNT, goal.totalAmount)
        values.put(COLUMN_CURRENT_AMOUNT, goal.currentAmount)
        values.put(COLUMN_BEGIN_DATE, goal.beginDate.toString())
        values.put(COLUMN_END_DATE, goal.endDate.toString())
        values.put(COLUMNS_PREDICTED_END_DATE, goal.predictedEndDate.toString())
        values.put(COLUMN_PRIORITY, goal.priority)

        val db = this.writableDatabase
        Log.d("TAG", "inserting in db")
        db.insert(TABLE_GOALS, null, values)
        db.close()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun findGoal(title: String): Goal? {
        val query =
            "SELECT * FROM $TABLE_GOALS WHERE $COLUMN_TITLE =  \"$title\""

        val db = this.writableDatabase
        val cursor = db.rawQuery(query, null)

        var goal: Goal? = null

        if (cursor.moveToFirst()) {
            cursor.moveToFirst()
            val id = Integer.parseInt(cursor.getString(0))
            val title = cursor.getString(1)
            val totalAmount = cursor.getString(2).toDouble()
            val currentAmount = cursor.getString(3).toDouble()
            val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
            val beginDate = LocalDate.parse(cursor.getString(4), formatter)
            val endDate = LocalDate.parse(cursor.getString(5), formatter)
            val predictedEndDate = LocalDate.parse(cursor.getString(6), formatter)
            val priority = cursor.getString(7).toInt()

            goal = Goal(id, title, totalAmount, currentAmount, beginDate, endDate,
                predictedEndDate, priority)
            cursor.close()
        }

        db.close()
        return goal
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun readDB(): MutableList<Goal>? {
        val query = "SELECT * FROM $TABLE_GOALS"

        val db = this.writableDatabase
        val cursor = db.rawQuery(query, null)

        var goals: MutableList<Goal>? = null

        while (cursor.moveToNext()) {
            val id = Integer.parseInt(cursor.getString(0))
            val title = cursor.getString(1)
            val totalAmount = cursor.getString(2).toDouble()
            val currentAmount = cursor.getString(3).toDouble()
            val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
            val beginDate = LocalDate.parse(cursor.getString(4), formatter)
            val endDate = LocalDate.parse(cursor.getString(5), formatter)
            val predictedEndDate = LocalDate.parse(cursor.getString(6), formatter)
            val priority = cursor.getString(7).toInt()

            val goal = Goal(id, title, totalAmount, currentAmount, beginDate, endDate,
                predictedEndDate, priority)
            if (goals == null)
                goals = mutableListOf(goal)
            else goals.add(goal)
        }

        db.close()
        return goals
    }

    fun deleteGoal(id: Int): Boolean {
        var result = false

        val query =
            "SELECT * FROM $TABLE_GOALS WHERE $COLUMN_ID = \"$id\""
        val db = this.writableDatabase
        val cursor = db.rawQuery(query, null)

        if (cursor.moveToFirst()) {
            val id = cursor.getString(0).toInt()
            db.delete(TABLE_GOALS, "$COLUMN_ID = ?",
                arrayOf(id.toString()))
            cursor.close()
            result = true
        }
        db.close()
        return result
    }
}