# Blood Transport Temperature Monitoring System (IoT Real-Time Dashboard)

A native Android application built with **Kotlin** and **Jetpack Compose** implementing a clean, minimal, flat, high-contrast monochrome IoT monitoring dashboard for medical blood cold chain transport.

---

## 🚀 Architecture Overview (MVVM)

The application follows the **Model-View-ViewModel (MVVM)** architectural pattern:

```
┌──────────────────────────────────────────────────────────┐
│              Supabase PostgreSQL Database                │
│                 table: "telemetry_logs"                  │
└─────────────────────────────┬────────────────────────────┘
                              │ WebSockets / Realtime Stream
                              ▼
┌──────────────────────────────────────────────────────────┐
│       Data Layer: SupabaseProvider & Repository          │
│   • SupabaseConfig.kt (URL & Anon Key Constants)         │
│   • TelemetryRepositoryImpl.kt (ChangeFlow & Decoder)     │
└─────────────────────────────┬────────────────────────────┘
                              │ Flow<TelemetryLog>
                              ▼
┌──────────────────────────────────────────────────────────┐
│              ViewModel: DashboardViewModel               │
│   • StateFlow<DashboardUiState>                          │
│   • State Evaluation (NORMAL, TEMP ALERT, TAMPER ALERT)  │
│   • Rolling 5-Item Telemetry Buffer                      │
│   • Session Statistics (Max/Min Temp, Tamper Counter)    │
│   • 4-Digit Custody PIN Validation ("1234")              │
└─────────────────────────────┬────────────────────────────┘
                              │ StateFlow & Haptic Event
                              ▼
┌──────────────────────────────────────────────────────────┐
│            UI Layer: Jetpack Compose Dashboard           │
│   • Top Bar (Trip ID, Custom Battery & Signal Icons)     │
│   • Cargo Dropdown (Whole Blood: 2-6°C | Platelets: 20-24°)│
│   • Center Massive Real-time Temperature Display (82sp)  │
│   • Dynamic Solid Color Background Transitions           │
│   • Tamper Banner ("DATA INTEGRITY COMPROMISED")         │
│   • Rolling 5-reading Telemetry History List             │
│   • Chain of Custody Full-width Action Button & Modal    │
│   • Handover Summary Screen                              │
│   • Android Vibrator Service Integration                 │
└──────────────────────────────────────────────────────────┘
```

---

## 📦 Project Structure

```
BloodTransportMonitor/
├── app/
│   ├── build.gradle.kts                   # Compose, Supabase, Ktor, Serialization dependencies
│   ├── proguard-rules.pro                 # Proguard keep rules for Ktor & Supabase
│   └── src/main/
│       ├── AndroidManifest.xml            # INTERNET, ACCESS_NETWORK_STATE, VIBRATE
│       ├── java/com/bloodtransport/iot/
│       │   ├── BloodTransportApp.kt       # Application class
│       │   ├── MainActivity.kt            # Edge-to-edge Compose root
│       │   ├── data/
│       │   │   ├── config/
│       │   │   │   └── SupabaseConfig.kt  # SUPABASE_URL & SUPABASE_ANON_KEY placeholders
│       │   │   ├── model/
│       │   │   │   ├── CargoType.kt       # Whole Blood [2-6°C] & Platelets [20-24°C]
│       │   │   │   ├── SystemAlertState.kt# NORMAL, TEMPERATURE_ALERT, TAMPER_ALERT
│       │   │   │   └── TelemetryLog.kt    # PostgreSQL table schema data class
│       │   │   ├── remote/
│       │   │   │   └── SupabaseProvider.kt# Supabase client singleton with Realtime & Postgrest
│       │   │   └── repository/
│       │   │       ├── TelemetryRepository.kt
│       │   │       └── TelemetryRepositoryImpl.kt # WebSocket subscriber + simulator fallback
│       │   ├── ui/
│       │   │   ├── components/
│       │   │   │   ├── BatteryIcon.kt     # Minimal vector battery with fill & %
│       │   │   │   ├── SignalIcon.kt      # Cellular 4-bar signal vector
│       │   │   │   ├── TamperBanner.kt    # "DATA INTEGRITY COMPROMISED" banner
│       │   │   │   ├── TelemetryHistoryList.kt # Rolling buffer table
│       │   │   │   ├── PinModalDialog.kt  # 4-Digit Custody PIN keypad modal
│       │   │   │   └── TripSummaryDialog.kt # Session audit summary
│       │   │   ├── screen/
│       │   │   │   ├── DashboardScreen.kt # Main Jetpack Compose screen
│       │   │   │   └── DashboardUiState.kt# Immutable UI state & session stats
│       │   │   ├── theme/
│       │   │   │   ├── Color.kt           # Pure black/white & alert palettes
│       │   │   │   ├── Theme.kt           # High-contrast theme
│       │   │   │   └── Type.kt            # Sans-serif & tabular typography
│       │   │   └── viewmodel/
│       │   │       ├── DashboardViewModel.kt
│       │   │       └── DashboardViewModelFactory.kt
│       │   └── util/
│       │       └── VibrationHelper.kt     # Android Vibrator heavy haptic alerts
│       └── res/
│           ├── values/ (strings.xml, colors.xml, themes.xml)
│           ├── drawable/ (vector assets)
│           └── mipmap-anydpi-v26/ (launcher icons)
├── schema.sql                             # PostgreSQL schema & Realtime publication SQL
├── build.gradle.kts                       # Root build configuration
├── settings.gradle.kts                    # Project modules
└── gradle.properties
```

