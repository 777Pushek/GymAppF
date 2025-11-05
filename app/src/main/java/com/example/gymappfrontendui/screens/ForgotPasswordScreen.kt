package com.example.gymappfrontendui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.gymappfrontendui.R
import com.example.gymappfrontendui.Routes
import com.example.gymappfrontendui.viewmodel.ForgotPasswordState
import com.example.gymappfrontendui.viewmodel.ForgotPasswordStep
import com.example.gymappfrontendui.viewmodel.ForgotPasswordViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordFlowScreen(
    navController: NavController,
    forgotPasswordViewModel: ForgotPasswordViewModel = viewModel()
) {
    val uiState by forgotPasswordViewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reset Password") },
                navigationIcon = {
                    if (uiState.currentStep == ForgotPasswordStep.EnterCode || uiState.currentStep == ForgotPasswordStep.EnterNewPassword) {
                        IconButton(onClick = forgotPasswordViewModel::goBackStep) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    } else if (uiState.currentStep == ForgotPasswordStep.EnterUsername) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to Login")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 24.dp)) {
            when (uiState.currentStep) {
                ForgotPasswordStep.EnterUsername -> EnterUsernameStep(uiState, forgotPasswordViewModel)
                ForgotPasswordStep.EnterCode -> EnterCodeStep(uiState, forgotPasswordViewModel, navController)
                ForgotPasswordStep.EnterNewPassword -> EnterNewPasswordStep(uiState, forgotPasswordViewModel, navController)
                ForgotPasswordStep.Success -> PasswordResetSuccessStep(navController)
                ForgotPasswordStep.Error -> {}
            }
        }
    }
}
@Composable
fun EnterUsernameStep(
    uiState: ForgotPasswordState,
    viewModel: ForgotPasswordViewModel
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Enter your username", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "We'll send a verification code to the email associated with your account.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = uiState.username,
            onValueChange = viewModel::updateUsername,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Username") },
            leadingIcon = { Icon(Icons.Rounded.AccountCircle, contentDescription = null) },
            isError = uiState.usernameError != null,
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done), // Use correct ImeAction import
            keyboardActions = KeyboardActions(onDone = { viewModel.sendResetCode() }),
            readOnly = uiState.isLoading
        )
        uiState.usernameError?.let {
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall, modifier = Modifier.align(Alignment.Start).padding(start = 16.dp, top = 4.dp))
        }
        uiState.errorMessage?.let {
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 8.dp), textAlign = TextAlign.Center)
        }
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = viewModel::sendResetCode,
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading
        ) {
            AnimatedContent(targetState = uiState.isLoading, label = "Send Code Button Anim") { isLoading ->
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp, color = LocalContentColor.current)
                } else {
                    Text("Send Verification Code")
                }
            }
        }
    }
}
@Composable
fun EnterCodeStep(
    uiState: ForgotPasswordState,
    viewModel: ForgotPasswordViewModel,
    navController: NavController
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Enter Verification Code", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Check your email for the verification code we sent to the address linked to username '${uiState.username}'.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = uiState.code,
            onValueChange = viewModel::updateCode,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Verification Code") },
            leadingIcon = { Icon(Icons.Rounded.Email, contentDescription = null) }, // Use appropriate icon
            isError = uiState.codeError != null,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done), // Use correct ImeAction import
            keyboardActions = KeyboardActions(onDone = { viewModel.verifyResetCode() }),
            readOnly = uiState.isLoading
        )
        uiState.codeError?.let {
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall, modifier = Modifier.align(Alignment.Start).padding(start = 16.dp, top = 4.dp))
        }
        uiState.errorMessage?.let {
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 8.dp), textAlign = TextAlign.Center)
        }
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = viewModel::verifyResetCode,
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading
        ) {
            AnimatedContent(targetState = uiState.isLoading, label = "Verify Code Button Anim") { isLoading ->
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp, color = LocalContentColor.current)
                } else {
                    Text("Verify Code")
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        TextButton(
            onClick = {
                viewModel.cancelAndGoToLogin()
                navController.popBackStack()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cancel", color = MaterialTheme.colorScheme.error)
        }
        TextButton(onClick = { viewModel.sendResetCode() }) {
            Text("Resend Code")
        }
    }
}

@Composable
fun EnterNewPasswordStep(
    uiState: ForgotPasswordState,
    viewModel: ForgotPasswordViewModel,
    navController: NavController
) {
    var showPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Enter New Password", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = uiState.newPassword,
            onValueChange = viewModel::updateNewPassword,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("New Password") },
            leadingIcon = { Icon(Icons.Rounded.Lock, contentDescription = null) },
            isError = uiState.newPasswordError != null,
            singleLine = true,
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next), // Use correct ImeAction import
            trailingIcon = {
                IconButton(onClick = { showPassword = !showPassword }) {
                    Icon(
                        painter = painterResource(if (showPassword) R.drawable.show else R.drawable.hide),
                        contentDescription = "Show/Hide password",
                        modifier = Modifier.size(24.dp)
                    )
                }
            },
            readOnly = uiState.isLoading
        )
        uiState.newPasswordError?.let {
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall, modifier = Modifier.align(Alignment.Start).padding(start = 16.dp, top = 4.dp))
        }
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = uiState.confirmPassword,
            onValueChange = viewModel::updateConfirmPassword,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Confirm New Password") },
            leadingIcon = { Icon(Icons.Rounded.Lock, contentDescription = null) },
            isError = uiState.confirmPasswordError != null,
            singleLine = true,
            visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done), // Use correct ImeAction import
            keyboardActions = KeyboardActions(onDone = { viewModel.resetPassword() }),
            trailingIcon = {
                IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                    Icon(
                        painter = painterResource(if (showConfirmPassword) R.drawable.show else R.drawable.hide),
                        contentDescription = "Show/Hide password",
                        modifier = Modifier.size(24.dp)
                    )
                }
            },
            readOnly = uiState.isLoading
        )
        uiState.confirmPasswordError?.let {
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall, modifier = Modifier.align(Alignment.Start).padding(start = 16.dp, top = 4.dp))
        }
        uiState.errorMessage?.let {
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 8.dp), textAlign = TextAlign.Center)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = viewModel::resetPassword,
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading
        ) {
            AnimatedContent(targetState = uiState.isLoading, label = "Reset Pwd Button Anim") { isLoading ->
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp, color = LocalContentColor.current)
                } else {
                    Text("Reset Password")
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        TextButton(
            onClick = {
                viewModel.cancelAndGoToLogin()
                navController.navigate(Routes.LoginPage) {
                    popUpTo(Routes.LoginPage) { inclusive = true }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cancel", color = MaterialTheme.colorScheme.error)
        }
    }
}
@Composable
fun PasswordResetSuccessStep(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Filled.Check,
            contentDescription = "Success",
            tint = Color(0xFF4CAF50),
            modifier = Modifier.size(80.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Password Reset Successful!",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "You can now log in with your new password.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = {
                navController.navigate(Routes.LoginPage) {
                    popUpTo(Routes.LoginPage) { inclusive = true }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back to Login")
        }
    }
}