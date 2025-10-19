package com.example.gymappfrontendui.screens
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
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
fun RegisterScreen(navController: NavController,mainViewModel: LoginRegistryViewModel)
{

    var login by remember {
        mutableStateOf("")
    }
    var password by remember {
        mutableStateOf("")
    }
    var confirmPassword by remember {
        mutableStateOf("")
    }
    var showError by remember {
        mutableStateOf(false) }

    val passwordsNotMatching = password != confirmPassword

    val passwordTooWeak = !isValidPassword(password)

    var showPassword by remember {
        mutableStateOf(false)
    }
    var showConfirmPassword by remember {
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

        Text("Rejestracja", fontSize = 38.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(4.dp))

        Text("Wpisz poniżej swoje dane do rejestracji")

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(value = login, onValueChange = {
            login = it
        }, label = { Text("Login") },
            leadingIcon = {
                Icon( Icons.Rounded.AccountCircle,
                    contentDescription = "Login icon")
            },
            isError = showError && isEmpty(login)
        )
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
            isError = showError && passwordTooWeak,
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

        if(showError && passwordTooWeak){
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Hasło musi mieć min. 8 znaków, 1 dużą literę i cyfrę",
                color = Color.Red,
                fontSize = 12.sp
)}
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(value = confirmPassword, onValueChange = {
            confirmPassword = it
        }, label = { Text("Powtórz hasło") },
            isError = showError && passwordsNotMatching,
            leadingIcon = {
                Icon( Icons.Rounded.Lock,
                    contentDescription = "Login icon")
            },
            visualTransformation = if(showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val image = if (showConfirmPassword)
                    painterResource(id = R.drawable.show)
                else
                    painterResource(id = R.drawable.hide)

                Icon(
                    painter = image,
                    contentDescription = "Password visibility icon",
                    modifier = Modifier.clickable{
                        showConfirmPassword = !showConfirmPassword
                    }.size(24.dp)
                )

            }
        )
        if (showError && passwordsNotMatching) {
            Text(
                text = "Hasła nie są takie same",
                color = Color.Red,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(onClick = {
            showError = true
            if(!passwordsNotMatching && !passwordTooWeak && !isEmpty(login)){
              //  mainViewModel.register(username = login, password = password)
                
                navController.navigate("Login_Page")
            }

        }) {
            Text("Zarejestruj się")
        }

        TextButton(onClick = {navController.navigate("Login_Page")}) {
            Text("Powrót do logowania")
        }
    }
}
fun isValidPassword(password: String): Boolean {
    if(password.length < 8){
        return false
    }
    if (!password.any { it.isUpperCase() }) return false

    if (!password.any { it.isDigit() }) return false

    return true
}
