package com.example.counterapp.navigation

sealed class Routes(val route: String) {
    data object Home : Routes("home")
    data object CategoryDetail : Routes("category/{categoryId}") {
        fun createRoute(categoryId: Long) = "category/$categoryId"
    }
    data object CounterDetail : Routes("counter/{counterId}") {
        fun createRoute(counterId: Long) = "counter/$counterId"
    }
}
