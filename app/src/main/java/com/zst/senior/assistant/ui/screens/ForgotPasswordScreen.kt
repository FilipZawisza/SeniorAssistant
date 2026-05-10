package com.zst.senior.assistant.ui.screens

import android.util.Patterns
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.zst.senior.assistant.R
import com.zst.senior.assistant.model.AuthState
import com.zst.senior.assistant.navigation.safePopBackStack
import com.zst.senior.assistant.ui.components.GradientButton
import com.zst.senior.assistant.ui.theme.BrandBlue
import com.zst.senior.assistant.viewmodel.AuthViewModel

/**
 * Ekran odzyskiwania hasła (zapomniane hasło), pozwalający użytkownikowi na zresetowanie
 * danych logowania poprzez podanie adresu e-mail powiązanego z jego kontem.
 *
 * Komponent weryfikuje wprowadzony e-mail pod kątem poprawności formatu (wykorzystując [Patterns.EMAIL_ADDRESS]),
 * a następnie asynchronicznie deleguje proces wysyłania linku do [AuthViewModel].
 * Automatycznie obsługuje stany ładowania (UI Feedback), wyświetlanie komunikatów błędów/sukcesu w [Toast]
 * oraz zapewnia odpowiednią ergonomię poprzez automatyczne chowanie klawiatury po zatwierdzeniu wpisu.
 * Posiada pełne wsparcie dla systemowego trybu wysokiego kontrastu (WCAG).
 *
 * @param navController Kontroler nawigacji Compose, służący m.in. do bezpiecznego powrotu na poprzedni ekran po udanej akcji.
 * @param authViewModel ViewModel zarządzający stanem uwierzytelniania, wykorzystywany do zlecenia operacji `resetPassword`.
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    navController: NavController,
    authViewModel: AuthViewModel
) {
    val context = LocalContext.current
    // POPRAWKA: FocusManager do chowania klawiatury po kliknięciu Enter
    val focusManager = LocalFocusManager.current
    val authState by authViewModel.authUiState.collectAsState()

    var email by rememberSaveable { mutableStateOf("") }
    var emailError by rememberSaveable { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val isThemeHighContrast = MaterialTheme.colorScheme.background == Color.Black
    val contentColor = if (isThemeHighContrast) MaterialTheme.colorScheme.primary else Color.Black
    val fieldTextColor = if (isThemeHighContrast) Color.White else Color.Black

    // Wcześniejsze pobranie stringów dla komunikatów błędów
    val emptyEmailErrorText = stringResource(R.string.forgot_password_error_email_empty)
    val invalidEmailErrorText = stringResource(R.string.forgot_password_error_email_invalid)

    LaunchedEffect(authState) {
        when (val state = authState) {
            is AuthState.Loading -> isLoading = true
            is AuthState.Success -> {
                isLoading = false
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                navController.safePopBackStack()
                authViewModel.resetAuthState()
            }
            is AuthState.Error -> {
                isLoading = false
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                authViewModel.resetAuthState()
            }
            else -> isLoading = false
        }
    }

    val submitAction = {
        focusManager.clearFocus()
        if (email.isBlank()) {
            emailError = emptyEmailErrorText
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailError = invalidEmailErrorText
        } else {
            authViewModel.resetPassword(email)
        }
    }

    Scaffold(
        containerColor = if (isThemeHighContrast) Color.Black else Color.White,
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = { navController.safePopBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.forgot_password_back),
                            tint = contentColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(if (isThemeHighContrast) Color.Black else Color.White)
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LockReset,
                    contentDescription = "Ikona kłódki oznaczająca resetowanie hasła",
                    tint = BrandBlue,
                    modifier = Modifier.size(80.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = stringResource(R.string.forgot_password_title),
                    style = MaterialTheme.typography.headlineMedium,
                    color = contentColor,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(R.string.forgot_password_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if(isThemeHighContrast) Color.White else Color.Gray,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        if (emailError != null) emailError = null
                    },
                    label = { Text(stringResource(R.string.forgot_password_email_label)) },
                    leadingIcon = {
                        Icon(Icons.Default.Email, contentDescription = stringResource(R.string.login_email_icon_desc), tint = BrandBlue)
                    },
                    isError = emailError != null,
                    supportingText = {
                        if (emailError != null) {
                            Text(text = emailError!!, color = MaterialTheme.colorScheme.error)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = if(isThemeHighContrast) Color.Black else Color.White,
                        unfocusedContainerColor = if(isThemeHighContrast) Color.Black else Color.White,
                        focusedTextColor = fieldTextColor,
                        unfocusedTextColor = fieldTextColor,
                        focusedBorderColor = if(isThemeHighContrast) MaterialTheme.colorScheme.primary else BrandBlue,
                        unfocusedBorderColor = if(isThemeHighContrast) Color.Gray else Color.Gray,
                        errorBorderColor = MaterialTheme.colorScheme.error
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { submitAction() }
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (isLoading) {
                    CircularProgressIndicator(color = contentColor)
                } else {
                    GradientButton(
                        text = stringResource(R.string.forgot_password_button),
                        onClick = { submitAction() },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}