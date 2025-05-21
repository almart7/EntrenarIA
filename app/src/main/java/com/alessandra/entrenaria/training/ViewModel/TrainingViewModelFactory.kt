package com.alessandra.entrenaria.training.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.alessandra.entrenaria.training.data.repository.TrainingRepository

class TrainingViewModelFactory(
    private val repository: TrainingRepository,
    private val userId: String
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TrainingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TrainingViewModel(repository, userId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
