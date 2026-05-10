package com.zst.senior.assistant.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.zst.senior.assistant.R
import com.zst.senior.assistant.model.AuthState
import com.zst.senior.assistant.ui.components.CustomPasswordTextField
import com.zst.senior.assistant.ui.components.CustomTextField
import com.zst.senior.assistant.ui.components.GradientButton
import com.zst.senior.assistant.ui.theme.AuthBackgroundGradient
import com.zst.senior.assistant.ui.theme.BrandBlue
import com.zst.senior.assistant.ui.theme.BrandOrange
import com.zst.senior.assistant.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

/**
 * Ekran rejestracji nowego użytkownika w aplikacji.
 *
 * Komponent ten dostarcza formularz umożliwiający założenie konta. Użytkownik wybiera swoją rolę
 * (Senior lub Wolontariusz) oraz podaje dane osobowe, kontaktowe i adresowe.
 * Formularz posiada podstawową walidację po stronie interfejsu (sprawdzenie, czy wszystkie pola
 * są wypełnione oraz czy hasła są identyczne).
 * * * Ekran zawiera delikatne animacje wejściowe (pojawianie się nagłówka i karty z opóźnieniem).
 * * Nasłuchuje zmian stanu autoryzacji z [AuthViewModel] – w przypadku błędów wyświetla komunikaty
 * poprzez Snackbar, natomiast w przypadku sukcesu prezentuje modalne okno dialogowe informujące
 * o konieczności weryfikacji adresu e-mail, zatrzymując automatyczne zamknięcie ekranu.
 * * Automatycznie dostosowuje kolorystykę do trybu wysokiego kontrastu (WCAG).
 *
 * @param navController Kontroler nawigacji służący do obsługi przycisku wstecz oraz
 * opuszczenia ekranu po udanej rejestracji (i zatwierdzeniu komunikatu).
 * @param authViewModel ViewModel zarządzający procesem autoryzacji i komunikacją z Firebase.
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(navController: NavController, authViewModel: AuthViewModel) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var imie by rememberSaveable { mutableStateOf("") }
    var nazwisko by rememberSaveable { mutableStateOf("") }
    var ulica by rememberSaveable { mutableStateOf("") }
    var miasto by rememberSaveable { mutableStateOf("") }
    var rola by rememberSaveable { mutableStateOf("Senior") }
    var telefon by rememberSaveable { mutableStateOf("") }

    val authState by authViewModel.authState.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current

    val isHighContrast = MaterialTheme.colorScheme.background == Color.Black

    val backgroundModifier = if (isHighContrast) {
        Modifier.background(Color.Black)
    } else {
        Modifier.background(brush = AuthBackgroundGradient) // Teraz wszystkie ekrany mają to samo tło
    }
    val topContentColor = if (isHighContrast) MaterialTheme.colorScheme.primary else Color.Black
    val cardBgColor = if (isHighContrast) Color.Black else Color.White
    val textFieldColors = getRegisterTextFieldColors(isHighContrast)

    // STANY DO ANIMACJI WEJŚCIOWYCH ORAZ DIALOGÓW
    var isVisible by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { isVisible = true }

    val headerAlpha by animateFloatAsState(targetValue = if (isVisible) 1f else 0f, animationSpec = tween(600), label = "headerAlpha")
    val cardAlpha by animateFloatAsState(targetValue = if (isVisible) 1f else 0f, animationSpec = tween(600, delayMillis = 200), label = "cardAlpha")

    // OBSŁUGA STANU AUTORYZACJI
    LaunchedEffect(authState) {
        when (val state = authState) {
            is AuthState.RegisterSuccess -> {
                // Zamiast pokazywać szybko znikający Snackbar i od razu zamykać ekran,
                // aktywujemy okno dialogowe, które czeka na reakcję użytkownika.
                showSuccessDialog = true
            }
            is AuthState.Error -> {
                scope.launch {
                    snackbarHostState.showSnackbar(state.message)
                }
                authViewModel.resetAuthState()
            }
            else -> {}
        }
    }

    // Wcześniejsze pobranie komunikatu o błędzie z hasłami
    val passwordsMismatchMessage = stringResource(R.string.register_error_passwords_mismatch)

    // OKNO DIALOGOWE PO ZAKOŃCZENIU REJESTRACJI
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { /* Celowo puste, aby wymusić kliknięcie przycisku na dole */ },
            title = {
                Text(
                    text = stringResource(R.string.register_success_title),
                    fontWeight = FontWeight.Bold,
                    color = if (isHighContrast) MaterialTheme.colorScheme.primary else Color.Black
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.register_success_message),
                    color = if (isHighContrast) Color.White else Color.DarkGray
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        authViewModel.resetAuthState()
                        navController.popBackStack()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isHighContrast) MaterialTheme.colorScheme.primary else BrandBlue
                    )
                ) {
                    Text(stringResource(R.string.register_understand), color = if (isHighContrast) Color.Black else Color.White)
                }
            },
            containerColor = cardBgColor
        )
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
                    IconButton(onClick = { navController.popBackStack() }) { // Zmiana z safePopBackStack jeśli nie zdefiniowano takiego rozszerzenia, upewnij się, że pasuje do Twojego projektu
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.register_back),
                            tint = topContentColor
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = backgroundModifier.padding(paddingValues)) {
            val isLoading = authState is AuthState.Loading

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // ANIMOWANY NAGŁÓWEK
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .alpha(headerAlpha)
                ) {
                    Text(
                        text = stringResource(R.string.register_title),
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = topContentColor
                    )
                    Text(
                        text = stringResource(R.string.register_subtitle),
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (isHighContrast) Color.White else Color.Gray,
                        modifier = Modifier.padding(bottom = 24.dp, top = 8.dp)
                    )
                }

                // ANIMOWANA KARTA FORMULARZA
                Column(
                    modifier = Modifier
                        .alpha(cardAlpha)
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(containerColor = cardBgColor),
                        elevation = CardDefaults.cardElevation(8.dp)
                    ) {
                        Column(Modifier.padding(24.dp)) {
                            // WYBÓR ROLI
                            Text(
                                text = stringResource(R.string.register_role_label),
                                style = MaterialTheme.typography.labelLarge,
                                color = if (isHighContrast) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.Start,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val radioColor = MaterialTheme.colorScheme.primary
                                val textColor = if (isHighContrast) Color.White else Color.Black

                                RadioButton(
                                    selected = rola == "Senior",
                                    onClick = { rola = "Senior" },
                                    enabled = !isLoading,
                                    colors = RadioButtonDefaults.colors(selectedColor = radioColor)
                                )
                                Text(stringResource(R.string.register_role_senior), modifier = Modifier.clickable { rola = "Senior" }, color = textColor)

                                Spacer(Modifier.width(24.dp))

                                RadioButton(
                                    selected = rola == "Wolontariusz",
                                    onClick = { rola = "Wolontariusz" },
                                    enabled = !isLoading,
                                    colors = RadioButtonDefaults.colors(selectedColor = radioColor)
                                )
                                Text(stringResource(R.string.register_role_volunteer), modifier = Modifier.clickable { rola = "Wolontariusz" }, color = textColor)
                            }

                            Spacer(Modifier.height(16.dp))

                            // POLA FORMULARZA
                            CustomTextField(value = imie, onValueChange = { imie = it }, label = stringResource(R.string.register_first_name_label), enabled = !isLoading, leadingIcon = { Icon(Icons.Default.Person, contentDescription = stringResource(R.string.register_icon_profile_desc)) }, colors = textFieldColors, keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Next))
                            Spacer(Modifier.height(12.dp))

                            CustomTextField(value = nazwisko, onValueChange = { nazwisko = it }, label = stringResource(R.string.register_last_name_label), enabled = !isLoading, leadingIcon = { Icon(Icons.Default.Person, contentDescription = stringResource(R.string.register_icon_profile_desc)) }, colors = textFieldColors, keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Next))
                            Spacer(Modifier.height(12.dp))

                            CustomTextField(value = email, onValueChange = { email = it }, label = stringResource(R.string.register_first_name_label), enabled = !isLoading, leadingIcon = { Icon(Icons.Default.Email, contentDescription = stringResource(R.string.register_icon_email_desc)) }, colors = textFieldColors, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next))
                            Spacer(Modifier.height(12.dp))

                            CustomTextField(value = telefon, onValueChange = { telefon = it }, label = stringResource(R.string.register_phone_label), enabled = !isLoading, leadingIcon = { Icon(Icons.Default.Phone, contentDescription = stringResource(R.string.register_icon_phone_desc)) }, colors = textFieldColors, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next))
                            Spacer(Modifier.height(12.dp))

                            CustomTextField(value = miasto, onValueChange = { miasto = it }, label = stringResource(R.string.register_city_label), enabled = !isLoading, leadingIcon = { Icon(Icons.Default.LocationCity, contentDescription = stringResource(R.string.register_icon_city_desc)) }, colors = textFieldColors, keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Next))
                            Spacer(Modifier.height(12.dp))

                            CustomTextField(value = ulica, onValueChange = { ulica = it }, label = stringResource(R.string.register_street_label), enabled = !isLoading, leadingIcon = { Icon(Icons.Default.Home, contentDescription = stringResource(R.string.register_icon_home_desc)) }, colors = textFieldColors, keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Next))
                            Spacer(Modifier.height(12.dp))

                            CustomPasswordTextField(value = password, onValueChange = { password = it }, label = stringResource(R.string.login_password_label), enabled = !isLoading, leadingIcon = { Icon(Icons.Default.Lock, contentDescription = stringResource(R.string.register_icon_lock_desc)) }, colors = textFieldColors, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next))
                            Spacer(Modifier.height(12.dp))

                            CustomPasswordTextField(value = confirmPassword, onValueChange = { confirmPassword = it }, label = stringResource(R.string.register_confirm_password_label), enabled = !isLoading, leadingIcon = { Icon(Icons.Default.Lock, contentDescription = stringResource(R.string.register_icon_lock_desc)) }, colors = textFieldColors, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done), keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }))
                        }
                    }

                    Spacer(Modifier.height(32.dp))

                    val allFieldsFilled = imie.isNotBlank() && nazwisko.isNotBlank() && email.isNotBlank() && ulica.isNotBlank() && miasto.isNotBlank() && password.isNotBlank() && confirmPassword.isNotBlank() && telefon.isNotBlank()

                    // PRZYCISK REJESTRACJI LUB WSKAŹNIK ŁADOWANIA
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally), color = if(isHighContrast) Color.White else BrandOrange)
                    } else {
                        GradientButton(
                            text = stringResource(R.string.register_button),
                            enabled = allFieldsFilled,
                            onClick = {
                                if (password == confirmPassword) {
                                    authViewModel.registerUser(email, password, imie, nazwisko, ulica, miasto, rola, telefon)
                                } else {
                                    scope.launch {
                                        snackbarHostState.showSnackbar(passwordsMismatchMessage)
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}

/**
 * Funkcja pomocnicza generująca zestaw kolorów dla pól tekstowych używanych w formularzu rejestracji.
 *
 * Dostosowuje paletę barw (takich jak kolory obramowania, tekstu, ikon i kursora)
 * w zależności od tego, czy aplikacja znajduje się w trybie wysokiego kontrastu.
 *
 * @param isHighContrast Flaga określająca, czy włączony jest tryb WCAG (wysoki kontrast).
 * @return [TextFieldColors] skonfigurowane do użycia w komponentach tekstowych Compose.
 */
@Composable
fun getRegisterTextFieldColors(isHighContrast: Boolean): TextFieldColors {
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
        unfocusedBorderColor = if (isHighContrast) Color.Gray else Color.Gray,
        focusedLabelColor = if (isHighContrast) inputColorHC else inputColorStd,
        unfocusedLabelColor = if (isHighContrast) inputColorHC else Color.Gray,
        focusedLeadingIconColor = if (isHighContrast) inputColorHC else inputColorStd,
        unfocusedLeadingIconColor = if (isHighContrast) inputColorHC else inputColorStd
    )
}