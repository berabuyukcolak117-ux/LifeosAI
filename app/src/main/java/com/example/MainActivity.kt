package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.ui.LifeViewModel
import com.example.ui.LifeViewModelFactory
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.MainDashboard
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.ProfileWizardScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    
    private val viewModel: LifeViewModel by viewModels {
        LifeViewModelFactory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    // Access routing states from view model
                    val onboardingCompleted by viewModel.isOnboardingCompleted.collectAsState()
                    val loggedIn by viewModel.isLoggedIn.collectAsState()
                    val wizardStep by viewModel.wizardStep.collectAsState()

                    Crossfade(
                        targetState = RouterState(onboardingCompleted, loggedIn, wizardStep),
                        label = "app_router"
                    ) { state ->
                        when {
                            !state.onboardingCompleted -> {
                                OnboardingScreen(
                                    onFinished = { viewModel.completeOnboarding() }
                                )
                            }
                            !state.loggedIn -> {
                                LoginScreen(
                                    onLoginCompleted = { email ->
                                        viewModel.performLogin(email)
                                    }
                                )
                            }
                            state.wizardStep < 8 -> {
                                ProfileWizardScreen(
                                    viewModel = viewModel,
                                    onWizardCompleted = {
                                        // Completed Wizard setup
                                    }
                                )
                            }
                            else -> {
                                MainDashboard(viewModel = viewModel)
                            }
                        }
                    }
                }
            }
        }
    }
}

data class RouterState(
    val onboardingCompleted: Boolean,
    val loggedIn: Boolean,
    val wizardStep: Int
)
