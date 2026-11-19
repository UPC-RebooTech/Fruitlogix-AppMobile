package com.rebootech.fruitlogix.auth

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.rebootech.fruitlogix.auth.presentation.login.LoginScreen
import com.rebootech.fruitlogix.auth.presentation.signup.SignUpScreen

fun NavGraphBuilder.authGraph(navController: NavHostController) {
    composable(AuthRoutes.Login) {
        LoginScreen(
            onNavigateToSignUp = {
                navController.navigate(AuthRoutes.SignUp)
            }
        )
    }
    composable(AuthRoutes.SignUp) {
        SignUpScreen(
            onNavigateBackToLogin = {
                navController.popBackStack()
            }
        )
    }
}

