package com.alessandra.entrenaria.core.navigation


import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.alessandra.entrenaria.auth.ui.screens.AuthScreen
import com.alessandra.entrenaria.auth.ui.screens.AuthViewModel
import com.alessandra.entrenaria.chat.ui.screens.ChatScreen
import com.alessandra.entrenaria.training.ui.screens.exercises.ExerciseDetailScreen
import com.alessandra.entrenaria.training.ui.screens.exercises.ExerciseListScreen
import com.alessandra.entrenaria.training.ui.screens.exercises.NewExerciseScreen
import com.alessandra.entrenaria.training.ui.screens.trainingDays.TrainingDaysScreen
import com.alessandra.entrenaria.training.ui.screens.trainingPeriods.TrainingPeriodsScreen
import com.alessandra.entrenaria.user.ui.UserScreen
import com.google.firebase.auth.FirebaseAuth

@Composable
fun NavigationWrapper(auth: FirebaseAuth) {
    val navController = rememberNavController()

    // Estado que observa el login en tiempo real
    // Evita que el usuario pueda volver a una pantalla restringida una vez hecho logout
    val isUserLoggedIn = remember { mutableStateOf(auth.currentUser != null) }

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener {
            isUserLoggedIn.value = it.currentUser != null
        }
        auth.addAuthStateListener(listener)
        onDispose { auth.removeAuthStateListener(listener) }
    }

    NavHost(
        navController = navController,
        // La pantalla de inicio es Periods si ya hay un usuario autenticado
        // Si no, la pantalla de inicio es Initial
        startDestination = if (isUserLoggedIn.value) TrainingPeriods else Auth
    ) {

        // Pantallas
        composable<Auth> {
            val initialViewModel: AuthViewModel = viewModel()
            AuthScreen(
                navigateToHome = {
                    navController.navigate(TrainingPeriods) {
                        popUpTo(Auth) { inclusive = true }
                    }
                },
                viewModel = initialViewModel
            )
        }

        composable<User> {
            UserScreen(
                onNavigateToTrainings = { navController.navigate(TrainingPeriods) },
                onNavigateToUser = { navController.navigate(User) },
                onNavigateToChat = { navController.navigate(Chat) },
                onLogout = {
                    auth.signOut()
                    navController.navigate(Auth) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // La pantalla de training days sólo muestra los periodos del usuario logeado
        // Debe aceptar argumentos (userId)
        composable<TrainingPeriods> {
            val userId = auth.currentUser?.uid ?: return@composable

            TrainingPeriodsScreen(
                userId = userId,
                onNavigateToTrainingDays = { periodId ->
                    navController.navigate(TrainingDays(periodId)) },
                // Navegación del menú inferior
                onNavigateToTrainings = { navController.navigate(TrainingPeriods) },
                onNavigateToUser = { navController.navigate(User) },
                onNavigateToChat = {navController.navigate(Chat) }
            )
        }



        // La pantalla de training days sólo muestra los días del periodo seleccionado y usuario actual
        // Debe aceptar argumentos (periodId, userId)
        composable<TrainingDays> { backStackEntry ->
            val userId = auth.currentUser?.uid ?: return@composable
            val periodId = backStackEntry.arguments?.getString("periodId") ?: return@composable

            TrainingDaysScreen(
                userId = userId,
                periodId = periodId,
                onNavigateToExercises = { pId, dId -> navController.navigate(ExerciseList(pId, dId))},
                // Navegación del menú inferior
                onNavigateToTrainings = { navController.navigate(TrainingPeriods)},
                onNavigateToUser = { navController.navigate(User)},
                onNavigateToChat = { navController.navigate(Chat)}
            )
        }


        // La pantalla de ejercicios  sólo muestra los ejercicios del día seleccionado
        // Debe aceptar argumentos (dayId)
        composable<ExerciseList> { backStackEntry ->
            val userId = auth.currentUser?.uid ?: return@composable
            val periodId = backStackEntry.arguments?.getString("periodId") ?: return@composable
            val dayId = backStackEntry.arguments?.getString("dayId") ?: return@composable

            ExerciseListScreen(
                userId = userId,
                periodId = periodId,
                dayId = dayId,
                onNavigateToExerciseDetail = { exerciseId ->
                    navController.navigate(ExerciseDetail(exerciseId)) },
                onNavigateToNewExercise = { navController.navigate(NewExercise(periodId, dayId)) },
                // Navegación del menú inferior
                onNavigateToTrainings = { navController.navigate(TrainingPeriods) },
                onNavigateToUser = { navController.navigate(User) },
                onNavigateToChat = { navController.navigate(Chat) }
            )
        }


        composable<NewExercise> { backStackEntry ->
            val userId = auth.currentUser?.uid ?: return@composable
            val periodId = backStackEntry.arguments?.getString("periodId") ?: return@composable
            val dayId = backStackEntry.arguments?.getString("dayId") ?: return@composable
            val exerciseId = backStackEntry.arguments?.getString("exerciseId")

            NewExerciseScreen(
                userId = userId,
                periodId = periodId,
                dayId = dayId,
                exerciseId = exerciseId,
                onNavigateToTrainings = { navController.navigate(TrainingPeriods) },
                onNavigateToUser = { navController.navigate(User) },
                onNavigateToChat = { navController.navigate(Chat) },
                onBack = { navController.popBackStack() }
            )
        }


        composable<ExerciseDetail> { backStackEntry ->
            val userId = auth.currentUser?.uid ?: return@composable
            val exerciseId = backStackEntry.arguments?.getString("exerciseId") ?: return@composable

            ExerciseDetailScreen(
                userId = userId,
                exerciseId = exerciseId,
                // Navega a la pantalla de edición del ejercicio (reutiliza NewExerciseScreen)
                onNavigateToEditExercise = { periodId, dayId, exerciseId ->
                    navController.navigate(NewExercise(periodId, dayId, exerciseId))
                },
                // Navegación del menú inferior
                onNavigateToTrainings = { navController.navigate(TrainingPeriods) },
                onNavigateToUser = { navController.navigate(User) },
                onNavigateToChat = { navController.navigate(Chat) }
            )
        }


        composable<Chat> {
            val userId = auth.currentUser?.uid ?: return@composable

            ChatScreen(
                userId = userId,
                // Navegación del menú inferior
                onNavigateToTrainings = { navController.navigate(TrainingPeriods) },
                onNavigateToUser = { navController.navigate(User) },
                onNavigateToChat = { navController.navigate(Chat) }
            )
        }

    }
}
