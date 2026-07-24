package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

import com.example.data.Subject
import com.example.ui.MainViewModel
import com.example.ui.screens.*

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MaterialTheme {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }
}

enum class NavTab(val title: String, val icon: ImageVector) {
    DASHBOARD("Dashboard", Icons.Default.BarChart),
    SUBJECTS("Subjects", Icons.Default.Palette),
    TASKS("Tasks", Icons.Default.Checklist),
    FOCUS("Focus", Icons.Default.Timer),
    PROFILE("Profile", Icons.Default.Person)
}

@Composable
fun MainAppScreen(viewModel: MainViewModel) {
    var currentTab by remember { mutableStateOf(NavTab.DASHBOARD) }
    var selectedTimerSubject by remember { mutableStateOf<Subject?>(null) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                NavTab.values().forEach { tab ->
                    NavigationBarItem(
                        selected = currentTab == tab,
                        onClick = { currentTab = tab },
                        icon = { Icon(tab.icon, contentDescription = tab.title) },
                        label = { Text(tab.title) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                NavTab.DASHBOARD -> DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToTimer = { sub ->
                        selectedTimerSubject = sub
                        currentTab = NavTab.FOCUS
                    }
                )

                NavTab.SUBJECTS -> SubjectsScreen(
                    viewModel = viewModel,
                    onNavigateToTimer = { sub ->
                        selectedTimerSubject = sub
                        currentTab = NavTab.FOCUS
                    }
                )

                NavTab.TASKS -> TasksScreen(viewModel = viewModel)

                NavTab.FOCUS -> PomodoroTimerScreen(
                    viewModel = viewModel,
                    initialSubject = selectedTimerSubject
                )

                NavTab.PROFILE -> ProfileScreen(viewModel = viewModel)
            }
        }
    }
}
