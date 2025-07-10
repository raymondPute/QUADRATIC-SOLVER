package com.example.quadraticsolver

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "equations")
data class Equation(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val a: Double,
    val b: Double,
    val c: Double,
    val result: String,
    val steps: String
)