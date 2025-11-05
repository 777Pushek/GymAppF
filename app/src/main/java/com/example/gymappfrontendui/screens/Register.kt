package com.example.gymappfrontendui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.gymappfrontendui.R
import com.example.gymappfrontendui.viewmodel.LoginRegistryViewModel
import kotlinx.coroutines.launch

private fun isValidPassword(password: String): Boolean {
    if (password.length < 8) {
        return false
    }
    if (!password.any { it.isUpperCase() }) return false
    if (!password.any { it.isDigit() }) return false
    return true
}

@Composable
fun RegisterScreen(navController: NavController, mainViewModel: LoginRegistryViewModel) {

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    val passwordsNotMatching = password != confirmPassword
    val passwordTooWeak = !isValidPassword(password) || password.isBlank()
    val usernameEmpty = username.isBlank()

    var showPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    var showGuestDataDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    val navigateToMain = {
        navController.navigate("Main_Screen/$username") {
            popUpTo("Login_Page") { inclusive = true }
        }
    }

    if (showGuestDataDialog) {
        AlertDialog(
            onDismissRequest = {
                showGuestDataDialog = false
                navigateToMain()
            },
            title = { Text("Guest data transfer") },
            text = { Text("We've detected data created while using the app as a guest. Would you like to transfer this data to your new account?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showGuestDataDialog = false
                        scope.launch {
                            mainViewModel.assignGuestDataToUser()
                            navigateToMain()
                        }
                    }
                ) {
                    Text("Yes, transfer")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showGuestDataDialog = false
                        navigateToMain()
                    }
                ) {
                    Text("No")
                }
            }
        )
    }

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
            "Register",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "Enter your details below to register",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { username = it; if (showError) showError = username.isBlank() },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Username") },
            leadingIcon = {
                Icon(Icons.Rounded.AccountCircle, contentDescription = "Username icon")
            },
            isError = showError && usernameEmpty,
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
        )
        if (showError && usernameEmpty) {
            Text(
                text = "Username is required!",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(start = 16.dp, top = 4.dp)
            )
        } else {
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it; if (showError) showError = !isValidPassword(it) || it.isBlank() },
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
            isError = showError && passwordTooWeak,
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next
            )
        )
        if (showError && passwordTooWeak) {
            val errorText = when {
                password.isBlank() -> "Password is required!"
                !isValidPassword(password) -> "Min. 8 chars, 1 uppercase, 1 digit"
                else -> ""
            }
            Text(
                text = errorText,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(start = 16.dp, top = 4.dp)
            )
        } else {
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it; if (showError) showError = password != it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Confirm Password") },
            leadingIcon = {
                Icon(Icons.Rounded.Lock, contentDescription = "Confirm password icon")
            },
            visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val imagePainter = if (showConfirmPassword)
                    painterResource(id = R.drawable.show)
                else
                    painterResource(id = R.drawable.hide)
                val description = if (showConfirmPassword) "Hide password" else "Show password"

                Icon(
                    painter = imagePainter,
                    contentDescription = description,
                    modifier = Modifier
                        .clickable { showConfirmPassword = !showConfirmPassword }
                        .size(24.dp)
                )
            },
            isError = showError && passwordsNotMatching,
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = {
                focusManager.clearFocus()
            })
        )
        if (showError && passwordsNotMatching) {
            Text(
                text = "Passwords do not match",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(start = 16.dp, top = 4.dp)
            )
        } else {
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                focusManager.clearFocus()
                showError = true
                val canRegister = !passwordsNotMatching && !passwordTooWeak && !username.isBlank()

                if (canRegister) {

                    mainViewModel.register(username, password) { success ->
                        if (success) {
                            scope.launch {
                                if (mainViewModel.hasGuestData()) {
                                    showGuestDataDialog = true
                                } else {
                                    navigateToMain()
                                }
                            }
                        } else {
                            Toast.makeText(
                                context,
                                "Register failed (username might be taken)",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Register")
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = { navController.navigate("Login_Page") }) {
            Text("Back to Login")
        }
    }
}
