package com.example.if3210_2024_android_ppl.database.transaction

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [Transaction::class],
    version = 1,
    exportSchema = false
)
abstract class TransactionDatabase: RoomDatabase() {

    abstract fun transactionDao(): TransactionDao

    companion object {
        @Volatile private var INSTANCE: TransactionDatabase? = null
        private  val LOCK = Any()

        operator fun invoke(context: Context) = INSTANCE?: synchronized(LOCK){
            INSTANCE?: buildDatabase(context).also {
                INSTANCE = it
            }
        }

        fun getDatabase(context: Context): TransactionDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null){
                return  tempInstance
            }
            synchronized(this) {
                val instance = buildDatabase(context.applicationContext)
                INSTANCE = instance
                return instance

            }
        }

        private fun buildDatabase(context: Context) = Room.databaseBuilder(
            context.applicationContext,
            TransactionDatabase::class.java,
            "transaction_database"
        ).build()
    }
}