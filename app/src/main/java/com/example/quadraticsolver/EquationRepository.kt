package com.example.quadraticsolver

import androidx.lifecycle.LiveData
import kotlinx.coroutines.flow.Flow

class EquationRepository(private val equationDao: EquationDao) {
    val allEquations: LiveData<List<Equation>> get() = equationDao.getAllEquations()

    suspend fun insert(equation: Equation) {
        equationDao.insert(equation)
        equationDao.deleteExcessEquations() // Keep only 100 recent equations
    }
}