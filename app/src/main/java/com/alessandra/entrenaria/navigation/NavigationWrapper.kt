package com.alessandra.entrenaria.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.alessandra.entrenaria.ui.screens.initial.InitialScreen
import com.alessandra.entrenaria.ui.screens.login.LoginScreen
import com.alessandra.entrenaria.ui.screens.profile.EditProfileScreen
import com.alessandra.entrenaria.ui.screens.profile.ProfileScreen
import com.alessandra.entrenaria.ui.screens.signup.SignUpScreen
import com.google.firebase.auth.FirebaseAuth

@Composable
fun NavigationWrapper(auth: FirebaseAuth, isUserLoggedIn: Boolean) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = if (isUserLoggedIn) Profile else Initial
    ) {
        composable<Initial> {
            InitialScreen(
                navigateToLogin = { navController.navigate(Login) },
                navigateToSignUp = { navController.navigate(SignUp) },
                navigateToProfile = { navController.navigate(Profile) }
            )
        }

        composable<Login> {
            LoginScreen(
                auth = auth,
                navigateBack = { navController.popBackStack() },
                navigateToProfile = { navController.navigate(Profile) }
            )
        }

        composable<SignUp> {
            SignUpScreen(
                auth = auth,
                navigateBack = { navController.popBackStack() },
                navigateToProfile = { navController.navigate(Profile) }
            )
        }

        composable<Profile> {
            ProfileScreen(
                onLogout = {
                    navController.navigate(Initial) {
                        popUpTo(Profile) { inclusive = true }
                    }
                },
                onEditProfile = {
                    navController.navigate(EditProfile)
                }
            )
        }

        composable<EditProfile> {
            EditProfileScreen(
                onProfileUpdated = {
                    navController.popBackStack()
                },
                onCancel = {
                    navController.popBackStack()
                },
                onLogout = {
                    navController.navigate(Initial) {
                        popUpTo(Profile) { inclusive = true }
                    }
                }
            )
        }
    }
}
