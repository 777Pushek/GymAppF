import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.gymappfrontendui.Routes
import com.example.gymappfrontendui.viewmodel.LoginRegistryViewModel

@Composable
fun AppNavigationRouter(
    navController: NavController,
    viewModel: LoginRegistryViewModel
) {

    val usernameState by viewModel.currentUsername.collectAsStateWithLifecycle()
    val isFirstLaunch = viewModel.isFirstLaunch()

    LaunchedEffect(usernameState) {
        if (usernameState != null) {
            val destination = if (isFirstLaunch) {
                Routes.LoginPage
            } else {
                "${Routes.MainScreen}/$usernameState"
            }
            navController.navigate(destination) {
                popUpTo(Routes.AppRouter) { inclusive = true }
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
    }
}