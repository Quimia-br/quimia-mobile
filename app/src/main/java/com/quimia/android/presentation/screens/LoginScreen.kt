package com.quimia.android.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.quimia.android.data.auth.GoogleSignInHelper
import com.quimia.android.domain.auth.AuthResult
import com.quimia.android.presentation.auth.AuthViewModel
import com.quimia.android.utils.AnalyticsLogger
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    viewModel: AuthViewModel = viewModel()
) {
    val authState by viewModel.authState.collectAsStateWithLifecycle()
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    val googleSignInHelper = remember { GoogleSignInHelper(context) }
    var googleSignInData by remember { mutableStateOf<android.content.Intent?>(null) }
    
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        AnalyticsLogger.logGoogleSignInEvent("LAUNCHER_RESULT", "Result code: ${result.resultCode}")
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            AnalyticsLogger.logGoogleSignInEvent("LAUNCHER_RESULT", "Result OK, data: ${result.data?.data}")
            googleSignInData = result.data
        } else {
            AnalyticsLogger.logGoogleSignInEvent("LAUNCHER_RESULT", "Result NOT OK or CANCELLED")
        }
    }
    
    LaunchedEffect(googleSignInData) {
        if (googleSignInData != null) {
            AnalyticsLogger.logGoogleSignInEvent("PROCESS_DATA", "Processing sign-in data")
            try {
                val idToken = googleSignInHelper.getIdTokenFromResult(googleSignInData)
                AnalyticsLogger.logGoogleSignInEvent("PROCESS_DATA", "ID Token received: ${idToken?.substring(0, 20) ?: "NULL"}...")
                if (!idToken.isNullOrBlank()) {
                    AnalyticsLogger.logGoogleSignInEvent("PROCESS_DATA", "Calling loginWithGoogle")
                    viewModel.loginWithGoogle(idToken)
                } else {
                    AnalyticsLogger.logGoogleSignInEvent("PROCESS_DATA", "ID Token is null or blank")
                    googleSignInData = null
                }
            } catch (e: Exception) {
                AnalyticsLogger.logGoogleSignInError("PROCESS_DATA", e)
                googleSignInData = null
            }
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Quimia",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Entre na sua conta",
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF5A6470)
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("E-mail") }
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Senha") },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    TextButton(onClick = { passwordVisible = !passwordVisible }) {
                        Text(if (passwordVisible) "Ocultar" else "Mostrar")
                    }
                }
            )

            Spacer(modifier = Modifier.height(18.dp))

            when (val state = authState) {
                is AuthResult.Error -> {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                AuthResult.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                }
                is AuthResult.Success -> Unit
            }

            Button(
                onClick = { viewModel.loginWithEmail(email.trim(), password) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1DBF73))
            ) {
                Text("Entrar")
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = { viewModel.registerWithEmail(email.trim(), password) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Criar conta")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .height(1.dp)
                        .weight(1f)
                        .background(Color(0xFFD8DFEA))
                )
                Text(
                    text = " ou ",
                    modifier = Modifier.padding(horizontal = 12.dp),
                    color = Color(0xFF67748C)
                )
                Box(
                    modifier = Modifier
                        .height(1.dp)
                        .weight(1f)
                        .background(Color(0xFFD8DFEA))
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = { 
                    AnalyticsLogger.logGoogleSignInEvent("BUTTON_CLICK", "User clicked 'Continuar com Google'")
                    try {
                        val signInIntent = googleSignInHelper.getSignInIntent()
                        AnalyticsLogger.logGoogleSignInEvent("BUTTON_CLICK", "Sign-in intent created, launching...")
                        googleSignInLauncher.launch(signInIntent)
                    } catch (e: Exception) {
                        AnalyticsLogger.logGoogleSignInError("BUTTON_CLICK", e)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Continuar com Google")
            }
        }
    }
}
