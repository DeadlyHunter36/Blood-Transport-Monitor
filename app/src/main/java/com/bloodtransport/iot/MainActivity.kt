package com.bloodtransport.iot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.bloodtransport.iot.data.repository.TelemetryRepositoryImpl
import com.bloodtransport.iot.ui.screen.DashboardScreen
import com.bloodtransport.iot.ui.theme.BloodTransportTheme
import com.bloodtransport.iot.ui.viewmodel.DashboardViewModel
import com.bloodtransport.iot.ui.viewmodel.DashboardViewModelFactory

/**
 * Main Activity hosting the real-time Blood Transport Temperature Monitoring dashboard.
 */
class MainActivity : ComponentActivity() {

    private val viewModel: DashboardViewModel by viewModels {
        DashboardViewModelFactory(TelemetryRepositoryImpl())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            BloodTransportTheme {
                DashboardScreen(viewModel = viewModel)
            }
        }
    }
}
