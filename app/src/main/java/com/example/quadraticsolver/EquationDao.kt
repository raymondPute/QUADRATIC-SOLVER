package com.example.quadraticsolver

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface EquationDao {
    @Insert
    suspend fun insert(equation: Equation)

    @Query("SELECT * FROM equations ORDER BY id DESC")
    fun getAllEquations(): Flow<List<Equation>>

    @Query("DELETE FROM equations WHERE id NOT IN (SELECT id FROM equations ORDER BY id DESC LIMIT 100)")
    suspend fun deleteExcessEquations()
}