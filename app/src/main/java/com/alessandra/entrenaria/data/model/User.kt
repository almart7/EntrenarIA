package com.alessandra.entrenaria.data.model

data class User(
    val uid: String = "",
    val email: String? = null,
    val name: String? = null,
    val age: Int? = null,
    val gender: String? = null
)
