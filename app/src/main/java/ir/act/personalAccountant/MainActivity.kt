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
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import ir.act.personalAccountant.presentation.googlesheets.GoogleSheetsViewModel
import ir.act.personalAccountant.presentation.navigation.PersonalAccountantNavigation
import ir.act.personalAccountant.ui.theme.PersonalAccountantTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

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
                    PersonalAccountantNavigation(
                        navController = navController,
                        googleSheetsViewModel = googleSheetsViewModel,
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