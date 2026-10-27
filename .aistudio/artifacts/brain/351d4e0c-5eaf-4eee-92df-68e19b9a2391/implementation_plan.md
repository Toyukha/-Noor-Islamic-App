# Daily Obligations Room Architecture, ViewModel & Multi-Tier Notification System

Comprehensive architectural blueprint to implement an offline-first Room database schema and DAO for daily Islamic obligations (5 daily prayers, voluntary Sunnah, fasting/Sawm types, and Quran completion), an `ObligationsViewModel` with reactive state flows, and a dual-tier notification engine (system phone alarms with 10-minute prayer pre-warnings, Suhoor/Iftar alerts, custom Quran recitation scheduling, and an animated in-app top action banner).

---

## User Review & Critical Decisions

> [!IMPORTANT]
> The following product decisions were confirmed during the Phase 1 consultation:

- **Confirmed Decision 1 (Phone Notifications)**: Prayer alarms will alert 10 minutes prior to each Solah, alongside dedicated Suhoor (pre-dawn) and Iftar (sunset) reminders for fasting days.
- **Confirmed Decision 2 (Quran Recitation Reminder)**: Users can configure their own custom daily scheduled recitation time with an intuitive in-app time picker.
- **Confirmed Decision 3 (In-App Reminder Experience)**: An animated top banner slides into view when an obligation or prayer window is active, featuring one-tap quick-action buttons (e.g. "Mark Prayed", "Log Quran", "Dismiss").

---

## 1. Overview & Core Concept

- **What It Does**: Provides a rock-solid, offline-first Room database layer and stateful `ObligationsViewModel` for tracking daily prayers, Sunnah rak'ahs, fasting, and Quran recitation. Schedules accurate Android system notifications via `AlarmManager` and `NotificationManager` (10 minutes before prayer times, Suhoor, Iftar, and daily Quran recitation), paired with a dynamic in-app top banner for instant interaction.
- **Target Audience**: Practicing Muslims seeking a seamless, distraction-free companion that works 100% offline while maintaining cross-device cloud sync and reliable prayer reminders.
- **Key Value**: Guarantees zero missed prayers or lost logs even when completely offline or during phone restarts, with instant in-app actions to mark obligations complete.

---

## 2. User Experience & Visual Design

- **Key User Flows**:
  1. **Daily Tracking & Instant Feedback**: On the Tracker tab, checking off a prayer immediately writes to Room SQLite, updates the `ObligationsViewModel` UI state, checks streak milestones, and pushes to Firestore in the background.
  2. **In-App Top Alert Banner**: When within 10 minutes of a prayer or at custom Quran time, an animated top banner smoothly slides down over the screen with a glowing emerald border, displaying the prayer name, countdown, and instant action button `Mark Prayed`.
  3. **Phone Notification System**: System status bar notifications are dispatched via high-importance channels with sound and vibration, providing action buttons `Mark as Prayed` directly from the notification shade.
  4. **Custom Quran & Fasting Scheduling**: In Settings or Today tab, users easily pick their preferred daily Quran reminder time (e.g. 20:30) and toggle fasting reminder notifications.
- **Visual Identity & Design System**:
  - *Palette*: Islamic Emerald (`#0F5132`), Radiant Gold (`#D4AF37`), Deep Night (`#081C15`), Soft Cream surface (`#F8F9FA`).
  - *Typography*: Clean M3 typography scale with bold numeric prayer counters and elegant Arabic script formatting.
  - *Motion & Feedback*: Jetpack Compose `AnimatedVisibility` with spring physics for the top banner and tactile haptic feedback on checkbox toggles.

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1: Dual-Tier Notification Engine (In-App + Phone OS)**
  - *Chosen Approach*: Android `AlarmManager` exact alarms for OS notifications combined with a reactive in-app banner state in `ObligationsViewModel`.
  - *Why*: Phone OS notifications catch the user when the screen is off or app is closed; the in-app banner provides a non-intrusive, interactive experience while actively using the app.
  - *Alternatives Considered*: Standard snackbars (insufficient space for prayer countdown and quick action buttons) or intrusive modal dialogs (interrupts reading).
- **Decision 2: Room SQLite + Firestore Hybrid Sync**
  - *Chosen Approach*: Write to Room SQLite immediately for 0ms UI latency, with background Firestore synchronization.
  - *Why*: Ensures 100% offline availability in areas with weak connectivity (mosques, travel) without ever failing or dropping records.

---

## 4. Technical Architecture & Data Strategy

```
┌────────────────────────────────────────────────────────────────────────┐
│                        Jetpack Compose UI Layer                        │
│                                                                        │
│   ┌───────────────────────────┐      ┌─────────────────────────────┐   │
│   │ AnimatedInAppAlertBanner  │      │  TrackerScreen & TodayView  │   │
│   └─────────────▲─────────────┘      └──────────────▲──────────────┘   │
└─────────────────┼───────────────────────────────────┼──────────────────┘
                  │                                   │
┌─────────────────┴───────────────────────────────────┴──────────────────┐
│                   ObligationsViewModel (StateFlow)                     │
│  - uiState: StateFlow<ObligationsUiState>                              │
│  - activeBanner: StateFlow<InAppAlert?>                                │
│  - togglePrayer(), updateQuranPages(), setCustomQuranReminderTime()    │
└─────────────────▲───────────────────────────────────▲──────────────────┘
                  │                                   │
┌─────────────────┴────────────────────────┐   ┌──────┴──────────────────┐
│             NoorRepository               │   │ NotificationScheduler   │
│                                          │   │  (AlarmManager + Broadcast│
│  ┌─────────────────┐ ┌─────────────────┐ │   │   Receiver + Channel)   │
│  │ NoorDatabase    │ │ Firebase Cloud  │ │   └─────────────────────────┘
│  │ (Room SQLite)   │ │ (Firestore)     │ │
│  │  - ObligationDao│ │  - daily_logs   │ │
│  │  - QuranDao     │ │  - user_streaks │ │
│  │  - ReflectionDao│ └─────────────────┘ │
│  └─────────────────┘                     │
└──────────────────────────────────────────┘
```

### Data Entities & Schema:
- **`DailyObligationEntity`**: `id` (`"${date}_${userId}"`), `userId`, `date`, `fajr`, `dhuhr`, `asr`, `maghrib`, `isha`, `sunnahPrayers`, `isFasting`, `fastingType`, `quranPagesRead`, `quranMinutes`, `lastSurah`, `lastAyah`, `syncPending`, `lastModified`.
- **`QuranProgressEntity`**: `userId`, `lastSurah`, `lastAyah`, `totalPagesRead`, `totalMinutesSpent`, `khatmCount`, `lastUpdated`.
- **`DailyObligationDao`**: Query methods for single day, weekly range, pending sync, and atomic update transactions.

### Key Interactive Components:
1. `ObligationsViewModel`: Exposes `StateFlow<DailyLog>`, `StateFlow<InAppAlertBannerState?>`, and notification preference controls.
2. `AnimatedInAppAlertBanner`: Slides from top with action buttons for 10-minute prayer warnings, Iftar countdown, and Quran time.
3. `PrayerAlarmReceiver` & `NotificationHelper`: Fires exact 10-minute pre-prayer warnings, Suhoor/Iftar alerts, and custom Quran recitation reminders with action pending intents.
