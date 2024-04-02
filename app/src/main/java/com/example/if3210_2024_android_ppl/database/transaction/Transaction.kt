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

    @ColumnInfo(name = "price")
    val price: Int?,

    @ColumnInfo(name = "location")
    val location: String?,

    @ColumnInfo(name = "date")
    val date: String?,

    @ColumnInfo(name = "category")
    val category: String?,

    @ColumnInfo(name = "latitude")
    val latitude: Double?,

    @ColumnInfo(name = "longitude")
    val longitude: Double?
)

