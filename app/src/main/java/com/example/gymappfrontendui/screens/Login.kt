package com.example.gymappfrontendui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.* // Use wildcard import
import androidx.compose.foundation.text.KeyboardActions // Import for keyboard actions
import androidx.compose.foundation.text.KeyboardOptions // Import for keyboard options
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.* // Use wildcard import
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
// Removed unused Color import
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager // Import to manage focus
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction // Import for keyboard actions
import androidx.compose.ui.text.input.KeyboardType // Import for keyboard types
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
// Removed unused sp import
import androidx.navigation.NavController
import com.example.gymappfrontendui.R
import com.example.gymappfrontendui.Routes
import com.example.gymappfrontendui.viewmodel.LoginRegistryViewModel


@Composable
fun LoginScreen(navController: NavController, mainViewModel: LoginRegistryViewModel) {

    val loggedInUser by mainViewModel.loggedInUser.collectAsState(initial = null)

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.biceps),
            contentDescription = "App logo",
            modifier = Modifier.size(160.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "Welcome!",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "Login to your account",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { username = it; if (showError) showError = isEmpty(it) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Username") },
            leadingIcon = {
                Icon(Icons.Rounded.AccountCircle, contentDescription = "Username icon")
            },
            isError = showError && isEmpty(username),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
        )

        Spacer(modifier = Modifier.height(4.dp))
        if (showError && isEmpty(username)) {
            Text(
                text = "Username is required!",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(start = 16.dp)
            )
        } else {
            Spacer(modifier = Modifier.height(4.dp + 16.dp))
        }

        OutlinedTextField(
            value = password,
            onValueChange = { password = it; if (showError) showError = isEmpty(it) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Password") },
            leadingIcon = {
                Icon(Icons.Rounded.Lock, contentDescription = "Password icon")
            },
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val imagePainter = if (showPassword)
                    painterResource(id = R.drawable.show)
                else
                    painterResource(id = R.drawable.hide)
                val description = if (showPassword) "Hide password" else "Show password"

                Icon(
                    painter = imagePainter,
                    contentDescription = description,
                    modifier = Modifier
                        .clickable { showPassword = !showPassword }
                        .size(24.dp)
                )
            },
            isError = showError && isEmpty(password),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = {
                focusManager.clearFocus()
            })
        )
        Spacer(modifier = Modifier.height(4.dp))
        if (showError && isEmpty(password)) {
            Text(
                text = "Password is required!",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(start = 16.dp)
            )
        } else {
            Spacer(modifier = Modifier.height(4.dp + 16.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                focusManager.clearFocus()
                showError = true
                if (!isEmpty(username) && !isEmpty(password)) {


                    mainViewModel.login(username, password) { success ->

                        if (success) {
                            navController.navigate("Main_Screen/$username") {

                                popUpTo(Routes.LoginPage) { inclusive = true }
                            }
                        } else {
                            Toast.makeText(context, "Błędne dane logowania", Toast.LENGTH_SHORT)
                                .show()
                        }

                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Login")
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = {
                mainViewModel.setAppHasBeenOpened()
                navController.navigate("Main_Screen/guest") {
                    popUpTo("Login_Page") { inclusive = true }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Text(
                "Continue without account",
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(onClick = { /* TODO: Implement Forgot Password */ }) {
                Text(text = "Forgot Password?")
            }
            TextButton(onClick = { navController.navigate("Register_Page") }) {
                Text(text = "Register here")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant) // Use theme color
            Text(
                "  OR  ",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant) // Use theme color
        }

        Spacer(modifier = Modifier.height(16.dp))

        Image(
            painter = painterResource(id = R.drawable.google),
            contentDescription = "Login with Google",
            modifier = Modifier
                .size(50.dp)
                .clickable {
                    // TODO: Implement Google login
                }
        )
    }
}

fun isEmpty(text: String): Boolean {
    return text.isEmpty()
}