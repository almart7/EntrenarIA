package com.alessandra.entrenaria.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
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
fun NavigationWrapper(auth: FirebaseAuth, isUserLoggedIn: Boolean) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = if (isUserLoggedIn) Home else Initial
    ) {
        composable<Initial> {
            InitialScreen(
                navigateToLogin = { navController.navigate(Login) },
                navigateToSignUp = { navController.navigate(SignUp) },
                naviageToHome = { navController.navigate(Home) }
            )
        }

        composable<Login> {
            LoginScreen(
                auth = auth,
                navigateBack = { navController.popBackStack() },
                navigateToHome = { navController.navigate(Home) }
            )
        }

        composable<SignUp> {
            SignUpScreen(
                auth = auth,
                navigateBack = { navController.popBackStack() },
                navigateToHome = { navController.navigate(Home) }
            )
        }

        composable<Profile> {
            ProfileScreen(
                navController = navController,
                onLogout = {
                    navController.navigate(Initial) {
                        popUpTo(Profile) { inclusive = true }
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

            NewExerciseScreen(
                userId = userId,
                periodId = periodId,
                dayId = dayId,
                navController = navController
            )
        }

    }
}
