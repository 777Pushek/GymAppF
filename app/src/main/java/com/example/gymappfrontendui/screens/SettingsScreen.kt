package com.example.gymappfrontendui.screens

import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.gymappfrontendui.R
import com.example.gymappfrontendui.Routes
import com.example.gymappfrontendui.viewmodel.LoginRegistryViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private enum class AccountSheetState {
    NONE,
    USERNAME,
    PASSWORD,
    EMAIL,
    VERIFY_EMAIL
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    loginViewModel: LoginRegistryViewModel,
    navController: NavController
) {
    val isLoggedIn by loginViewModel.loginState.collectAsState()
    val username by loginViewModel.currentUsername.collectAsState()
    val loggedInUser by loginViewModel.loggedInUser.collectAsState()

    var sheetState by remember { mutableStateOf(AccountSheetState.NONE) }
    val bottomSheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val onDismissSheet: () -> Unit = {
        scope.launch { bottomSheetState.hide() }.invokeOnCompletion {
            if (!bottomSheetState.isVisible) sheetState = AccountSheetState.NONE
        }
    }
    val onErrorSheet: (String) -> Unit = { message ->
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
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
                    iconRes = R.drawable.progress,
                    text = "Measurement Progress",
                    onClick = { navController.navigate(Routes.BodyMeasurementProgressScreen) }
                )
            }
            item {
                SettingsOptionItem(
                    iconRes = R.drawable.fitness,
                    text = "Workout Progress",
                    onClick = { navController.navigate(Routes.WorkoutProgressScreen) }
                )
            }
            item {
                SettingsOptionItem(
                    icon = Icons.Default.Notifications,
                    text = "Workout Reminder",
                    onClick = {
                        navController.navigate(Routes.ScheduleList)
                    }
                )
            }

            if (isLoggedIn) {
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    SectionTitle("Account")
                }
                item {
                    SettingsOptionItem(
                        icon = Icons.Default.AccountCircle,
                        text = "Change Username",
                        onClick = { sheetState = AccountSheetState.USERNAME }
                    )
                }
                item {
                    SettingsOptionItem(
                        icon = Icons.Default.Email,
                        text = "Change Email Address",
                        onClick = { sheetState = AccountSheetState.EMAIL }
                    )
                }

                if (loggedInUser?.email != null && loggedInUser?.emailVerified == false) {
                    item {
                        SettingsOptionItem(
                            icon = Icons.Default.Check,
                            text = "Verify Email Address",
                            onClick = { sheetState = AccountSheetState.VERIFY_EMAIL }
                        )
                    }
                }

                item {
                    SettingsOptionItem(
                        icon = Icons.Default.Lock,
                        text = "Change Password",
                        onClick = { sheetState = AccountSheetState.PASSWORD }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(40.dp))
                    Button(
                        onClick = {
                            scope.launch {
                                loginViewModel.logout()
                                navController.navigate("login_page") {
                                    popUpTo(0) { inclusive = true }
                                }
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
                }
            }
        }

        if (sheetState != AccountSheetState.NONE) {
            ModalBottomSheet(
                onDismissRequest = onDismissSheet,
                sheetState = bottomSheetState
            ) {
                when (sheetState) {
                    AccountSheetState.USERNAME -> ChangeUsernameSheet(
                        loginViewModel = loginViewModel,
                        onDismiss = onDismissSheet
                    )
                    AccountSheetState.PASSWORD -> ChangePasswordSheet(
                        loginViewModel = loginViewModel,
                        onDismiss = onDismissSheet
                    )
                    AccountSheetState.EMAIL -> ChangeEmailSheet(
                        loginViewModel = loginViewModel,
                        onDismiss = onDismissSheet
                    )
                    AccountSheetState.VERIFY_EMAIL -> VerifyEmailSheet(
                        loginViewModel = loginViewModel,
                        onDismiss = onDismissSheet,
                        onError = onErrorSheet
                    )
                    AccountSheetState.NONE -> {}
                }
            }
        }
    }
}


