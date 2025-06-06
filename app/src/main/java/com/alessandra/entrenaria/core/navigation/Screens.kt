package com.alessandra.entrenaria.core.navigation

import kotlinx.serialization.Serializable


// Definición de las rutas de navegación (rutas tipadas)
/*
 Este archivo define los destinos de navegación como clases serializables.
 Esto permite pasar argumentos fácilmente y navegar con seguridad de tipos
*/
@Serializable object Auth
@Serializable object User
@Serializable object TrainingPeriods
@Serializable object Chat

@Serializable data class TrainingDays(val periodId: String)
@Serializable data class ExerciseList(val periodId: String, val dayId: String)
@Serializable data class NewExercise(val periodId: String, val dayId: String, val exerciseId: String? = null)
@Serializable data class ExerciseDetail(val exerciseId: String)
