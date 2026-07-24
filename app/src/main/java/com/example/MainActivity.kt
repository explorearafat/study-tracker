package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.notifications.FocusShieldService
import com.example.notifications.NotificationHelper
import com.example.ui.MainViewModel
import com.example.ui.components.FocusBlockAlertDialog
import com.example.ui.screens.*
import com.example.ui.theme.StudyTrackerTheme
import com.example.util.FocusShieldManager
import kotlinx.coroutines.flow.MutableStateFlow

enum class Screen(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    Dashboard("dashboard", "Overview", Icons.Filled.GridView, Icons.Outlined.GridView),
    Timer("timer", "Timer", Icons.Filled.Timer, Icons.Outlined.Timer),
    Subjects("subjects", "Subjects", Icons.AutoMirrored.Filled.MenuBook, Icons.AutoMirrored.Outlined.MenuBook),
    Tasks("tasks", "Tasks", Icons.Filled.TaskAlt, Icons.Outlined.TaskAlt),
    Analytics("analytics", "Analytics", Icons.Filled.BarChart, Icons.Outlined.BarChart),
    Profile("profile", "Profile", Icons.Filled.Person, Icons.Outlined.Person)
}

class MainActivity : ComponentActivity() {

    private val blockedAppNameFlow = MutableStateFlow<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleFocusBlockIntent(intent)

        setContent {
            val blockedAppName by blockedAppNameFlow.collectAsStateWithLifecycle()
            StudyTrackerApp(
                blockedAppName = blockedAppName,
                onDismissBlockedAlert = { blockedAppNameFlow.value = null }
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleFocusBlockIntent(intent)
    }

    private fun handleFocusBlockIntent(intent: Intent?) {
        if (intent?.getBooleanExtra("SHOW_FOCUS_BLOCK_ALERT", false) == true) {
            val appName = intent.getStringExtra("BLOCKED_APP_NAME") ?: "Social Media App"
            blockedAppNameFlow.value = appName
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyTrackerApp(
    viewModel: MainViewModel = viewModel(),
    blockedAppName: String? = null,
    onDismissBlockedAlert: () -> Unit = {}
) {
    val context = LocalContext.current
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val isDarkMode = userProfile?.isDarkMode ?: false
    val timerUiState by viewModel.timerUiState.collectAsStateWithLifecycle()
    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val selectedSubject = subjects.find { it.id == timerUiState.selectedSubjectId }

    LaunchedEffect(Unit) {
        NotificationHelper.createNotificationChannel(context)
    }

    LaunchedEffect(timerUiState.isRunning) {
        if (timerUiState.isRunning) {
            if (FocusShieldManager.isShieldEnabled(context)) {
                FocusShieldService.startService(
                    context = context,
                    subjectName = selectedSubject?.name ?: "Focus Session",
                    remainingSeconds = timerUiState.remainingSeconds
                )
            }
        } else {
            if (FocusShieldService.isRunning) {
                FocusShieldService.stopService(context)
            }
        }
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { }
    )

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    StudyTrackerTheme(darkTheme = isDarkMode) {
        if (blockedAppName != null) {
            FocusBlockAlertDialog(
                blockedAppName = blockedAppName,
                remainingSeconds = timerUiState.remainingSeconds,
                onDismiss = onDismissBlockedAlert,
                onEmergencyPause = {
                    viewModel.toggleTimer()
                    onDismissBlockedAlert()
                }
            )
        }

        val showOnboarding = userProfile != null && !userProfile!!.isOnboardingCompleted

        if (showOnboarding) {
            val subjects by viewModel.subjects.collectAsStateWithLifecycle()
            OnboardingScreen(
                userProfile = userProfile,
                subjects = subjects,
                onCompleteOnboarding = { name, academicLevel, motto, targetDailyHours, avatarUri, initialTasks, reminderEnabled, reminderHour, reminderMinute ->
                    viewModel.completeOnboarding(
                        name, academicLevel, motto, targetDailyHours, avatarUri, initialTasks, reminderEnabled, reminderHour, reminderMinute
                    )
                }
            )
        } else {
            val navController = rememberNavController()
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Dashboard.route

            Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        val currentScreen = remember(currentRoute) {
                            Screen.entries.find { it.route == currentRoute }
                        }
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
                    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
                    val sessions by viewModel.sessions.collectAsStateWithLifecycle()
                    val quoteUiState by viewModel.quoteUiState.collectAsStateWithLifecycle()

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
                    val timerUiState by viewModel.timerUiState.collectAsStateWithLifecycle()
                    val subjects by viewModel.subjects.collectAsStateWithLifecycle()

                    PomodoroTimerScreen(
                        timerState = timerUiState,
                        subjects = subjects,
                        onToggleTimer = { viewModel.toggleTimer() },
                        onResetTimer = { viewModel.resetTimer() },
                        onSetMode = { mode -> viewModel.setTimerMode(mode) },
                        onSelectSubject = { subId -> viewModel.selectTimerSubject(subId) },
                        onAddMinutes = { mins -> viewModel.addTimerMinutes(mins) }
                    )
                }

                composable(Screen.Subjects.route) {
                    val subjects by viewModel.subjects.collectAsStateWithLifecycle()

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
                    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
                    val subjects by viewModel.subjects.collectAsStateWithLifecycle()

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
                    val sessions by viewModel.sessions.collectAsStateWithLifecycle()
                    val subjects by viewModel.subjects.collectAsStateWithLifecycle()

                    AnalyticsScreen(
                        sessions = sessions,
                        subjects = subjects,
                        userProfile = userProfile
                    )
                }

                composable(Screen.Profile.route) {
                    val sessions by viewModel.sessions.collectAsStateWithLifecycle()
                    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
                    val subjects by viewModel.subjects.collectAsStateWithLifecycle()

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
                        onUpdateReminderSettings = { enabled, hour, minute ->
                            viewModel.updateReminderSettings(enabled, hour, minute)
                        },
                        onResetOnboarding = {
                            viewModel.resetOnboarding()
                        },
                        onToggleDarkMode = { viewModel.toggleDarkMode() }
                    )
                }
            }
        }
    }
    }
}
