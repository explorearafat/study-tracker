package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.MainViewModel
import com.example.ui.screens.*
import com.example.ui.theme.StudyTrackerTheme

enum class Screen(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    Dashboard("dashboard", "Overview", Icons.Filled.GridView, Icons.Outlined.GridView),
    Timer("timer", "Timer", Icons.Filled.Timer, Icons.Outlined.Timer),
    Subjects("subjects", "Subjects", Icons.Filled.MenuBook, Icons.Outlined.MenuBook),
    Tasks("tasks", "Tasks", Icons.Filled.TaskAlt, Icons.Outlined.TaskAlt),
    Analytics("analytics", "Analytics", Icons.Filled.BarChart, Icons.Outlined.BarChart),
    Profile("profile", "Profile", Icons.Filled.Person, Icons.Outlined.Person)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            StudyTrackerApp()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyTrackerApp(viewModel: MainViewModel = viewModel()) {
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val sessions by viewModel.sessions.collectAsStateWithLifecycle()
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val timerUiState by viewModel.timerUiState.collectAsStateWithLifecycle()
    val quoteUiState by viewModel.quoteUiState.collectAsStateWithLifecycle()

    val isDarkMode = userProfile?.isDarkMode ?: false

    StudyTrackerTheme(darkTheme = isDarkMode) {
        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Dashboard.route

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        val currentScreen = Screen.entries.find { it.route == currentRoute }
                        Text(
                            text = currentScreen?.title ?: "Study Tracker",
                            fontWeight = FontWeight.ExtraBold,
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    actions = {
                        IconButton(onClick = { viewModel.toggleDarkMode() }) {
                            Icon(
                                imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = "Toggle Dark Mode",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    windowInsets = WindowInsets.navigationBars
                ) {
                    Screen.entries.forEach { screen ->
                        val selected = currentRoute == screen.route
                        val iconScale by animateFloatAsState(
                            targetValue = if (selected) 1.15f else 1.0f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            ),
                            label = "NavIconScale"
                        )

                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                if (currentRoute != screen.route) {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (selected) screen.selectedIcon else screen.unselectedIcon,
                                    contentDescription = screen.title,
                                    modifier = Modifier.scale(iconScale)
                                )
                            },
                            label = {
                                Text(
                                    text = screen.title,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            modifier = Modifier.testTag("nav_${screen.route}")
                        )
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Dashboard.route,
                modifier = Modifier.padding(innerPadding),
                enterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { 180 },
                        animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing)
                    ) + fadeIn(animationSpec = tween(240))
                },
                exitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { -180 },
                        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing)
                    ) + fadeOut(animationSpec = tween(200))
                },
                popEnterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { -180 },
                        animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing)
                    ) + fadeIn(animationSpec = tween(240))
                },
                popExitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { 180 },
                        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing)
                    ) + fadeOut(animationSpec = tween(200))
                }
            ) {
                composable(Screen.Dashboard.route) {
                    DashboardScreen(
                        userProfile = userProfile,
                        subjects = subjects,
                        sessions = sessions,
                        quoteUiState = quoteUiState,
                        onRefreshQuote = { topic -> viewModel.fetchMotivationalQuote(topic) },
                        onNavigateToTimer = {
                            navController.navigate(Screen.Timer.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        onNavigateToSubjects = {
                            navController.navigate(Screen.Subjects.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        onNavigateToTasks = {
                            navController.navigate(Screen.Tasks.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }

                composable(Screen.Timer.route) {
                    PomodoroTimerScreen(
                        timerState = timerUiState,
                        subjects = subjects,
                        onToggleTimer = { viewModel.toggleTimer() },
                        onResetTimer = { viewModel.resetTimer() },
                        onSetMode = { mode -> viewModel.setTimerMode(mode) },
                        onSelectSubject = { subId -> viewModel.selectTimerSubject(subId) }
                    )
                }

                composable(Screen.Subjects.route) {
                    SubjectsScreen(
                        subjects = subjects,
                        onAddSubject = { name, cat, colorHex, targetMins, iconName ->
                            viewModel.addSubject(name, cat, colorHex, targetMins, iconName)
                        },
                        onUpdateSubject = { sub -> viewModel.updateSubject(sub) },
                        onDeleteSubject = { sub -> viewModel.deleteSubject(sub) },
                        onLogManualTime = { subId, mins, notes ->
                            viewModel.logManualSession(subId, mins, notes)
                        }
                    )
                }

                composable(Screen.Tasks.route) {
                    TasksScreen(
                        tasks = tasks,
                        subjects = subjects,
                        onAddTask = { title, desc, subId, priority ->
                            viewModel.addTask(title, desc, subId, priority, null)
                        },
                        onToggleTaskCompleted = { task -> viewModel.toggleTaskCompleted(task) },
                        onDeleteTask = { task -> viewModel.deleteTask(task) }
                    )
                }

                composable(Screen.Analytics.route) {
                    AnalyticsScreen(
                        sessions = sessions,
                        subjects = subjects
                    )
                }

                composable(Screen.Profile.route) {
                    ProfileScreen(
                        userProfile = userProfile,
                        sessions = sessions,
                        tasks = tasks,
                        subjects = subjects,
                        onUpdateProfile = { name, level, motto, targetHours, avatarUri, wMins, bMins, lbMins ->
                            viewModel.updateProfile(
                                name, level, motto, targetHours, avatarUri, wMins, bMins, lbMins
                            )
                        },
                        onToggleDarkMode = { viewModel.toggleDarkMode() }
                    )
                }
            }
        }
    }
}
