package com.travelguide

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.travelguide.navigation.AppNavHost
import com.travelguide.session.SessionEvent
import com.travelguide.theme.AppTheme

@Composable
fun TravelGuideApp(container: AppContainer) {
    AppTheme {
        val navController = rememberNavController()
        val snackbarHostState = remember { SnackbarHostState() }

        // единый обработчик событий сессии
        LaunchedEffect(Unit) {
            container.sessionManager.events.collect { ev ->
                when (ev) {
                    is SessionEvent.Unauthorized -> {
                        snackbarHostState.showSnackbar(ev.message)
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true } // чистим весь стек
                        }
                    }
                }
            }
        }

        val start = "boot"

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { padding ->
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                Box(Modifier.padding(padding)) {
                    AppNavHost(navController, "boot", container)
                }
            }
        }
    }
}