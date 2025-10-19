package com.example.gymappfrontendui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.gymappfrontendui.R
import com.example.gymappfrontendui.viewmodel.LoginRegistryViewModel


@Composable
fun LoginScreen(navController: NavController, mainViewModel : LoginRegistryViewModel)
{

    val loggedInUser by mainViewModel.loggedInUser.collectAsState(initial = null)

    ///zmienne

    var login by remember {
        mutableStateOf("")
    }
    var password by remember {
        mutableStateOf("")
    }

    var showPassword by remember {
        mutableStateOf(false)
    }

    var showError by remember {
        mutableStateOf(false) }

    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.biceps),
            contentDescription = "Login image",
            modifier = Modifier.size(160.dp)
        )

        Text("Witaj!", fontSize = 28.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(4.dp))

        Text("Zaloguj się do swojego konta")

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(value = login, onValueChange = {
            login = it
        }, label = { Text("Login") },
            leadingIcon = {
                Icon( Icons.Rounded.AccountCircle,
                    contentDescription = "Login icon")
            },
            isError = showError && isEmpty(login))
        Spacer(modifier = Modifier.height(12.dp))
        if (showError && isEmpty(login)) {
            Text(
                text = "Login jest wymagany!",
                color = Color.Red,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(value = password, onValueChange = {
            password = it
        }, label = { Text("Hasło") },
            isError = showError && isEmpty(password),
            leadingIcon = {
                Icon( Icons.Rounded.Lock,
                    contentDescription = "Login icon")
            },
            visualTransformation = if(showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val image = if (showPassword)
                    painterResource(id = R.drawable.show)
                else
                    painterResource(id = R.drawable.hide)

                Icon(
                    painter = image,
                    contentDescription = "Password visibility icon",
                    modifier = Modifier.clickable{
                        showPassword = !showPassword
                    }.size(24.dp)
                )

            }

        )
        Spacer(modifier = Modifier.height(12.dp))
        if (showError && isEmpty(password)) {
            Text(
                text = "Hasło jest wymagane!",
                color = Color.Red,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(onClick = {
            showError = true
            if(!isEmpty(login) && !isEmpty(password)){
                mainViewModel.login(login, password)

            }
        }) {
            Text("Zaloguj")
        }

        OutlinedButton(
            onClick = {
                mainViewModel.setAppHasBeenOpened()

                navController.navigate("Main_Screen/guest")
                {
                    popUpTo("Login_Page") { inclusive = true }
                }
            },
            modifier = Modifier.fillMaxWidth(0.7f),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color.Gray
            )
        ) {
            Text("Kontynuuj bez konta", fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row{
            TextButton(onClick = { }) {
                Text(text = "Zapomniałem hasła")
            }
            TextButton(onClick = { navController.navigate("Register_Page")}) {
                Text(text = "Zarejestruj się")
            }

        }
        //Text("Lub zaloguj się używając")
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = Color.LightGray)
            Text("  LUB  ", fontSize = 12.sp, color = Color.Gray)
            HorizontalDivider(modifier = Modifier.weight(1f), color = Color.LightGray)
        }

        Spacer(modifier = Modifier.height(16.dp))
        Image(
            painter = painterResource(id = R.drawable.google),
            contentDescription = "Login google image",
            modifier = Modifier.size(50.dp).clickable{
                // google logowanie
            }
        )

    }

}
fun isEmpty(text: String): Boolean {
    return text.isEmpty()
}
