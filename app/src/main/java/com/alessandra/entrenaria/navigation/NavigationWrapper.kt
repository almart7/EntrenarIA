package com.alessandra.entrenaria.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.alessandra.entrenaria.presentation.initial.InitialScreen
import com.alessandra.entrenaria.presentation.login.LoginScreen
import com.alessandra.entrenaria.presentation.signup.SignUpScreen
import com.google.firebase.auth.FirebaseAuth

@Composable
fun NavigationWrapper( auth: FirebaseAuth) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Initial ) {

        composable<Initial> {
            InitialScreen(
                navigateToLogin = { navController.navigate(Login) },
                navigateToSignUp = { navController.navigate(SignUp) }
            )
        }

        composable<Login> {
            LoginScreen(auth){navController.popBackStack()}
        }
        composable<SignUp> {
            SignUpScreen(auth){navController.popBackStack()}
        }
    }
}