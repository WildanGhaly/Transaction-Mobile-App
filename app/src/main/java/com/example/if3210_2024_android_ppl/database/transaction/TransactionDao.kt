package com.example.if3210_2024_android_ppl.database.transaction

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface TransactionDao {

    @Insert
    suspend fun addTransaction(transaction: Transaction)

    @Insert
    suspend fun addMultiTransaction(multiTransaction: List<Transaction>)

    @Update
    suspend fun updateTransaction(transaction: Transaction)

    @Delete
    suspend fun deleteTransaction(transaction: Transaction)

    @Query("SELECT * FROM transaction_table")
    suspend fun getTransactions(): List<Transaction>

    @Query("SELECT * FROM transaction_table WHERE id=:id")
    suspend fun getTransactions(id: Int): List<Transaction>

    @Query("SELECT * FROM transaction_table WHERE idUser=:id ORDER BY id DESC")
    suspend fun getTransactions(id: String?): List<Transaction>
}