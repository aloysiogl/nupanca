package com.nupanca.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Build

class AccountInfoDBHandler(context: Context,
                           factory: SQLiteDatabase.CursorFactory?) :
        SQLiteOpenHelper(context, DATABASE_NAME, factory, DATABASE_VERSION) {

    companion object {
        private var id_counter = 0

        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "accountInfoDB.db"
        const val TABLE_ACCOUNT_INFO = "account_info"

        const val COLUMN_ID = "_id"
        const val COLUMN_ACCOUNT_BALANCE = "account_balance"
        const val COLUMN_SAVINGS_BALANCE = "savings_balance"
        const val COLUMN_FOOD_PLAN = "food_plan"
        const val COLUMN_TRANSPORT_PLAN = "transport_plan"
        const val COLUMN_SHOPPING_PLAN = "shopping_plan"
        const val COLUMN_HOUSING_PLAN = "housing_plan"
        const val COLUMNS_OTHERS_PLAN = "others_plan"
        const val COLUMN_SAVINGS_PLAN = "savings_plan"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val create_goals_table = ("CREATE TABLE " +
                TABLE_ACCOUNT_INFO + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY," +
                COLUMN_ACCOUNT_BALANCE + " DOUBLE," +
                COLUMN_SAVINGS_BALANCE + " DOUBLE," +
                COLUMN_FOOD_PLAN + " DOUBLE," +
                COLUMN_TRANSPORT_PLAN + " DOUBLE," +
                COLUMN_SHOPPING_PLAN + " DOUBLE," +
                COLUMN_HOUSING_PLAN + " DOUBLE," +
                COLUMNS_OTHERS_PLAN + " DOUBLE," +
                COLUMN_SAVINGS_PLAN + " DOUBLE" +
                ")")
        db.execSQL(create_goals_table)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_ACCOUNT_INFO")
        onCreate(db)
    }

    fun addAccount(account: AccountInfo) {
        val values = ContentValues()
        account.id = id_counter++
        values.put(COLUMN_ID, account.id)
        values.put(COLUMN_ACCOUNT_BALANCE, account.accountBalance)
        values.put(COLUMN_SAVINGS_BALANCE, account.savingsBalance)
        values.put(COLUMN_FOOD_PLAN, account.foodPlan)
        values.put(COLUMN_TRANSPORT_PLAN, account.transportPlan)
        values.put(COLUMN_SHOPPING_PLAN, account.shoppingPlan)
        values.put(COLUMN_HOUSING_PLAN, account.housingPlan)
        values.put(COLUMNS_OTHERS_PLAN, account.othersPlan)
        values.put(COLUMN_SAVINGS_PLAN, account.savingsPlan)

        val db = this.writableDatabase
        db.insert(TABLE_ACCOUNT_INFO, null, values)
        db.close()
    }

    fun findAccount(id: Int): AccountInfo? {
        val query =
            "SELECT * FROM $TABLE_ACCOUNT_INFO WHERE $COLUMN_ID =  \"$id\""

        val db = this.writableDatabase
        val cursor = db.rawQuery(query, null)

        var account: AccountInfo? = null

        if (cursor.moveToFirst()) {
            cursor.moveToFirst()
            val id = cursor.getString(0).toInt()
            val accountBalance = cursor.getString(1).toDouble()
            val savingsBalance = cursor.getString(2).toDouble()
            val foodPlan = cursor.getString(3).toDouble()
            val transportPlan = cursor.getString(4).toDouble()
            val shoppingPlan = cursor.getString(5).toDouble()
            val housingPlan = cursor.getString(6).toDouble()
            val othersPlan = cursor.getString(7).toDouble()
            val savingsPlan = cursor.getString(8).toDouble()

            account = AccountInfo(id, accountBalance, savingsBalance, foodPlan, transportPlan,
                shoppingPlan, housingPlan, othersPlan, savingsPlan)
            cursor.close()
        }

        db.close()
        return account
    }

    fun deleteAccount(id: Int): Boolean {
        var result = false

        val query =
            "SELECT * FROM $TABLE_ACCOUNT_INFO WHERE $COLUMN_ID = \"$id\""
        val db = this.writableDatabase
        val cursor = db.rawQuery(query, null)

        if (cursor.moveToFirst()) {
            val id = cursor.getString(0).toInt()
            db.delete(TABLE_ACCOUNT_INFO, "$COLUMN_ID = ?",
                arrayOf(id.toString()))
            cursor.close()
            result = true
        }
        db.close()
        return result
    }
}