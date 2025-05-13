package com.alessandra.entrenaria.navigation

import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.alessandra.entrenaria.ui.screens.chat.ChatScreen
import com.alessandra.entrenaria.ui.screens.exercises.ExerciseDetailScreen
import com.alessandra.entrenaria.ui.screens.exercises.ExerciseListScreen
import com.alessandra.entrenaria.ui.screens.exercises.NewExerciseScreen
import com.alessandra.entrenaria.ui.screens.home.HomeScreen
import com.alessandra.entrenaria.ui.screens.initial.InitialScreen
import com.alessandra.entrenaria.ui.screens.login.LoginScreen
import com.alessandra.entrenaria.ui.screens.profile.ProfileScreen
import com.alessandra.entrenaria.ui.screens.signup.SignUpScreen
import com.alessandra.entrenaria.ui.screens.trainingDays.TrainingDaysScreen
import com.google.firebase.auth.FirebaseAuth

@Composable
fun NavigationWrapper(auth: FirebaseAuth) {
    val navController = rememberNavController()

    // Estado que observa el login en tiempo real
    // Evita que el usuario pueda volver atrás  una vez hecho logout
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
        startDestination = if (isUserLoggedIn.value) Home else Initial
    ) {
        composable<Initial> {
            InitialScreen(
                navigateToLogin = { navController.navigate(Login) },
                navigateToSignUp = { navController.navigate(SignUp) },
                naviageToHome = {
                    navController.navigate(Home) {
                        popUpTo(Initial) { inclusive = true }
                    }
                }
            )
        }

        composable<Login> {
            LoginScreen(
                auth = auth,
                navigateBack = { navController.popBackStack() },
                navigateToHome = {
                    navController.navigate(Home) {
                        popUpTo(Login) { inclusive = true }
                    }
                }
            )
        }

        composable<SignUp> {
            SignUpScreen(
                auth = auth,
                navigateBack = { navController.popBackStack() },
                navigateToHome = {
                    navController.navigate(Home) {
                        popUpTo(SignUp) { inclusive = true }
                    }
                }
            )
        }

        composable<Profile> {
            ProfileScreen(
                navController = navController,
                onLogout = {
                    auth.signOut()
                    navController.navigate(Initial) {
                        popUpTo(0) { inclusive = true } // 🔥 limpia toda la pila de navegación
                    }
                }
            )
        }

        composable<Home> {
            HomeScreen(
                userId = auth.currentUser?.uid ?: "",
                navController = navController,
                onTrainingPeriodClick = { periodId ->
                    navController.navigate(TrainingDays(periodId))
                }
            )
        }

        composable<TrainingDays> { backStackEntry ->
            val periodId = backStackEntry.arguments?.getString("periodId") ?: return@composable
            val userId = auth.currentUser?.uid ?: return@composable
            TrainingDaysScreen(
                userId = userId,
                periodId = periodId,
                navController = navController
            )
        }

        composable<ExerciseList> { backStackEntry ->
            val userId = auth.currentUser?.uid ?: return@composable
            val periodId = backStackEntry.arguments?.getString("periodId") ?: return@composable
            val dayId = backStackEntry.arguments?.getString("dayId") ?: return@composable

            ExerciseListScreen(
                userId = userId,
                periodId = periodId,
                dayId = dayId,
                navController = navController
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
                navController = navController
            )
        }

        composable<ExerciseDetail> { backStackEntry ->
            val userId = auth.currentUser?.uid ?: return@composable
            val exerciseId = backStackEntry.arguments?.getString("exerciseId") ?: return@composable

            ExerciseDetailScreen(
                userId = userId,
                exerciseId = exerciseId,
                navController = navController
            )
        }

        composable<Chat> {
            val userId = auth.currentUser?.uid ?: return@composable
            ChatScreen(
                userId = userId,
                navController = navController
            )
        }
    }
}
