package com.example.gymappfrontendui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountBox // Zmieniono ikonę
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
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        item {
            Spacer(modifier = Modifier.height(16.dp))
            if (isLoggedIn) {
                LoggedInProfileHeader(username = username)
            } else {
                GuestProfileHeader(navController = navController)
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            SectionTitle("Funkcje")
        }
        item {
            SettingsOptionItem(
                icon = Icons.Default.AccountBox,
                text = "Pomiary ciała",
                onClick = { /* TODO: navController.navigate("body_measurements") */ }
            )
        }
        item {
            SettingsOptionItem(
                icon = Icons.Default.DateRange,
                text = "Postępy treningowe",
                onClick = { /* TODO: navController.navigate("progress_charts") */ }
            )
        }
        if (isLoggedIn) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SectionTitle("Konto")
            }
            item {
                SettingsOptionItem(
                    icon = Icons.Default.Email,
                    text = "Zmień adres email",
                    onClick = { /* TODO: Pokaż dialog zmiany emaila */ }
                )
            }
            item {
                SettingsOptionItem(
                    icon = Icons.Default.Lock,
                    text = "Zmień hasło",
                    onClick = { /* TODO: Pokaż dialog zmiany hasła */ }
                )
            }
            item {
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = {
                        loginViewModel.logout()
                        navController.navigate("Login_Page") {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Wyloguj")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Wyloguj się")
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}


@Composable
fun LoggedInProfileHeader(username: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = "Avatar",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(100.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Witaj, $username!",
            fontSize = 22.sp,
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
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Przeglądasz jako Gość",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Zaloguj się lub załóż konto, aby synchronizować swoje postępy online.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { navController.navigate("Login_Page") },
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                Text("Zaloguj się / Zarejestruj")
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
            .padding(bottom = 8.dp, top = 8.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsOptionItem(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit
) {
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
                // Użyj standardowej ikony "chevron right"
                imageVector = Icons.AutoMirrored.Default.ArrowForward,
                contentDescription = "Przejdź",
                modifier = Modifier.size(16.dp),
                tint = Color.Gray
            )
        },
        modifier = Modifier
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
    )
    // Użyj standardowego Dividera zamiast tła
    Divider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
}