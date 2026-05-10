package com.zst.senior.assistant.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.zst.senior.assistant.R
import com.zst.senior.assistant.navigation.AppRoutes
import com.zst.senior.assistant.navigation.safeNavigate
import com.zst.senior.assistant.navigation.safePopBackStack
import com.zst.senior.assistant.ui.components.CustomTextField
import com.zst.senior.assistant.ui.theme.AccentGradient
import com.zst.senior.assistant.ui.theme.SosRed
import com.zst.senior.assistant.ui.theme.WcagBlack
import com.zst.senior.assistant.ui.theme.volunteerGradient
import com.zst.senior.assistant.viewmodel.ProfileState
import com.zst.senior.assistant.viewmodel.UserProfileViewModel

/**
 * Ekran zarządzania profilem użytkownika.
 *
 * Komponent ten pozwala użytkownikowi przeglądać i edytować swoje dane osobowe, a także oferuje
 * podgląd statystyk specyficznych dla jego roli w aplikacji.
 * * **Główne funkcje:**
 * * **Dla Seniora:** Wyświetla zebrane punkty w grze "Seniordle" z możliwością przejścia do rankingu
 * oraz sekcję zarządzania kontaktem SOS do opiekuna.
 * * **Dla Wolontariusza:** Prezentuje statystyki pomagania, w tym średnią ocenę oraz ilość ukończonych zleceń.
 * * **Wspólne:** Edycja danych (imię, nazwisko, miasto, ulica, telefon, e-mail), resetowanie hasła,
 * oraz możliwość trwałego usunięcia konta (z dodatkowym oknem potwierdzenia).
 * * Ekran jest w pełni responsywny na stany ViewModelu (ładowanie, błąd, sukces) oraz zapewnia
 * wsparcie dla motywów o wysokim kontraście (WCAG) poprzez adaptacyjną zmianę palety kolorów.
 *
 * @param navController Kontroler nawigacji używany do powrotu lub przekierowań (np. ekran logowania po usunięciu konta).
 * @param userProfileViewModel ViewModel zarządzający logiką profilu (pobieranie/zapisywanie danych, akcje autoryzacyjne).
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    userProfileViewModel: UserProfileViewModel
) {
    val profileState by userProfileViewModel.profileState.collectAsState()

    // POBRANIE PUNKTÓW Z VIEWMODELU (dla Seniora)
    val punkty by userProfileViewModel.punktyGry.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    var imie by remember { mutableStateOf("") }
    var nazwisko by remember { mutableStateOf("") }
    var ulica by remember { mutableStateOf("") }
    var miasto by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var originalEmail by remember { mutableStateOf("") }
    var telefon by remember { mutableStateOf("") }
    var opiekunNumer by rememberSaveable { mutableStateOf("") }
    var isSenior by remember { mutableStateOf(false) }
    var role by remember { mutableStateOf("") }

    // Nowe zmienne dla statystyk wolontariusza
    var sredniaOcena by remember { mutableDoubleStateOf(0.0) }
    var iloscZlecen by remember { mutableIntStateOf(0) }

    val isLoading = profileState is ProfileState.Loading
    val fieldsEnabled = !isLoading

    val isHighContrast = MaterialTheme.colorScheme.background == WcagBlack
    val isBackgroundWhite = role == "Admin"

    val backgroundModifier = Modifier.fillMaxSize().then(
        if (isHighContrast) {
            Modifier.background(Color.Black)
        } else if (isBackgroundWhite) {
            Modifier.background(Color.White)
        } else {
            val brush = when (role) {
                "Senior" -> AccentGradient
                "Wolontariusz" -> volunteerGradient
                else -> AccentGradient
            }
            Modifier.background(brush)
        }
    )

    // Jasny kolor dla górnej części (nagłówek, ikona, statystyki) w trybie kontrastu
    val topContentColor = if (isHighContrast) MaterialTheme.colorScheme.primary else if (isBackgroundWhite) Color.Black else Color.White

    // Przywrócenie neonowego zielonego (secondary) dla formularzy w trybie kontrastu, przy jednoczesnym zachowaniu białego tekstu
    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = if (isHighContrast) Color.Black else Color.White,
        unfocusedContainerColor = if (isHighContrast) Color.Black else Color.White,
        disabledContainerColor = if (isHighContrast) Color.Black else Color.White,
        focusedTextColor = if (isHighContrast) Color.White else Color.Black, // Tekst wpisywany jest biały
        unfocusedTextColor = if (isHighContrast) Color.White else Color.Black,
        cursorColor = if (isHighContrast) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
        focusedBorderColor = if (isHighContrast) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
        unfocusedBorderColor = if (isHighContrast) MaterialTheme.colorScheme.secondary else Color.Gray,
        focusedLabelColor = if (isHighContrast) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
        unfocusedLabelColor = if (isHighContrast) MaterialTheme.colorScheme.secondary else Color.Gray
    )

    // POPRAWKA: Wcześniejsze pobranie stringów do użycia w LaunchedEffect
    val msgUpdated = stringResource(R.string.profile_msg_updated)
    val msgEmailVerificationSent = stringResource(R.string.profile_msg_email_verification_sent)
    val msgPasswordResetSent = stringResource(R.string.profile_msg_password_reset_sent)
    val msgNoUser = stringResource(R.string.profile_msg_no_user)
    val msgGuardianRequired = stringResource(R.string.profile_msg_guardian_required)
    val msgAccountDeleted = stringResource(R.string.profile_msg_account_deleted)

    LaunchedEffect(profileState) {
        when (val state = profileState) {
            is ProfileState.Success -> {
                val data = state.data
                imie = data["Imie"] as? String ?: ""
                nazwisko = data["Nazwisko"] as? String ?: ""
                ulica = data["Ulica"] as? String ?: ""
                miasto = data["Miasto"] as? String ?: ""
                email = data["Email"] as? String ?: ""
                originalEmail = email
                role = data["role"] as? String ?: ""
                isSenior = role == "Senior"
                telefon = data["Telefon"] as? String ?: ""
                opiekunNumer = data["OpiekunNumer"] as? String ?: ""

                // Pobieranie statystyk wolontariusza - używamy Number zeby uniknąć crasha
                sredniaOcena = (data["sredniaOcena"] as? Number)?.toDouble() ?: 0.0
                iloscZlecen = (data["iloscZlecen"] as? Number)?.toInt() ?: 0
            }
            is ProfileState.ProfileSaved -> {
                Toast.makeText(context, msgUpdated, Toast.LENGTH_SHORT).show()
                userProfileViewModel.loadProfile()
            }
            is ProfileState.EmailUpdateSuccess -> {
                Toast.makeText(context, msgEmailVerificationSent, Toast.LENGTH_LONG).show()
            }
            is ProfileState.PasswordResetSent -> {
                Toast.makeText(context, msgPasswordResetSent, Toast.LENGTH_LONG).show()
            }
            is ProfileState.Error -> {
                val msg = when(state.message) {
                    "Brak zalogowanego użytkownika." -> msgNoUser
                    "Ze względów bezpieczeństwa, numer telefonu opiekuna jest wymagany!" -> msgGuardianRequired
                    else -> state.message
                }
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            }
            is ProfileState.AccountDeleteSuccess -> {
                Toast.makeText(context, msgAccountDeleted, Toast.LENGTH_LONG).show()
                navController.navigate(AppRoutes.LOGIN) {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                }
            }
            else -> {}
        }
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text(stringResource(R.string.profile_delete_confirm_title)) },
            text = { Text(stringResource(R.string.profile_delete_confirm_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmDialog = false
                        userProfileViewModel.deleteUserAccount()
                    }
                ) {
                    Text(stringResource(R.string.profile_delete_confirm_button), color = SosRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text(stringResource(R.string.profile_cancel))
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.profile_title),
                        color = topContentColor,
                        fontWeight = if (isHighContrast) FontWeight.Black else FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    navigationIconContentColor = topContentColor,
                    actionIconContentColor = topContentColor
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.safePopBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.profile_back))
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            userProfileViewModel.saveProfile(imie, nazwisko, ulica, miasto, telefon, opiekunNumer)
                            if (email != originalEmail) {
                                userProfileViewModel.updateEmailWithVerification(email)
                            }
                        },
                        enabled = fieldsEnabled
                    ) {
                        Text(
                            stringResource(R.string.profile_save),
                            fontWeight = FontWeight.Bold,
                            color = topContentColor
                        )
                    }
                }
            )
        }
    )
    { paddingValues ->
        Box(
            modifier = backgroundModifier.padding(paddingValues)
        ) {
            when (profileState) {
                is ProfileState.Idle, is ProfileState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = topContentColor
                    )
                }
                is ProfileState.Error -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = stringResource(R.string.profile_error_title), tint = topContentColor, modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(8.dp))
                        val errorMsg = if ((profileState as ProfileState.Error).message.contains("Błąd ładowania profilu")) {
                            stringResource(R.string.profile_msg_load_error, (profileState as ProfileState.Error).message.substringAfter(": "))
                        } else {
                            (profileState as ProfileState.Error).message
                        }
                        Text(
                            text = errorMsg,
                            color = topContentColor,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                is ProfileState.Success, is ProfileState.ProfileSaved, is ProfileState.EmailUpdateSuccess, is ProfileState.PasswordResetSent, is ProfileState.AccountDeleteSuccess -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(Modifier.height(8.dp))
                        Surface(
                            shape = CircleShape,
                            color = if (isHighContrast) Color.Black else if (isBackgroundWhite) Color.LightGray.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.2f),
                            border = if (isHighContrast) BorderStroke(3.dp, MaterialTheme.colorScheme.primary) else if (isBackgroundWhite) BorderStroke(1.dp, Color.LightGray) else null,
                            modifier = Modifier.size(100.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = stringResource(R.string.profile_avatar_desc),
                                    tint = if (isHighContrast) MaterialTheme.colorScheme.primary else if (isBackgroundWhite) Color.Black else Color.White,
                                    modifier = Modifier.size(60.dp)
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))

                        if (role.isNotEmpty()) {
                            Text(
                                text = role,
                                color = topContentColor,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }

                        // --- SEKCJA DLA SENIORA (Punkty w grze) ---
                        if (isSenior) {
                            Spacer(Modifier.height(12.dp))
                            Surface(
                                color = if (isHighContrast) Color.Black else Color.White.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, if (isHighContrast) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.4f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = stringResource(R.string.profile_points_desc),
                                        tint = if (isHighContrast) MaterialTheme.colorScheme.primary else Color(0xFFFFD700),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = stringResource(R.string.profile_points_format, punkty),
                                        color = topContentColor,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }

                            Spacer(Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = { navController.safeNavigate("leaderboard") },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = topContentColor),
                                border = BorderStroke(1.dp, topContentColor)
                            ) {
                                Text(stringResource(R.string.profile_view_leaderboard))
                            }
                        }

                        // --- NOWE: SEKCJA DLA WOLONTARIUSZA (Statystyki) ---
                        if (role == "Wolontariusz") {
                            Spacer(Modifier.height(12.dp))
                            Surface(
                                color = if (isHighContrast) Color.Black else Color.White.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, if (isHighContrast) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.4f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = stringResource(R.string.profile_rating_desc),
                                        tint = if (isHighContrast) MaterialTheme.colorScheme.primary else Color(0xFFFFD700),
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Text(
                                        text = if (iloscZlecen > 0) stringResource(R.string.profile_volunteer_stats_format, sredniaOcena, iloscZlecen) else stringResource(R.string.profile_no_ratings),
                                        color = topContentColor,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                        // ----------------------------------

                        Spacer(Modifier.height(24.dp))

                        val cardShape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .then(
                                    // Zastosowanie koloru secondary (neonowego zielonego) dla obramowania karty
                                    if (isHighContrast) Modifier.border(2.dp, MaterialTheme.colorScheme.secondary, cardShape)
                                    else if (isBackgroundWhite) Modifier.border(1.dp, Color.LightGray.copy(alpha = 0.5f), cardShape)
                                    else Modifier
                                ),
                            shape = cardShape,
                            colors = CardDefaults.cardColors(
                                containerColor = if (isHighContrast) Color.Black else Color.White
                            ),
                            elevation = CardDefaults.cardElevation(8.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text(
                                    stringResource(R.string.profile_personal_data),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    // Nagłówek również w kolorze secondary
                                    color = if (isHighContrast) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface
                                )

                                CustomTextField(value = imie, onValueChange = { imie = it }, label = stringResource(R.string.profile_first_name_label), enabled = fieldsEnabled, colors = textFieldColors)
                                CustomTextField(value = nazwisko, onValueChange = { nazwisko = it }, label = stringResource(R.string.profile_last_name_label), enabled = fieldsEnabled, colors = textFieldColors)
                                CustomTextField(value = email, onValueChange = { email = it }, label = stringResource(R.string.profile_email_label), keyboardType = KeyboardType.Email, enabled = fieldsEnabled, colors = textFieldColors)
                                CustomTextField(
                                    value = telefon,
                                    onValueChange = { newValue ->
                                        // Zapisujemy tylko jeśli ma max 9 znaków i składa się wyłącznie z cyfr
                                        if (newValue.length <= 9 && newValue.all { it.isDigit() }) {
                                            telefon = newValue
                                        }
                                    },
                                    label = stringResource(R.string.profile_phone_label),
                                    keyboardType = KeyboardType.Phone,
                                    enabled = fieldsEnabled,
                                    colors = textFieldColors
                                )
                                CustomTextField(value = miasto, onValueChange = { miasto = it }, label = stringResource(R.string.profile_city_label), enabled = fieldsEnabled, colors = textFieldColors)
                                CustomTextField(value = ulica, onValueChange = { ulica = it }, label = stringResource(R.string.profile_street_label), enabled = fieldsEnabled, colors = textFieldColors)

                                if (isSenior) {
                                    Spacer(Modifier.height(8.dp))
                                    HorizontalDivider(color = if (isHighContrast) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outlineVariant)
                                    Spacer(Modifier.height(8.dp))

                                    Text(stringResource(R.string.profile_guardian_section), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = if (isHighContrast) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary)
                                    Text(stringResource(R.string.profile_guardian_subtitle), style = MaterialTheme.typography.bodySmall, color = if (isHighContrast) Color.White else MaterialTheme.colorScheme.onSurfaceVariant)
                                    CustomTextField(
                                        value = opiekunNumer,
                                        onValueChange = { newValue ->
                                            // Zapisujemy tylko jeśli ma max 9 znaków i składa się wyłącznie z cyfr
                                            if (newValue.length <= 9 && newValue.all { it.isDigit() }) {
                                                opiekunNumer = newValue
                                            }
                                        },
                                        label = stringResource(R.string.profile_guardian_phone_label),
                                        keyboardType = KeyboardType.Phone,
                                        enabled = fieldsEnabled,
                                        colors = textFieldColors
                                    )
                                }

                                Spacer(Modifier.height(8.dp))
                                HorizontalDivider(color = if (isHighContrast) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outlineVariant)
                                Spacer(Modifier.height(8.dp))

                                OutlinedButton(
                                    onClick = { userProfileViewModel.sendPasswordResetEmail() },
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = fieldsEnabled,
                                    // Guzik również w kolorze secondary dla spójności danych
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = if(isHighContrast) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary),
                                    border = BorderStroke(1.dp, if(isHighContrast) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary)
                                ) {
                                    Text(stringResource(R.string.profile_change_password))
                                }

                                Button(
                                    onClick = { showDeleteConfirmDialog = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = SosRed,
                                        contentColor = Color.White
                                    ),
                                    enabled = fieldsEnabled
                                ) {
                                    Text(stringResource(R.string.profile_delete_account))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}