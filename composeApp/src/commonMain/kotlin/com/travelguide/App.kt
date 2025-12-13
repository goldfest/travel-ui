// composeApp/src/commonMain/kotlin/com/travelguide/App.kt (если KMP)
// ИЛИ composeApp/src/androidMain/kotlin/com/travelguide/App.kt (если только Android)
package com.travelguide

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.travelguide.navigation.AppNavHost
import com.travelguide.theme.AppTheme

@Composable
fun TravelGuideApp() {
    AppTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            val navController = rememberNavController()
            AppNavHost(navController = navController)
        }
    }
}