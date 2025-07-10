package com.example.quadraticsolver

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface EquationDao {
    @Query("SELECT * FROM equations ORDER BY id DESC") // ✅ Fixed
    fun getAllEquations(): Flow<List<Equation>>

    @Insert
    suspend fun insert(equation: Equation)

    @Delete
    suspend fun delete(equation: Equation)

    @Query("DELETE FROM equations WHERE id NOT IN (SELECT id FROM equations ORDER BY id DESC LIMIT :limit)") // ✅ Fixed
    suspend fun deleteExcessEquations(limit: Int)
}
