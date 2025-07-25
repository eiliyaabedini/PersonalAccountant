package ir.act.personalAccountant

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import ir.act.personalAccountant.presentation.MainViewModel
import ir.act.personalAccountant.presentation.googlesheets.GoogleSheetsViewModel
import ir.act.personalAccountant.presentation.navigation.PersonalAccountantNavigation
import ir.act.personalAccountant.presentation.navigation.Routes
import ir.act.personalAccountant.ui.theme.PersonalAccountantTheme
import ir.act.personalAccountant.util.Constants

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()
    private val googleSheetsViewModel: GoogleSheetsViewModel by viewModels()

    private val signInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            googleSheetsViewModel.handleSignInResult(result.data)
        }
    }

    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PersonalAccountantTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val isUserSignedIn by mainViewModel.isUserSignedIn.collectAsState(initial = false)
                    var navigationDestination by remember { mutableStateOf<String?>(null) }

                    LaunchedEffect(Unit) {
                        navigationDestination =
                            intent?.getStringExtra(Constants.Navigation.NAVIGATE_TO_KEY)
                    }

                    LaunchedEffect(navigationDestination) {
                        navigationDestination?.let { destination ->
                            when (destination) {
                                Constants.Navigation.NAVIGATE_TO_EXPENSE_ENTRY -> {
                                    navController.navigate(Routes.EXPENSE_ENTRY)
                                    navigationDestination = null
                                }
                            }
                        }
                    }

                    // Always start with the main expense list - login is optional
                    val startDestination = Routes.EXPENSE_LIST
                    
                    PersonalAccountantNavigation(
                        navController = navController,
                        googleSheetsViewModel = googleSheetsViewModel,
                        startDestination = startDestination,
                        onGoogleSignInClick = {
                            val signInIntent = googleSheetsViewModel.getSignInIntent()
                            signInLauncher.launch(signInIntent)
                        }
                    )
                }
            }
        }
    }
}