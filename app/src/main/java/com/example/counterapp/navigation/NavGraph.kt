package com.example.counterapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.counterapp.ui.category.CategoryDetailScreen
import com.example.counterapp.ui.counter.CounterDetailScreen
import com.example.counterapp.ui.home.HomeScreen

@Composable
fun CounterAppNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Routes.Home.route
    ) {
        composable(Routes.Home.route) {
            HomeScreen(navController = navController)
        }
        composable(
            route = Routes.CategoryDetail.route,
            arguments = listOf(navArgument("categoryId") { type = NavType.LongType })
        ) {
            CategoryDetailScreen(navController = navController)
        }
        composable(
            route = Routes.CounterDetail.route,
            arguments = listOf(navArgument("counterId") { type = NavType.LongType })
        ) {
            CounterDetailScreen(navController = navController)
        }
    }
}
