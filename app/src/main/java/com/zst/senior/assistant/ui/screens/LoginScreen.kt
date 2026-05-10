package com.zst.senior.assistant.ui.screens

import android.util.Patterns
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.zst.senior.assistant.R
import com.zst.senior.assistant.model.AuthState
import com.zst.senior.assistant.navigation.AppRoutes
import com.zst.senior.assistant.navigation.safePopBackStack
import com.zst.senior.assistant.ui.components.GradientButton
import com.zst.senior.assistant.ui.theme.AuthBackgroundGradient
import com.zst.senior.assistant.ui.theme.BrandBlue
import com.zst.senior.assistant.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

/**
 * Ekran logowania użytkownika.
 *
 * Komponent ten udostępnia interfejs logowania za pomocą adresu e-mail oraz hasła.
 * Obsługuje lokalną walidację formularza (sprawdzanie formatu e-mail oraz długości hasła),
 * możliwość przełączania widoczności wpisywanego hasła oraz płynne animacje pojawiania się elementów.
 * Reaguje na stan autoryzacji z [AuthViewModel], przekierowując użytkownika
 * na odpowiedni ekran docelowy w zależności od jego roli (Senior, Wolontariusz, Admin)
 * po pomyślnym zalogowaniu, lub wyświetlając komunikaty o błędach w Snackbarze.
 * Zapewnia również wsparcie dla trybu wysokiego kontrastu.
 *
 * @param navController Kontroler nawigacji służący do przechodzenia między ekranami (np. rejestracja, odzyskiwanie hasła, panele główne).
 * @param authViewModel ViewModel zarządzający procesem uwierzytelniania i przechowujący stan logowania.
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavController,
    authViewModel: AuthViewModel
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    // NOWE: Stany do zarządzania walidacją i widocznością hasła
    var emailError by rememberSaveable { mutableStateOf<String?>(null) }
    var passwordError by rememberSaveable { mutableStateOf<String?>(null) }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    val authState by authViewModel.authState.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current

    val isHighContrast = MaterialTheme.colorScheme.background == Color.Black

    val backgroundModifier = if (isHighContrast) {
        Modifier.background(Color.Black)
    } else {
        Modifier.background(brush = AuthBackgroundGradient)
    }

    val contentColor = if (isHighContrast) MaterialTheme.colorScheme.primary else Color.Black
    val cardContainerColor = if (isHighContrast) Color.Black else Color.White
    val cardBorder = if (isHighContrast) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null

    val textFieldColors = getLoginTextFieldColors(isHighContrast)

    var isScreenVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isScreenVisible = true }

    val contentAlpha by animateFloatAsState(
        targetValue = if (isScreenVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 800),
        label = "alpha"
    )
    val contentOffsetY by animateFloatAsState(
        targetValue = if (isScreenVisible) 0f else 50f,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "offset"
    )

    LaunchedEffect(authState) {
        when (val state = authState) {
            is AuthState.LoginSuccess -> {
                val destination = when (state.role) {
                    "Senior" -> AppRoutes.SENIOR_WELCOME
                    "Wolontariusz" -> AppRoutes.WOLONTARIUSZ_WELCOME
                    "Admin" -> AppRoutes.ADMIN_WELCOME
                    else -> AppRoutes.LOGIN
                }
                navController.navigate(destination) {
                    popUpTo(AppRoutes.LOGIN) { inclusive = true }
                }
                authViewModel.resetAuthState()
            }
            is AuthState.LoginError -> {
                scope.launch {
                    snackbarHostState.showSnackbar(state.message)
                    authViewModel.resetAuthState()
                }
            }
            else -> {}
        }
    }

    // Wcześniejsze pobranie stringów błędów
    val emptyEmailErrorText = stringResource(R.string.login_error_email_empty)
    val invalidEmailErrorText = stringResource(R.string.login_error_email_invalid)
    val emptyPasswordErrorText = stringResource(R.string.login_error_password_empty)
    val shortPasswordErrorText = stringResource(R.string.login_error_password_short)

    /**
     * Funkcja walidująca formularz logowania przed wysłaniem żądania.
     * Sprawdza poprawność formatu adresu e-mail oraz minimalną długość hasła.
     * Ustawia odpowiednie komunikaty o błędach lub wywołuje akcję logowania w ViewModelu.
     */
    // NOWE: Centralna funkcja walidująca formularz logowania
    val submitAction = {
        focusManager.clearFocus()
        var isValid = true

        if (email.isBlank()) {
            emailError = emptyEmailErrorText
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailError = invalidEmailErrorText
            isValid = false
        }

        if (password.isBlank()) {
            passwordError = emptyPasswordErrorText
            isValid = false
        } else if (password.length < 6) {
            passwordError = shortPasswordErrorText
            isValid = false
        }

        if (isValid) {
            authViewModel.loginUser(email, password)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = Color.Transparent,
        modifier = Modifier.imePadding(),
        topBar = {
            TopAppBar(
                title = { },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                navigationIcon = {
                    if (navController.previousBackStackEntry?.destination?.route == AppRoutes.REGISTER) {
                        IconButton(onClick = { navController.safePopBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.login_back), tint = contentColor)
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(backgroundModifier)
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            val isLoading = authState is AuthState.Loading

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState())
                    .alpha(contentAlpha)
                    .offset(y = contentOffsetY.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (isHighContrast) Color.Black else Color.White,
                    shadowElevation = if (isHighContrast) 0.dp else 12.dp,
                    border = if (isHighContrast) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
                    modifier = Modifier.size(100.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Image(
                            painter = painterResource(id = R.drawable.icon),
                            contentDescription = stringResource(R.string.login_logo_desc),
                            modifier = Modifier.size(80.dp)
                        )
                    }
                }

                Spacer(Modifier.height(32.dp))

                Text(
                    text = stringResource(R.string.login_welcome_back),
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = contentColor
                )

                Text(
                    text = stringResource(R.string.login_subtitle),
                    style = MaterialTheme.typography.bodyLarge,
                    color = contentColor.copy(alpha = 0.9f),
                    modifier = Modifier.padding(bottom = 32.dp, top = 8.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = cardContainerColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (isHighContrast) 0.dp else 8.dp),
                    border = cardBorder
                ) {
                    Column(Modifier.padding(24.dp)) {

                        // POPRAWKA: OutlinedTextField z wbudowaną walidacją i akcjami klawiatury
                        OutlinedTextField(
                            value = email,
                            onValueChange = {
                                email = it
                                if (emailError != null) emailError = null
                            },
                            label = { Text(stringResource(R.string.login_email_label)) },
                            leadingIcon = {
                                Icon(Icons.Default.Email, contentDescription = stringResource(R.string.login_email_icon_desc))
                            },
                            enabled = !isLoading,
                            isError = emailError != null,
                            supportingText = {
                                if (emailError != null) Text(text = emailError!!, color = MaterialTheme.colorScheme.error)
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next // Klawisz "Dalej"
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) } // Przeskok do hasła
                            ),
                            colors = textFieldColors,
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(Modifier.height(8.dp))

                        // POPRAWKA: OutlinedTextField dla hasła z ikoną odkrywania
                        OutlinedTextField(
                            value = password,
                            onValueChange = {
                                password = it
                                if (passwordError != null) passwordError = null
                            },
                            label = { Text(stringResource(R.string.login_password_label)) },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = stringResource(R.string.login_password_icon_desc))
                            },
                            trailingIcon = {
                                val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                                val description = if (passwordVisible) stringResource(R.string.login_password_hide) else stringResource(R.string.login_password_show)
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(imageVector = image, contentDescription = description)
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            enabled = !isLoading,
                            isError = passwordError != null,
                            supportingText = {
                                if (passwordError != null) Text(text = passwordError!!, color = MaterialTheme.colorScheme.error)
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done // Klawisz "Gotowe/Enter"
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = { submitAction() } // Odpala logowanie
                            ),
                            colors = textFieldColors,
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = stringResource(R.string.login_forgot_password),
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isHighContrast) MaterialTheme.colorScheme.primary else Color.Gray,
                            modifier = Modifier
                                .align(Alignment.End)
                                .clickable {
                                    navController.navigate(AppRoutes.FORGOT_PASSWORD)
                                }
                                .padding(4.dp)
                        )
                    }
                }

                Spacer(Modifier.height(32.dp))

                GradientButton(
                    text = if (isLoading) stringResource(R.string.login_loading) else stringResource(R.string.login_button),
                    onClick = { submitAction() },
                    enabled = !isLoading && email.isNotBlank() && password.isNotBlank()
                )

                Spacer(Modifier.height(24.dp))

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.login_no_account),
                        style = MaterialTheme.typography.bodyLarge,
                        color = contentColor.copy(alpha = 0.8f)
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { navController.navigate(AppRoutes.REGISTER) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(28.dp),
                        border = BorderStroke(1.dp, BrandBlue)
                    ) {
                        Text(stringResource(R.string.login_register_button), color = BrandBlue, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

/**
 * Generuje i zwraca obiekt [TextFieldColors] dostosowany do motywu ekranu logowania.
 * Odpowiada za kolorystykę obramowań, tekstu, kursorów oraz ikon w zależności
 * od tego, czy włączony jest tryb wysokiego kontrastu.
 *
 * @param isHighContrast Flaga określająca, czy aplikacja działa w trybie wysokiego kontrastu.
 * @return Konfiguracja kolorów dedykowana dla komponentów OutlinedTextField.
 */
@Composable
fun getLoginTextFieldColors(isHighContrast: Boolean): TextFieldColors {
    val inputColorHC = MaterialTheme.colorScheme.secondary
    val inputColorStd = BrandBlue
    val containerColor = if (isHighContrast) Color.Black else Color.White

    return OutlinedTextFieldDefaults.colors(
        focusedContainerColor = containerColor,
        unfocusedContainerColor = containerColor,
        disabledContainerColor = containerColor,
        focusedTextColor = if (isHighContrast) inputColorHC else Color.Black,
        unfocusedTextColor = if (isHighContrast) inputColorHC else Color.Black,
        cursorColor = if (isHighContrast) inputColorHC else inputColorStd,
        focusedBorderColor = if (isHighContrast) inputColorHC else inputColorStd,
        unfocusedBorderColor = if (isHighContrast) inputColorHC else Color.Gray,
        focusedLabelColor = if (isHighContrast) inputColorHC else inputColorStd,
        unfocusedLabelColor = if (isHighContrast) inputColorHC else Color.Gray,
        focusedLeadingIconColor = if (isHighContrast) inputColorHC else inputColorStd,
        unfocusedLeadingIconColor = if (isHighContrast) inputColorHC else inputColorStd,
        focusedTrailingIconColor = if (isHighContrast) inputColorHC else inputColorStd,
        unfocusedTrailingIconColor = if (isHighContrast) inputColorHC else inputColorStd,
        errorBorderColor = MaterialTheme.colorScheme.error,
        errorLeadingIconColor = MaterialTheme.colorScheme.error,
        errorLabelColor = MaterialTheme.colorScheme.error,
        errorCursorColor = MaterialTheme.colorScheme.error
    )
}