package com.example.gymappfrontendui.screens

import androidx.compose.foundation.BorderStroke // Import needed for Card border
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape // Needed for icon background potentially
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.gymappfrontendui.Routes
import com.example.gymappfrontendui.viewmodel.LoginRegistryViewModel

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    loginViewModel: LoginRegistryViewModel,
    navController: NavController
) {
    val isLoggedIn by loginViewModel.loginState.collectAsState()
    val username by loginViewModel.currentUsername.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {

        item {
            Spacer(modifier = Modifier.height(32.dp))
            if (isLoggedIn) {
                LoggedInProfileHeader(username = username)
            } else {
                GuestProfileHeader(navController = navController)
            }
            Spacer(modifier = Modifier.height(32.dp))
        }

        item {
            SectionTitle("Features")
        }
        item {
            SettingsOptionItem(
                icon = Icons.Default.AccountBox,
                text = "Body Measurements",
                onClick = { navController.navigate(Routes.BodyMeasurementsScreen) }
            )
        }
        item {
            SettingsOptionItem(
                icon = Icons.Default.DateRange,
                text = "Workout Progress",
                onClick = { navController.navigate(Routes.WorkoutProgressScreen) }
            )
        }
        if (isLoggedIn) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
                SectionTitle("Account")
            }
            item {
                SettingsOptionItem(
                    icon = Icons.Default.Email,
                    text = "Change Email Address",
                    onClick = { /* TODO: Show change email dialog */ }
                )
            }
            item {
                SettingsOptionItem(
                    icon = Icons.Default.Lock,
                    text = "Change Password",
                    onClick = { /* TODO: Show change password dialog */ }
                )
            }
            item {
                Spacer(modifier = Modifier.height(40.dp))
                Button(
                    onClick = {
                        loginViewModel.logout()
                        navController.navigate("login_page") {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Logout")
                }
                // Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}


@Composable
fun LoggedInProfileHeader(username: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
    ) {
        Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = "User Avatar",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(100.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Welcome, $username!",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
fun GuestProfileHeader(navController: NavController) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Browsing as Guest",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Log in or sign up to sync your progress online and unlock all features.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = { navController.navigate("login_page") },
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                Text("Login / Sign Up")
            }
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp, top = 16.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsOptionItem(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit
) {
    Column {
        ListItem(
            headlineContent = {
                Text(text, fontWeight = FontWeight.Medium)
            },
            leadingContent = {
                Icon(
                    imageVector = icon,
                    contentDescription = text,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            trailingContent = {
                Icon(
                    imageVector = Icons.AutoMirrored.Default.ArrowForward,
                    contentDescription = "Go to $text",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .clickable { onClick() },
            colors = ListItemDefaults.colors(
                containerColor = Color.Transparent
            )
        )
        HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
    }
}