package com.soorya.wealthmanager

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.*
import com.soorya.wealthmanager.ui.screens.addtransaction.AddTransactionScreen
import com.soorya.wealthmanager.ui.screens.dashboard.DashboardScreen
import com.soorya.wealthmanager.ui.screens.reports.ReportsScreen
import com.soorya.wealthmanager.ui.screens.settings.SettingsScreen
import com.soorya.wealthmanager.ui.screens.transactions.TransactionsScreen
import com.soorya.wealthmanager.ui.theme.*
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class WealthApp : Application()

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WealthManagerTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Pearl) {
                    WealthNav()
                }
            }
        }
    }
}

@Composable
fun WealthNav() {
    val nav = rememberNavController()

    NavHost(
        navController = nav,
        startDestination = "dashboard",
        enterTransition = { slideInHorizontally { it / 4 } + fadeIn() },
        exitTransition = { slideOutHorizontally { -it / 4 } + fadeOut() },
        popEnterTransition = { slideInHorizontally { -it / 4 } + fadeIn() },
        popExitTransition = { slideOutHorizontally { it / 4 } + fadeOut() }
    ) {
        composable("dashboard") {
            DashboardScreen(
                onAdd = { nav.navigate("add") },
                onViewAll = { nav.navigate("transactions") },
                onReports = { nav.navigate("reports") },
                onSettings = { nav.navigate("settings") }
            )
        }

        composable(
            "add",
            enterTransition = { slideInVertically { it } + fadeIn() },
            exitTransition = { slideOutVertically { it } + fadeOut() },
            popExitTransition = { slideOutVertically { it } + fadeOut() }
        ) {
            AddTransactionScreen(onDismiss = { nav.popBackStack() })
        }

        composable("transactions") {
            TransactionsScreen(onBack = { nav.popBackStack() })
        }

        composable("reports") {
            ReportsScreen(onBack = { nav.popBackStack() })
        }

        composable("settings") {
            SettingsScreen(onBack = { nav.popBackStack() })
        }
    }
}
