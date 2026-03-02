package com.travelguide

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.travelguide.navigation.AppNavHost
import com.travelguide.theme.AppTheme

@Composable
fun TravelGuideApp(container: AppContainer) {
    AppTheme {
        Surface(
            modifier = androidx.compose.ui.Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            val navController = rememberNavController()

            val start = if (container.authRepository.isLoggedIn()) "cityList" else "login"

            AppNavHost(navController, start, container)
        }
    }
}