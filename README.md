# 🎯 StudyTracker - Native Android Productivity & Focus App

<p align="center">
  <img src="screenshots/banner.jpg" alt="StudyTracker Banner" width="100%" />
</p>

<p align="center">
  <a href="https://kotlinlang.org/"><img src="https://img.shields.io/badge/Language-Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin" /></a>
  <a href="https://developer.android.com/jetpack/compose"><img src="https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?style=for-the-badge&logo=android&logoColor=white" alt="Jetpack Compose" /></a>
  <a href="https://developer.android.com/training/data-storage/room"><img src="https://img.shields.io/badge/Database-Room-3DDC84?style=for-the-badge&logo=sqlite&logoColor=white" alt="Room DB" /></a>
  <a href="https://m3.material.io/"><img src="https://img.shields.io/badge/Design-Material%203-FFB800?style=for-the-badge&logo=materialdesign&logoColor=white" alt="Material 3" /></a>
  <a href="https://android.com"><img src="https://img.shields.io/badge/MinSDK-API%2024%20(Android%207.0)-34A853?style=for-the-badge&logo=android" alt="Min SDK 24" /></a>
</p>

---

## 📌 Overview

**StudyTracker** is a modern, feature-packed native Android productivity application built with **Kotlin** and **Jetpack Compose**. It is engineered to help students, developers, and self-learners organize study subjects, manage tasks, track deep work sessions, maintain daily study goals, and visualize focus analytics.

Equipped with dynamic subject-themed Pomodoro timers, local push notifications for daily goals, local data persistence via Room Database, and customizable study reminders, StudyTracker ensures a distraction-free, privacy-focused experience.

---

## ✨ Core Features

### ⏱️ Dynamic Subject-Themed Pomodoro Timer
* **Subject Color Integration**: The circular progress ring dynamically transforms its sweep gradient and glow accents to match the exact color theme of the currently selected subject.
* **Flexible Focus Modes**: Switch instantly between **Work (25m)**, **Short Break (5m)**, and **Long Break (15m)** sessions.
* **Quick Controls**: Add +5 minutes with a single tap, toggle pause/resume, or reset session time smoothly.
* **Haptic Completion Alerts**: Tactile haptic feedback notifies you when a focus session completes.

### 🔔 Daily Study Goal Push Notifications
* **Target Daily Minutes**: Assign target study goals (in minutes) for each subject.
* **Real-Time Threshold Monitoring**: As you log study sessions, the system tracks total daily minutes per subject and automatically triggers a local push notification when a daily goal is achieved.

### 📚 Subject & Task Management
* **Custom Subjects**: Create subjects with custom icon selections, color codes, and target daily study goals.
* **Integrated Task List**: Organize subject tasks with priority levels (High, Medium, Low) and quick completion toggles.

### 📊 Focus Analytics & Session Tracking
* **Detailed Session Logs**: Automatically records every completed study session (type, duration, subject, timestamp, notes).
* **Visual Charts**: Interactive weekly study distribution charts and subject breakdown stats to analyze productivity trends over time.

### ⏰ Daily Study Reminders
* **Customizable Alarm Schedule**: Set custom reminder times (e.g., 08:00 PM daily) using Android's `AlarmManager` and `BroadcastReceiver`.

### 🎨 Material Design 3 & Edge-to-Edge Experience
* **Adaptive Dark & Light Themes**: Dynamic color support with full system edge-to-edge layout integration.
* **100% Offline & Private**: All data is stored locally on the device using Room Database. No cloud account or internet connection required.

---

## 📱 App Screenshots

| Dynamic Focus Timer | Subject Management | Analytics & Progress |
| :---: | :---: | :---: |
| <img src="screenshots/banner.jpg" width="280" alt="Focus Timer"/> | <img src="screenshots/banner.jpg" width="280" alt="Subjects"/> | <img src="screenshots/banner.jpg" width="280" alt="Analytics"/> |

> *Tip: Place your device screenshots inside the `screenshots/` directory to display them in this table.*

---

## 🛠️ Tech Stack & Architecture

StudyTracker follows Android's recommended **Clean Architecture + MVVM** design pattern:

* **Language**: [Kotlin](https://kotlinlang.org/) (100%)
* **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) with Material 3 Design System
* **Architecture**: ViewModel + Coroutines + Kotlin Flow (`StateFlow`, `collectAsStateWithLifecycle`)
* **Local Database**: [Room Database](https://developer.android.com/training/data-storage/room) with KSP
* **Navigation**: Jetpack Navigation Compose with type-safe state routing
* **Notifications**: Android `NotificationManager`, `NotificationChannel`, `PendingIntent`, and `BroadcastReceiver`
* **Dependency Injection**: Constructor Injection / Android ViewModel Provider
* **Build System**: Gradle Kotlin DSL (`build.gradle.kts`)

---

## 📁 Project Directory Structure

```text
com.example/
├── MainActivity.kt                  # App Entry point & NavHost configuration
├── data/                            # Data Layer
│   ├── AppDatabase.kt               # Room database configuration
│   ├── dao/                         # Data Access Objects (TaskDao, SubjectDao, StudySessionDao)
│   ├── model/                       # Entity classes (Subject, Task, StudySession, UserProfile)
│   └── repository/                  # StudyRepository handling database operations
├── notifications/                   # Local Notification Layer
│   ├── NotificationHelper.kt        # Notification channels & goal alert builder
│   └── StudyReminderReceiver.kt     # BroadcastReceiver for scheduled reminders
├── ui/                              # Presentation Layer
│   ├── MainViewModel.kt             # Shared ViewModel managing state & coroutine flows
│   ├── components/                  # Reusable Compose components (SubjectIcon, CustomCards, Charts)
│   ├── screens/                     # UI Screen Composables
│   │   ├── HomeScreen.kt            # Dashboard overview
│   │   ├── PomodoroTimerScreen.kt   # Dynamic Pomodoro timer screen
│   │   ├── SubjectsScreen.kt        # Subject list & management
│   │   ├── TasksScreen.kt           # Task manager & priority filters
│   │   ├── AnalyticsScreen.kt       # Study stats & charts
│   │   └── ProfileScreen.kt         # Profile settings & reminder configuration
│   └── theme/                       # Color, Typography, Shape, and Material 3 Theme definitions
└── util/                            # Helper utilities & extensions
```

---

## 🚀 Getting Started

### Prerequisites

* **Android Studio**: Ladybug (2024.2.1) or newer recommended
* **JDK Version**: Java 17 or higher
* **Android SDK**: Minimum API 24 (Android 7.0), Target API 36 (Android 15+)

### Steps to Run

1. **Clone the repository**:
   ```bash
   git clone https://github.com/explorearafat/study-tracker.git
   cd study-tracker
   ```

2. **Open in Android Studio**:
   - Open Android Studio, select **Open**, and browse to the cloned folder.
   - Wait for Gradle sync to complete automatically.

3. **Build & Run**:
   - Connect an Android device via USB (with USB Debugging enabled) or start an Android Emulator.
   - Press **Run** (`Shift + F10`) or run from terminal:
     ```bash
     ./gradlew assembleDebug
     ```

---

## 🔐 Permissions Used

* `android.permission.POST_NOTIFICATIONS`: Required on Android 13+ (API 33+) to send local goal alerts and daily study reminders.
* `android.permission.SCHEDULE_EXACT_ALARM` / `RECEIVE_BOOT_COMPLETED`: Required to reschedule daily study alarms across device reboots.

---

## 📄 License

```text
Copyright (2026) StudyTracker Project

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
