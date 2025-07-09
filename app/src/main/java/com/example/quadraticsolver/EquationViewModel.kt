package com.example.quadraticsolver

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import kotlinx.coroutines.launch

class EquationViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: EquationRepository
    val allEquations: LiveData<List<Equation>>

    init {
        val equationDao = Room.databaseBuilder(
            application,
            AppDatabase::class.java, "equation-database"
        ).build().equationDao()
        repository = EquationRepository(equationDao)
        allEquations = repository.allEquations
    }

    fun insert(equation: Equation) = viewModelScope.launch {
        repository.insert(equation)
    }
}