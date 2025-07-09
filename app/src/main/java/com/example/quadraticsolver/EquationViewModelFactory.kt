package com.example.quadraticsolver

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class EquationViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EquationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EquationViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}