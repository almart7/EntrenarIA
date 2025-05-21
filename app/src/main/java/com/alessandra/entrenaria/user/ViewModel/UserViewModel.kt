package com.alessandra.entrenaria.user.ViewModel

import androidx.lifecycle.ViewModel
import com.alessandra.entrenaria.user.data.model.User
import com.alessandra.entrenaria.user.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// ViewModel encargado de gestionar el estado de la pantalla de perfil del usuario.
// Se comunica con el UserRepository para cargar y actualizar los datos del usuario autenticado.
class UserViewModel(
    private val repository: UserRepository // Se inyecta el repositorio que contiene la lógica de acceso a Firestore
) : ViewModel() {

    // Estado interno mutable que representa los datos del usuario actual
    private val _user = MutableStateFlow<User?>(null)
    // Estado expuesto como flujo inmutable para ser observado por la UI
    val user: StateFlow<User?> = _user.asStateFlow()

    // Carga los datos del usuario autenticado desde Firestore.
    fun loadUserData() {
        repository.getCurrentUser { user ->
            _user.value = user
        }
    }

    // Actualiza los datos del perfil del usuario en Firestore.
    fun updateUserProfile(
        name: String,
        age: Int,
        gender: String,
        onSuccess: () -> Unit,
        onFailure: () -> Unit
    ) {
        repository.updateUserProfile(name, age, gender, onSuccess, onFailure)
    }
}
