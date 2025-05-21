package com.alessandra.entrenaria.user.data.repository

import com.alessandra.entrenaria.user.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class UserRepository {

    // Instancias de Firebase necesarias
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // Obtiene los datos del usuario actual desde Firestore
    fun getCurrentUser(onComplete: (User?) -> Unit) {
        val currentUser = auth.currentUser

        // Comprueba que haya un usuario autenticado
        if (currentUser == null) {
            onComplete(null)
            return
        }

        // Consulta los datos del usuario en firestore
        db.collection("users").document(currentUser.uid)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // Construye el objeto User con los datos del documento
                    val user = User(
                        uid = currentUser.uid,
                        email = document.getString("email"),
                        name = document.getString("name"),
                        age = document.getLong("age")?.toInt(),
                        gender = document.getString("gender")
                    )
                    onComplete(user)
                } else {
                    onComplete(null)
                }
            }
            .addOnFailureListener {
                onComplete(null)
            }
    }

    // Actualiza el perfil del usuario en Firestore
    fun updateUserProfile(
        name: String,
        age: Int,
        gender: String,
        onSuccess: () -> Unit,
        onFailure: () -> Unit
    ) {
        val currentUser = auth.currentUser ?: return onFailure()

        // Datos a actualizar en el documento (coge los introducidos en la pagina)
        val userData = hashMapOf(
            "name" to name,
            "age" to age,
            "gender" to gender
        )

        // Actualiza los datos del usuario en Firestore (merge para no tocar otros datos como email)
        db.collection("users").document(currentUser.uid)
            .set(userData, SetOptions.merge())
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure() }
    }

}
