package com.example.mobileapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.compose.AppTheme
import com.example.mobileapp.ui.screens.HistoryScreen
import com.example.mobileapp.ui.screens.HomeScreen
import com.example.mobileapp.ui.screens.OpenedServiceCardScreen
import com.example.mobileapp.ui.screens.SettingsScreen

object Routes {
    const val HOME = "home"
    const val SETTINGS = "settings"
    const val PROFILE = "profile"
    const val LOGIN = "login"
    const val REGISTRATION = "registration"
    const val ITEM = "item/{title}/{master}/{cost}"
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // Беру системную тему по умолчанию
            val systemTheme = isSystemInDarkTheme()

            // Создаю состояние темы приложения
            var isDarkTheme by rememberSaveable { mutableStateOf(systemTheme) }

            AppTheme(
                darkTheme = isDarkTheme
            ) {
                // Передаем состояние и функцию изменения темы вниз
                AppNavigation(
                    isDarkTheme = isDarkTheme,
                    onThemeChanged = { newThemeValue ->
                        isDarkTheme = newThemeValue
                    }
                )
            }
        }
    }
}

@Composable
fun AppNavigation(isDarkTheme: Boolean, onThemeChanged: (Boolean) -> Unit) {
    val navController = rememberNavController()

    // Scaffold создает структуру экрана с нижней панелью
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            BottomBar(navController)
        }
    ) { innerPadding ->
        // NavHost находится внутри Scaffold и учитывает отступы (innerPadding)
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(
                route = Routes.HOME,
                enterTransition = { fadeIn(animationSpec = tween(700)) },
                exitTransition = { fadeOut(animationSpec = tween(700)) }
            ) {
                HomeScreen(navController = navController)
            }

            composable(
                route = Routes.ITEM,
                enterTransition = { fadeIn(tween(700)) },
                exitTransition = { fadeOut(tween(700)) },
                arguments = listOf(
                    navArgument("title") { type = NavType.StringType },
                    navArgument("master") { type = NavType.StringType },
                    navArgument("cost") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val title = backStackEntry.arguments?.getString("title") ?: ""
                val master = backStackEntry.arguments?.getString("master") ?: ""
                // val cost = backStackEntry.arguments?.getInt("cost") ?: 0

                OpenedServiceCardScreen(
                    title = title,
                    master = master,
                    onBook = { _, _ -> },
                    onCancel = { navController.popBackStack() }
                )
            }

            composable(Routes.SETTINGS) {
                // Передаем текущую тему и callback для изменения
                SettingsScreen(
                    isDarkTheme = isDarkTheme,
                    onThemeChange = onThemeChanged
                )
            }

            composable(Routes.PROFILE) {
                HistoryScreen(navController = navController)
            }
        }
    }
}

@Composable
fun BottomBar(navController: NavController) {
    // Получаем текущий route, подписываясь на изменения
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Surface(
        tonalElevation = 4.dp,
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            BottomIconButton(
                icon = Icons.Default.Menu,
                contentDescription = "Меню",
                // Проверяем, является ли этот экран текущим
                isSelected = currentRoute == Routes.HOME,
                onClick = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.HOME) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )

            BottomIconButton(
                icon = Icons.Default.Person,
                contentDescription = "Профиль",
                isSelected = currentRoute == Routes.PROFILE,
                onClick = {
                    navController.navigate(Routes.PROFILE) {
                        launchSingleTop = true
                    }
                }
            )

            BottomIconButton(
                icon = Icons.Default.Settings,
                contentDescription = "Настройки",
                isSelected = currentRoute == Routes.SETTINGS,
                onClick = {
                    navController.navigate(Routes.SETTINGS) {
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}

@Composable
fun BottomIconButton(
    icon: ImageVector,
    contentDescription: String?,
    isSelected: Boolean, // <--- Новый параметр
    onClick: () -> Unit
) {
    // Если выбран - primary цвет, если нет - белый (или другой нейтральный)
    val tint = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        Color.White // Или MaterialTheme.colorScheme.onSurfaceVariant для лучшей поддержки тем
    }

    IconButton(onClick = onClick) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(28.dp)
        )
    }
}