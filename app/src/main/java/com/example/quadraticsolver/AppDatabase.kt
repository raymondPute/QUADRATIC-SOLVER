package com.example.quadraticsolver

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Equation::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun equationDao(): EquationDao
}