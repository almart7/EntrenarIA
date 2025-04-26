package com.alessandra.entrenaria

import androidx.compose.runtime.Composable
import androidx.navigation.compose.composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.alessandra.entrenaria.presentation.initial.InitialScreen
import com.alessandra.entrenaria.presentation.login.LoginScreen
import com.alessandra.entrenaria.presentation.signup.SignUpScreen
import com.google.firebase.auth.FirebaseAuth

@Composable
fun NavigationWrapper(navHostController: NavHostController, auth: FirebaseAuth) {
    NavHost(navController = navHostController, startDestination = "initial" ) {
        composable("initial") {
            InitialScreen(
                navigateToLogin = { navHostController.navigate("logIn") },
                navigateToSignUp = { navHostController.navigate("signUp") }
            )
        }

        composable("logIn") {
            LoginScreen(auth)
        }
        composable("signUp") {
            SignUpScreen(auth)
        }
    }
}