---

## 🛠️ Step 1: Configure Supabase

1. Open `schema.sql` in your Supabase project's **SQL Editor** and run it:
   ```sql
   CREATE TABLE IF NOT EXISTS public.telemetry_logs (
       id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
       trip_id TEXT NOT NULL,
       temperature DOUBLE PRECISION NOT NULL,
       battery_percentage INT NOT NULL CHECK (battery_percentage >= 0 AND battery_percentage <= 100),
       signal_strength INT NOT NULL,
       timestamp TEXT NOT NULL DEFAULT to_char(NOW(), 'YYYY-MM-DD"T"HH24:MI:SS"Z"'),
       is_tampered BOOLEAN NOT NULL DEFAULT FALSE
   );

   ALTER TABLE public.telemetry_logs ENABLE ROW LEVEL SECURITY;

   CREATE POLICY "Allow public read access to telemetry_logs"
       ON public.telemetry_logs FOR SELECT TO anon, authenticated USING (true);

   CREATE POLICY "Allow public insert access to telemetry_logs"
       ON public.telemetry_logs FOR INSERT TO anon, authenticated WITH CHECK (true);

   -- Crucial: Enable WebSockets publication
   ALTER PUBLICATION supabase_realtime ADD TABLE public.telemetry_logs;
   ```

2. Open `app/src/main/java/com/bloodtransport/iot/data/config/SupabaseConfig.kt` and paste your project credentials:
   ```kotlin
   object SupabaseConfig {
       const val SUPABASE_URL: String = "https://your-project-ref.supabase.co"
       const val SUPABASE_ANON_KEY: String = "eyJhbGciOiJIUzI1NiIsInR5..."
   }
   ```

*(Note: If placeholders remain untouched, the app automatically runs in built-in **Simulation Mode**, streaming simulated packet heartbeats every 3 seconds so you can test all features immediately without setup!)*

---

## 🎯 Key Features & Requirements

### 1. Three Evaluated Operational States & Dynamic Color Switching
- **NORMAL**:
  - Condition: Temperature in range (`2.0°C – 6.0°C` for Whole Blood, `20.0°C – 24.0°C` for Platelets) and `is_tampered == false`.
  - Background: Solid **Pure Black (`#000000`)**.
  - Text: High-contrast **Pure White (`#FFFFFF`)**.
- **TEMPERATURE ALERT**:
  - Condition: Temperature out of safe bounds.
  - Background: Solid **Red (`#D32F2F`)**.
  - Text: Pure White.
  - Triggers **heavy haptic vibration**.
- **TAMPER ALERT**:
  - Condition: `is_tampered == true`.
  - Background: Solid **Yellow (`#FFD600`)**.
  - Banner: Solid Black banner with `"DATA INTEGRITY COMPROMISED"`.
  - Text: Pure Black.
  - Triggers **heavy haptic vibration**.

### 2. Massive Real-time Temperature Display
- Uses clean sans-serif typography (`82.sp` headline) with decimal precision and subscript `°C` unit.
- Status badge indicates cold-chain specification status in real time.

### 3. Cargo Type Selection Dropdown
- Tap the selector pill in the upper section to switch between:
  - **Whole Blood**: `2.0°C` to `6.0°C`.
  - **Platelets**: `20.0°C` to `24.0°C`.
- When selected, the ViewModel immediately re-evaluates the active temperature against the new thresholds and updates UI/alerts.

### 4. Rolling Telemetry Buffer (Last 5 Readings)
- Maintained as an immutable FIFO list in ViewModel `StateFlow`.
- Displayed in a flat monochrome table with timestamps, temperature, and status chips.

### 5. Chain of Custody Action & Handover Summary
- Full-width **"COMPLETE TRIP (CHAIN OF CUSTODY)"** button anchored at the bottom.
- Opens a clean 4-digit PIN authentication modal:
  - Hardcoded validation PIN: **`1234`**.
  - Displays masked dots and custom high-contrast keypad with haptic click feedback.
- Upon correct PIN entry, the **Trip Audit Summary** is displayed:
  - **Max Temp** reached during the session.
  - **Min Temp** reached during the session.
  - **Tamper Count** total incidents.
  - Total Telemetry Data Packets logged.
  - Option to **Start New Trip** (resets session stats) or close the modal.

### 6. Interactive QA Simulator
- Tap the tuning icon (`Tune`) in the top right to open the QA Quick Test Bar.
- Instant single-tap triggers:
  - `Normal (4.2°)`: Verifies Normal state (Black background).
  - `Excursion (9.5°)`: Verifies Temperature Alert (Red background + heavy vibration).
  - `Tamper Breach`: Verifies Tamper Alert (Yellow background + Tamper Banner + heavy vibration).