@Composable
fun LoggedInProfileHeader(username: String?) {
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
            text = "Welcome, ${username ?: "..."}!",
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
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsOptionItem(
    @DrawableRes iconRes: Int,
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
                    painter = painterResource(id = iconRes),
                    contentDescription = text,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
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


@Composable
private fun ChangeUsernameSheet(
    loginViewModel: LoginRegistryViewModel,
    onDismiss: () -> Unit
) {
    var currentPassword by remember { mutableStateOf("") }
    var newUsername by remember { mutableStateOf("") }
    var showCurrentPass by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    var usernameError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }
    var generalError by remember { mutableStateOf<String?>(null) }

    var isLoading by remember { mutableStateOf(false) }
    var isSuccess by remember { mutableStateOf(false) }

    LaunchedEffect(isSuccess) {
        if (isSuccess) {
            delay(1500)
            onDismiss()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.ime)
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Change Username", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = newUsername,
            onValueChange = { newUsername = it; usernameError = false; generalError = null },
            label = { Text("New Username") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = usernameError,
            readOnly = isLoading || isSuccess
        )
        if (usernameError) {
            Text(
                text = "Username cannot be empty.",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.align(Alignment.Start).padding(start = 16.dp, top = 4.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = currentPassword,
            onValueChange = { currentPassword = it; passwordError = false; generalError = null },
            label = { Text("Current Password") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = if (showCurrentPass) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                IconButton(onClick = { showCurrentPass = !showCurrentPass }) {
                    Icon(
                        painter = painterResource(if (showCurrentPass) R.drawable.show else R.drawable.hide),
                        contentDescription = "Show/Hide password",
                        modifier = Modifier.size(24.dp)
                    )
                }
            },
            isError = passwordError,
            readOnly = isLoading || isSuccess
        )
        if (passwordError) {
            Text(
                text = "Password is required.",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.align(Alignment.Start).padding(start = 16.dp, top = 4.dp)
            )
        }

        generalError?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                usernameError = newUsername.isBlank()
                passwordError = currentPassword.isBlank()
                generalError = null

                if (usernameError || passwordError) {
                    return@Button
                }

                isLoading = true
                scope.launch {
                    val success = loginViewModel.changeUserName(currentPassword, newUsername)
                    if (success) {
                        isSuccess = true
                    } else {
                        isLoading = false
                        generalError = "Incorrect password or username is taken."
                        passwordError = true
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading && !isSuccess,
            colors = ButtonDefaults.buttonColors(
                disabledContainerColor = if (isSuccess) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                disabledContentColor = if (isSuccess) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            )
        ) {
            AnimatedContent(
                targetState = when {
                    isSuccess -> "Success"
                    isLoading -> "Loading"
                    else -> "Idle"
                },
                transitionSpec = {
                    (fadeIn() + scaleIn()).togetherWith(fadeOut() + scaleOut())
                },
                label = "Username Button Animation"
            ) { state ->
                when (state) {
                    "Success" -> Icon(Icons.Default.Check, contentDescription = "Success")
                    "Loading" -> CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = LocalContentColor.current,
                        strokeWidth = 2.dp
                    )
                    else -> Text("Save Username")
                }
            }
        }
    }
}

@Composable
private fun ChangePasswordSheet(
    loginViewModel: LoginRegistryViewModel,
    onDismiss: () -> Unit
) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    var showCurrentPass by remember { mutableStateOf(false) }
    var showNewPass by remember { mutableStateOf(false) }

    var currentPasswordError by remember { mutableStateOf(false) }
    var newPasswordError by remember { mutableStateOf(false) }
    var confirmPasswordError by remember { mutableStateOf(false) }
    var generalError by remember { mutableStateOf<String?>(null) }

    var isLoading by remember { mutableStateOf(false) }
    var isSuccess by remember { mutableStateOf(false) }

    LaunchedEffect(isSuccess) {
        if (isSuccess) {
            delay(1500)
            onDismiss()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.ime)
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Change Password", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = currentPassword,
            onValueChange = { currentPassword = it; currentPasswordError = false; generalError = null },
            label = { Text("Current Password") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = if (showCurrentPass) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                IconButton(onClick = { showCurrentPass = !showCurrentPass }) {
                    Icon(
                        painter = painterResource(if (showCurrentPass) R.drawable.show else R.drawable.hide),
                        contentDescription = "Show/Hide password",
                        modifier = Modifier.size(24.dp)
                    )
                }
            },
            isError = currentPasswordError,
            readOnly = isLoading || isSuccess
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = newPassword,
            onValueChange = { newPassword = it; newPasswordError = false; generalError = null },
            label = { Text("New Password") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = if (showNewPass) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                IconButton(onClick = { showNewPass = !showNewPass }) {
                    Icon(
                        painter = painterResource(if (showNewPass) R.drawable.show else R.drawable.hide),
                        contentDescription = "Show/Hide password",
                        modifier = Modifier.size(24.dp)
                    )
                }
            },
            isError = newPasswordError,
            readOnly = isLoading || isSuccess
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it; confirmPasswordError = false; generalError = null },
            label = { Text("Confirm New Password") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            isError = confirmPasswordError,
            readOnly = isLoading || isSuccess
        )

        generalError?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                currentPasswordError = currentPassword.isBlank()
                newPasswordError = newPassword.isBlank()
                confirmPasswordError = confirmPassword.isBlank()
                generalError = null

                if (currentPasswordError || newPasswordError || confirmPasswordError) {
                    generalError = "All fields are required."
                    return@Button
                }
                if (newPassword != confirmPassword) {
                    generalError = "New passwords do not match."
                    newPasswordError = true
                    confirmPasswordError = true
                    return@Button
                }

                val (isValid, errorMsg) = isValidPassword(newPassword)
                if (!isValid) {
                    generalError = errorMsg
                    newPasswordError = true
                    return@Button
                }

                isLoading = true
                scope.launch {
                    val success = loginViewModel.changeUserPassword(currentPassword, newPassword)
                    if (success) {
                        isSuccess = true
                    } else {
                        isLoading = false
                        generalError = "Failed to change password. Check your current password."
                        currentPasswordError = true
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading && !isSuccess,
            colors = ButtonDefaults.buttonColors(
                disabledContainerColor = if (isSuccess) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                disabledContentColor = if (isSuccess) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            )
        ) {
            AnimatedContent(
                targetState = when {
                    isSuccess -> "Success"
                    isLoading -> "Loading"
                    else -> "Idle"
                },
                transitionSpec = {
                    (fadeIn() + scaleIn()).togetherWith(fadeOut() + scaleOut())
                },
                label = "Password Button Animation"
            ) { state ->
                when (state) {
                    "Success" -> Icon(Icons.Default.Check, contentDescription = "Success")
                    "Loading" -> CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = LocalContentColor.current,
                        strokeWidth = 2.dp
                    )
                    else -> Text("Update Password")
                }
            }
        }
    }
}

@Composable
private fun ChangeEmailSheet(
    loginViewModel: LoginRegistryViewModel,
    onDismiss: () -> Unit
) {
    var currentPassword by remember { mutableStateOf("") }
    val user by loginViewModel.loggedInUser.collectAsState()
    var email by remember { mutableStateOf("") }
    var showCurrentPass by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    var passwordError by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf(false) }
    var generalError by remember { mutableStateOf<String?>(null) }

    var isLoading by remember { mutableStateOf(false) }
    var isSuccess by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf("") }

    LaunchedEffect(isSuccess) {
        if (isSuccess) {
            delay(1500)
            onDismiss()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.ime)
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (user?.email != null) {
            Text("Delete Email", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "You currently have ${user?.email} linked to your account. " +
                        "To add a new email, you must first delete the current one by confirming your password.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = currentPassword,
                onValueChange = { currentPassword = it; passwordError = false; generalError = null },
                label = { Text("Current Password") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                isError = passwordError,
                readOnly = isLoading || isSuccess
            )

            generalError?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    passwordError = currentPassword.isBlank()
                    generalError = null

                    if (passwordError) {
                        generalError = "Password is required."
                        return@Button
                    }
                    isLoading = true
                    scope.launch {
                        val success = loginViewModel.deleteUserEmail(currentPassword)
                        if (success) {
                            successMessage = "Email deleted!"
                            isSuccess = true
                        } else {
                            isLoading = false
                            generalError = "Failed to delete email. Check your password."
                            passwordError = true
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    disabledContainerColor = if (isSuccess) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error.copy(alpha = 0.5f),
                    disabledContentColor = if (isSuccess) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onError.copy(alpha = 0.5f)
                ),
                enabled = !isLoading && !isSuccess
            ) {
                AnimatedContent(
                    targetState = when {
                        isSuccess -> "Success"
                        isLoading -> "Loading"
                        else -> "Idle"
                    },
                    transitionSpec = {
                        (fadeIn() + scaleIn()).togetherWith(fadeOut() + scaleOut())
                    },
                    label = "Delete Email Button Animation"
                ) { state ->
                    when (state) {
                        "Success" -> Icon(Icons.Default.Check, contentDescription = "Success")
                        "Loading" -> CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = LocalContentColor.current,
                            strokeWidth = 2.dp
                        )
                        else -> Text("Delete Email")
                    }
                }
            }

        } else {
            Text("Add Email", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Add an email to your account for recovery purposes.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it; emailError = false; generalError = null },
                label = { Text("New Email Address") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                isError = emailError,
                readOnly = isLoading || isSuccess
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = currentPassword,
                onValueChange = { currentPassword = it; passwordError = false; generalError = null },
                label = { Text("Current Password") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = if (showCurrentPass) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = { showCurrentPass = !showCurrentPass }) {
                        Icon(
                            painter = painterResource(if (showCurrentPass) R.drawable.show else R.drawable.hide),
                            contentDescription = "Show/Hide password",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                isError = passwordError,
                readOnly = isLoading || isSuccess
            )

            generalError?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val isEmailValid = email.isNotBlank() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
                    emailError = !isEmailValid
                    passwordError = currentPassword.isBlank()
                    generalError = null

                    if (emailError) {
                        generalError = "Please enter a valid email address."
                        return@Button
                    }
                    if (passwordError) {
                        generalError = "Password is required."
                        return@Button
                    }

                    isLoading = true
                    scope.launch {
                        val success = loginViewModel.addEmail(currentPassword, email)
                        if (success) {
                            successMessage = "Email added!"
                            isSuccess = true
                        } else {
                            isLoading = false
                            generalError = "Failed to add email. Check password or email may be taken."
                            passwordError = true
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading && !isSuccess,
                colors = ButtonDefaults.buttonColors(
                    disabledContainerColor = if (isSuccess) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                    disabledContentColor = if (isSuccess) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                )
            ) {
                AnimatedContent(
                    targetState = when {
                        isSuccess -> "Success"
                        isLoading -> "Loading"
                        else -> "Idle"
                    },
                    transitionSpec = {
                        (fadeIn() + scaleIn()).togetherWith(fadeOut() + scaleOut())
                    },
                    label = "Add Email Button Animation"
                ) { state ->
                    when (state) {
                        "Success" -> Icon(Icons.Default.Check, contentDescription = "Success")
                        "Loading" -> CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = LocalContentColor.current,
                            strokeWidth = 2.dp
                        )
                        else -> Text("Add Email")
                    }
                }
            }
        }
    }
}

@Composable
private fun VerifyEmailSheet(
    loginViewModel: LoginRegistryViewModel,
    onDismiss: () -> Unit,
    onError: (String) -> Unit
) {
    var code by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val user by loginViewModel.loggedInUser.collectAsState()
    val userEmail = user?.email ?: "your email"

    var codeError by remember { mutableStateOf(false) }
    var generalError by remember { mutableStateOf<String?>(null) }

    var isLoading by remember { mutableStateOf(false) }
    var isSuccess by remember { mutableStateOf(false) }

    LaunchedEffect(isSuccess) {
        if (isSuccess) {
            delay(1500)
            onDismiss()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.ime)
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Verify Email", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "We've sent a verification code to $userEmail. Please enter it below.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = code,
            onValueChange = { code = it; codeError = false; generalError = null },
            label = { Text("Verification Code") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = codeError,
            readOnly = isLoading || isSuccess
        )

        generalError?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                codeError = code.isBlank()
                generalError = null

                if (codeError) {
                    generalError = "Please enter the verification code."
                    return@Button
                }

                isLoading = true
                scope.launch {
                    val success = loginViewModel.verifyEmail(code)
                    if (success) {
                        isSuccess = true
                    } else {
                        isLoading = false
                        generalError = "Verification failed. Invalid or expired code."
                        codeError = true
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading && !isSuccess,
            colors = ButtonDefaults.buttonColors(
                disabledContainerColor = if (isSuccess) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                disabledContentColor = if (isSuccess) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            )
        ) {
            AnimatedContent(
                targetState = when {
                    isSuccess -> "Success"
                    isLoading -> "Loading"
                    else -> "Idle"
                },
                transitionSpec = {
                    (fadeIn() + scaleIn()).togetherWith(fadeOut() + scaleOut())
                },
                label = "Verify Email Button Animation"
            ) { state ->
                when (state) {
                    "Success" -> Icon(Icons.Default.Check, contentDescription = "Success")
                    "Loading" -> CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = LocalContentColor.current,
                        strokeWidth = 2.dp
                    )
                    else -> Text("Verify")
                }
            }
        }

        TextButton(
            onClick = {
                scope.launch {
                    val success = loginViewModel.getEmailVerificationCode()
                    if (success) {
                        Toast.makeText(context, "Verification code sent!", Toast.LENGTH_SHORT).show()
                    } else {
                        onError("Failed to send code. Try again later.")
                    }
                }
            }
        ) {
            Text("Send Code")
        }
    }
}

private fun isValidPassword(password: String): Pair<Boolean, String?> {
    if (password.length < 8) {
        return false to "Password must be at least 8 characters."
    }
    if (!password.any { it.isUpperCase() }) {
        return false to "Password must contain at least one uppercase letter."
    }
    if (!password.any { it.isDigit() }) {
        return false to "Password must contain at least one digit."
    }
    return true to null
}