package com.example.knowlegegraphtoefl

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.knowlegegraphtoefl.ui.screens.HomeScreen
import com.example.knowlegegraphtoefl.ui.screens.PracticeScreen
import com.example.knowlegegraphtoefl.ui.theme.KnowlegeGraphToeflTheme
import com.example.knowlegegraphtoefl.ui.viewmodel.HomeUiState
import com.example.knowlegegraphtoefl.ui.viewmodel.HomeViewModel
import com.example.knowlegegraphtoefl.ui.viewmodel.PracticeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KnowlegeGraphToeflTheme {
                ToeflAppNavHost()
            }
        }
    }
}

@Composable
fun ToeflAppNavHost() {
    val navController = rememberNavController()
    
    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            val viewModel: HomeViewModel = hiltViewModel()
            HomeScreen(
                viewModel = viewModel,
                onStartPractice = {
                    navController.navigate("practice")
                }
            )
        }
        composable("practice") {
            val homeParentEntry = remember(it) {
                navController.getBackStackEntry("home")
            }
            val homeViewModel: HomeViewModel = hiltViewModel(homeParentEntry)
            val practiceViewModel: PracticeViewModel = hiltViewModel()
            
            val homeState by homeViewModel.uiState.collectAsState()
            
            if (homeState is HomeUiState.Success) {
                PracticeScreen(
                    viewModel = practiceViewModel,
                    targetSentences = (homeState as HomeUiState.Success).task.sentences,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
