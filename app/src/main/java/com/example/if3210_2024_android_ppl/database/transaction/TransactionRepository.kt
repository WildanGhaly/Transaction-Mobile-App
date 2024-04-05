package com.example.if3210_2024_android_ppl.database.transaction

class TransactionRepository(private val transactionDao: TransactionDao) {

    suspend fun addTransaction(transaction: Transaction) {
        transactionDao.addTransaction(transaction)
    }

    suspend fun addMultiTransaction(multiTransaction: List<Transaction>) {
        transactionDao.addMultiTransaction(multiTransaction)
    }

    suspend fun updateTransaction(transaction: Transaction) {
        transactionDao.updateTransaction(transaction)
    }

    suspend fun deleteTransaction(transaction: Transaction) {
        transactionDao.deleteTransaction(transaction)
    }

    suspend fun getTransactions(): List<Transaction> {
        return transactionDao.getTransactions()
    }

    suspend fun getTransactions(id: Int): List<Transaction> {
        return transactionDao.getTransactions(id)
    }

    suspend fun getTransactions(userId: String?): List<Transaction> {
        return transactionDao.getTransactions(userId)
    }

}