package com.zst.senior.assistant.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
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
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.zst.senior.assistant.R
import com.zst.senior.assistant.model.AuthState
import com.zst.senior.assistant.navigation.AppRoutes
import com.zst.senior.assistant.navigation.safePopBackStack
import com.zst.senior.assistant.ui.components.CustomPasswordTextField
import com.zst.senior.assistant.ui.components.CustomTextField
import com.zst.senior.assistant.ui.components.GradientButton
import com.zst.senior.assistant.ui.theme.WcagBlack
import com.zst.senior.assistant.viewmodel.AuthViewModel
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * Ekran panelu administratora służący do zarządzania użytkownikami o określonej roli (np. Senior, Wolontariusz).
 *
 * Komponent składa się z dwóch głównych zakładek (Tabów):
 * 1. **DODAJ**: Pozwala administratorowi na utworzenie nowego konta użytkownika. Zawiera formularz
 * wymagający podania danych osobowych, adresowych oraz hasła. Po udanej rejestracji ze względów
 * bezpieczeństwa (wymogi Firebase) administrator jest wylogowywany.
 * 2. **BLOKUJ**: Panel do nakładania i zdejmowania blokad (banów) na konta użytkowników przy użyciu
 * ich adresu e-mail. Możliwe jest zdefiniowanie czasu trwania blokady w dniach lub nałożenie jej na stałe.
 *
 * Ekran zawiera mechanizmy walidacji danych, wyświetlanie powiadomień Snackbar oraz okna dialogowe (AlertDialog)
 * zapobiegające przypadkowemu zablokowaniu lub odblokowaniu użytkownika.
 *
 * Został również w pełni dostosowany do wymogów dostępności (WCAG), oferując tryb wysokiego kontrastu (HC),
 * który automatycznie przełącza paletę barw, obramowania i wskaźniki na wersje wysokokontrastowe.
 *
 * @param navController Kontroler nawigacji wykorzystywany do cofania na poprzedni ekran oraz
 * przekierowywania do ekranu logowania po udanej rejestracji nowego użytkownika.
 * @param authViewModel ViewModel odpowiedzialny za logikę biznesową (autoryzacja, zarządzanie użytkownikami,
 * komunikacja z Firebase). Udostępnia stan operacji ([AuthState]).
 * @param userRole Tekstowa reprezentacja roli użytkownika, którym chcemy zarządzać (np. "senior", "wolontariusz").
 * Rola ta jest używana zarówno w nagłówku, jak i w procesie rejestracji.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminManageUsersScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    userRole: String,
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val authState by authViewModel.authState.collectAsState()

    // Pobranie stringów potrzebnych do walidacji wewnątrz bloków onClick
    val msgPasswordMismatch = stringResource(R.string.admin_manage_users_error_passwords_mismatch)
    val msgFieldsEmpty = stringResource(R.string.admin_manage_users_error_fields_empty)
    val msgProvideEmail = stringResource(R.string.admin_manage_users_error_provide_email)
    val msgTimeForever = stringResource(R.string.chat_time_forever).lowercase()

    // Dodawanie
    var imie by rememberSaveable { mutableStateOf("") }
    var nazwisko by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var telefon by rememberSaveable { mutableStateOf("") }
    var miasto by rememberSaveable { mutableStateOf("") }
    var ulica by rememberSaveable { mutableStateOf("") }
    var haslo by rememberSaveable { mutableStateOf("") }
    var confirmHaslo by rememberSaveable { mutableStateOf("") }

    // Banowanie
    var emailToBan by rememberSaveable { mutableStateOf("") }
    var banDurationStr by rememberSaveable { mutableStateOf("") } // puste = permanent

    var showBanDialog by remember { mutableStateOf(false) }
    var showUnbanDialog by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    val isLoading = authState is AuthState.Loading

    val displayRole = remember(userRole) {
        userRole.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    }

    val isHighContrast = MaterialTheme.colorScheme.background == WcagBlack

    val backgroundColor = if (isHighContrast) Color.Black else Color(0xFFFAFAFA)
    val topContentColor = if (isHighContrast) MaterialTheme.colorScheme.primary else Color.Black
    val inputColorHC = MaterialTheme.colorScheme.secondary
    val inputColorStd = MaterialTheme.colorScheme.primary
    val errorColor = MaterialTheme.colorScheme.error

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = if (isHighContrast) Color.Black else Color.White,
        unfocusedContainerColor = if (isHighContrast) Color.Black else Color.White,
        disabledContainerColor = if (isHighContrast) Color.Black else Color.White,
        focusedTextColor = if (isHighContrast) Color.White else Color.Black,
        unfocusedTextColor = if (isHighContrast) Color.White else Color.Black,
        cursorColor = if (isHighContrast) inputColorHC else inputColorStd,
        focusedBorderColor = if (isHighContrast) inputColorHC else inputColorStd,
        unfocusedBorderColor = if (isHighContrast) inputColorHC else Color.Gray,
        focusedLabelColor = if (isHighContrast) inputColorHC else inputColorStd,
        unfocusedLabelColor = if (isHighContrast) inputColorHC else Color.Gray,
        focusedLeadingIconColor = if (isHighContrast) inputColorHC else inputColorStd,
        unfocusedLeadingIconColor = if (isHighContrast) inputColorHC else inputColorStd
    )

    LaunchedEffect(authState) {
        when (val state = authState) {
            is AuthState.Success -> {
                scope.launch { snackbarHostState.showSnackbar(state.message) }
                authViewModel.resetAuthState()
                imie = ""; nazwisko = ""; email = ""; telefon = ""; miasto = ""; ulica = ""; haslo = ""; confirmHaslo = ""
                emailToBan = ""; banDurationStr = ""
            }
            is AuthState.Error -> {
                scope.launch { snackbarHostState.showSnackbar(state.message) }
                authViewModel.resetAuthState()
            }
            is AuthState.RegisterSuccess -> {
                showSuccessDialog = true
            }
            else -> { }
        }
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text(stringResource(R.string.admin_manage_users_success_title)) },
            text = { Text(stringResource(R.string.admin_manage_users_success_message)) },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        navController.navigate(AppRoutes.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                ) {
                    Text(stringResource(R.string.register_understand))
                }
            }
        )
    }

    if (showBanDialog) {
        AlertDialog(
            onDismissRequest = { showBanDialog = false },
            icon = { Icon(Icons.Default.Block, null, tint = errorColor) },
            title = { Text(stringResource(R.string.admin_manage_users_ban_confirm_title)) },
            text = {
                val durationText = if (banDurationStr.isBlank()) {
                    msgTimeForever
                } else {
                    stringResource(R.string.chat_time_days, banDurationStr.toInt())
                }
                Text(stringResource(R.string.admin_manage_users_ban_confirm_message, emailToBan, durationText))
            },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = errorColor),
                    onClick = {
                        val days = banDurationStr.toIntOrNull()
                        authViewModel.banUserByEmail(emailToBan, days)
                        showBanDialog = false
                    }
                ) {
                    Text(stringResource(R.string.admin_manage_users_button_ban), color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBanDialog = false }) {
                    Text(stringResource(R.string.profile_cancel))
                }
            }
        )
    }

    if (showUnbanDialog) {
        AlertDialog(
            onDismissRequest = { showUnbanDialog = false },
            icon = { Icon(Icons.Default.LockOpen, null, tint = inputColorStd) },
            title = { Text(stringResource(R.string.admin_manage_users_unban_confirm_title)) },
            text = { Text(stringResource(R.string.admin_manage_users_unban_confirm_message, emailToBan)) },
            confirmButton = {
                Button(
                    onClick = {
                        authViewModel.unbanUserByEmail(emailToBan)
                        showUnbanDialog = false
                    }
                ) {
                    Text(stringResource(R.string.admin_manage_users_button_unban))
                }
            },
            dismissButton = {
                TextButton(onClick = { showUnbanDialog = false }) {
                    Text(stringResource(R.string.profile_cancel))
                }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize().background(backgroundColor)) {
        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(R.string.admin_manage_users_new_user_title, displayRole),
                            color = topContentColor,
                            fontWeight = if (isHighContrast) FontWeight.Black else FontWeight.Bold
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        navigationIconContentColor = topContentColor,
                        actionIconContentColor = topContentColor,
                        titleContentColor = topContentColor
                    ),
                    navigationIcon = {
                        IconButton(onClick = { navController.safePopBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.admin_manage_users_back))
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                SecondaryTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = topContentColor,
                    indicator = {
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(selectedTab),
                            color = topContentColor
                        )
                    },
                    divider = { HorizontalDivider(color = topContentColor.copy(alpha = 0.3f)) }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text(stringResource(R.string.admin_manage_users_tab_add), fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text(stringResource(R.string.admin_manage_users_tab_block), fontWeight = FontWeight.Bold) }
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp)
                ) {
                    if (selectedTab == 0) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(
                                    if (isHighContrast) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(24.dp))
                                    else Modifier
                                ),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isHighContrast) Color.Black else Color.White
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text(
                                    stringResource(R.string.admin_manage_users_new_user_form_title, displayRole),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isHighContrast) Color.White else inputColorStd
                                )

                                CustomTextField(
                                    value = imie,
                                    onValueChange = { imie = it },
                                    label = stringResource(R.string.admin_manage_users_first_name_label),
                                    leadingIcon = { Icon(Icons.Default.Person, null) },
                                    colors = textFieldColors
                                )
                                CustomTextField(
                                    value = nazwisko,
                                    onValueChange = { nazwisko = it },
                                    label = stringResource(R.string.admin_manage_users_last_name_label),
                                    leadingIcon = { Icon(Icons.Default.Person, null) },
                                    colors = textFieldColors
                                )
                                CustomTextField(
                                    value = email,
                                    onValueChange = { email = it },
                                    label = stringResource(R.string.admin_manage_users_email_label),
                                    leadingIcon = { Icon(Icons.Default.Email, null) },
                                    keyboardType = KeyboardType.Email,
                                    colors = textFieldColors
                                )
                                CustomTextField(
                                    value = telefon,
                                    onValueChange = { telefon = it },
                                    label = stringResource(R.string.admin_manage_users_phone_label),
                                    leadingIcon = { Icon(Icons.Default.Phone, null) },
                                    keyboardType = KeyboardType.Phone,
                                    colors = textFieldColors
                                )
                                CustomTextField(
                                    value = miasto,
                                    onValueChange = { miasto = it },
                                    label = stringResource(R.string.admin_manage_users_city_label),
                                    leadingIcon = { Icon(Icons.Default.LocationCity, null) },
                                    colors = textFieldColors
                                )
                                CustomTextField(
                                    value = ulica,
                                    onValueChange = { ulica = it },
                                    label = stringResource(R.string.admin_manage_users_street_label),
                                    leadingIcon = { Icon(Icons.Default.Map, null) },
                                    colors = textFieldColors
                                )

                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    color = if (isHighContrast) inputColorHC else Color.LightGray
                                )

                                CustomPasswordTextField(
                                    value = haslo,
                                    onValueChange = { haslo = it },
                                    label = stringResource(R.string.admin_manage_users_password_label),
                                    colors = textFieldColors
                                )
                                CustomPasswordTextField(
                                    value = confirmHaslo,
                                    onValueChange = { confirmHaslo = it },
                                    label = stringResource(R.string.admin_manage_users_password_confirm_label),
                                    colors = textFieldColors
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                if (isLoading) {
                                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                        CircularProgressIndicator(color = if (isHighContrast) inputColorHC else inputColorStd)
                                    }
                                } else {
                                    GradientButton(
                                        text = stringResource(R.string.admin_manage_users_button_add),
                                        onClick = {
                                            if (haslo != confirmHaslo) {
                                                scope.launch { snackbarHostState.showSnackbar(msgPasswordMismatch) }
                                            } else if (imie.isBlank() || nazwisko.isBlank() || email.isBlank() || haslo.isBlank()) {
                                                scope.launch { snackbarHostState.showSnackbar(msgFieldsEmpty) }
                                            } else {
                                                authViewModel.registerUser(
                                                    email = email,
                                                    pass = haslo,
                                                    imie = imie,
                                                    nazwisko = nazwisko,
                                                    ulica = ulica,
                                                    miasto = miasto,
                                                    rola = userRole,
                                                    telefon = telefon
                                                )
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    } else {
                        // TAB: Blokowanie
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(
                                    if (isHighContrast) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(24.dp))
                                    else Modifier
                                ),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isHighContrast) Color.Black else Color.White
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text(
                                    stringResource(R.string.admin_manage_users_manage_bans_title),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isHighContrast) Color.White else inputColorStd
                                )

                                CustomTextField(
                                    value = emailToBan,
                                    onValueChange = { emailToBan = it },
                                    label = stringResource(R.string.admin_manage_users_email_to_ban_label),
                                    leadingIcon = { Icon(Icons.Default.AlternateEmail, null) },
                                    keyboardType = KeyboardType.Email,
                                    colors = textFieldColors
                                )

                                CustomTextField(
                                    value = banDurationStr,
                                    onValueChange = { if (it.all { char -> char.isDigit() }) banDurationStr = it },
                                    label = stringResource(R.string.admin_manage_users_ban_duration_label),
                                    leadingIcon = { Icon(Icons.Default.Timer, null) },
                                    keyboardType = KeyboardType.Number,
                                    colors = textFieldColors
                                )
                                Text(
                                    stringResource(R.string.admin_manage_users_ban_duration_hint),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isHighContrast) Color.White else Color.Gray
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            if (emailToBan.isBlank()) {
                                                scope.launch { snackbarHostState.showSnackbar(msgProvideEmail) }
                                            } else {
                                                showBanDialog = true
                                            }
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isHighContrast) Color.Black else errorColor,
                                            contentColor = if (isHighContrast) errorColor else Color.White
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        border = if (isHighContrast) BorderStroke(1.dp, errorColor) else null,
                                        enabled = !isLoading
                                    ) {
                                        Icon(Icons.Default.Block, null, modifier = Modifier.padding(end = 4.dp))
                                        Text(stringResource(R.string.admin_manage_users_button_ban))
                                    }

                                    Button(
                                        onClick = {
                                            if (emailToBan.isBlank()) {
                                                scope.launch { snackbarHostState.showSnackbar(msgProvideEmail) }
                                            } else {
                                                showUnbanDialog = true
                                            }
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isHighContrast) Color.Black else Color.DarkGray,
                                            contentColor = if (isHighContrast) inputColorHC else Color.White
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        border = if (isHighContrast) BorderStroke(1.dp, inputColorHC) else null,
                                        enabled = !isLoading
                                    ) {
                                        Icon(Icons.Default.LockOpen, null, modifier = Modifier.padding(end = 4.dp))
                                        Text(stringResource(R.string.admin_manage_users_button_unban))
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
        }
    }
}