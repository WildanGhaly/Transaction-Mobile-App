package com.example.if3210_2024_android_ppl.database.transaction

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.example.if3210_2024_android_ppl.database.user.User


@Entity(
    tableName = "transaction_table",
//    primaryKeys = ["id", "idUser"],
//    foreignKeys = [ForeignKey(
//        entity = User::class,
//        parentColumns = ["id"],
//        childColumns = ["idUser"],
//        onDelete = ForeignKey.CASCADE // or any other desired action
//    )]
)
data class Transaction(
    @NonNull
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int,

    @NonNull
    @ColumnInfo(name = "idUser")
    val idUser: String?,

    @ColumnInfo(name = "name")
    val name: String?,

    @ColumnInfo(name = "quantity")
    val quantity: Int?,

    @ColumnInfo(name = "price")
    val price: Double?,

    @ColumnInfo(name = "location")
    val location: String? = "ITB",

    @ColumnInfo(name = "date")
    val date: String?,

    @ColumnInfo(name = "category")
    val category: String? = "Pemasukan",

    @ColumnInfo(name = "latitude")
    val latitude: Double? = -6.9274065413170725,

    @ColumnInfo(name = "longitude")
    val longitude: Double? = 107.76996019357847
)

