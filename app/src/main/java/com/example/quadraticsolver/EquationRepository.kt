package com.example.quadraticsolver
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import kotlinx.coroutines.flow.Flow

class EquationRepository(private val equationDao: EquationDao) {
    val allEquations: LiveData<List<Equation>> = equationDao.getAllEquations().asLiveData() // Line 7

    suspend fun insert(equation: Equation) = equationDao.insert(equation)
    suspend fun delete(equation: Equation) = equationDao.delete(equation)
    suspend fun deleteExcessEquations(limit: Int) = equationDao.deleteExcessEquations(limit)
}