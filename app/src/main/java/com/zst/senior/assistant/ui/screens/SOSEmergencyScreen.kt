package com.zst.senior.assistant.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.navigation.NavController
import com.zst.senior.assistant.R
import com.zst.senior.assistant.navigation.safePopBackStack
import com.zst.senior.assistant.utils.getCurrentLocationSafely
import com.zst.senior.assistant.viewmodel.FallDetectorViewModel
import com.zst.senior.assistant.viewmodel.ProfileState
import com.zst.senior.assistant.viewmodel.UserProfileViewModel
import kotlinx.coroutines.launch

/**
 * Ekran alarmowy (SOS) uruchamiany ręcznie przez użytkownika lub automatycznie przez detektor upadku.
 *
 * Głównym zadaniem tego ekranu jest umożliwienie seniorowi szybkiego wezwania pomocy.
 * Ekran realizuje następujące funkcje:
 * 1. Wyświetla pulsującą animację ostrzegawczą, zwracającą uwagę na tryb alarmowy.
 * 2. Pobiera profil użytkownika w celu uzyskania awaryjnego adresu i numeru telefonu do opiekuna.
 * 3. Prosi o uprawnienia do lokalizacji (GPS) i próbuje ustalić dokładne współrzędne seniora.
 * 4. Buduje wiadomość SMS zawierającą lokalizację (adres GPS z linkiem do mapy lub adres z profilu).
 * 5. Umożliwia natychmiastowe połączenie ze służbami ratunkowymi (112) oraz wysłanie SMS-a do opiekuna.
 * 6. Pozwala na anulowanie alarmu, jeśli został wywołany przypadkowo.
 * 7. Wyświetla potwierdzenie (dialog) po pomyślnym wysłaniu wiadomości SMS.
 *
 * @param navController Kontroler nawigacji do zarządzania stosem ekranów (np. zamykanie alarmu).
 * @param fallDetectorViewModel ViewModel zarządzający statusem detekcji upadku.
 * @param userProfileViewModel ViewModel zarządzający danymi profilowymi użytkownika (numer opiekuna, adres stały).
 */

@Composable
fun SOSEmergencyScreen(
    navController: NavController,
    fallDetectorViewModel: FallDetectorViewModel,
    userProfileViewModel: UserProfileViewModel
) {
    val context = LocalContext.current // Pozostaje dla Intents, Toasts i natywnych API
    val scope = rememberCoroutineScope()

    // --- WCZEŚNIEJSZE POBRANIE ZASOBÓW TEKSTOWYCH ---
    val initialLocCalculating = stringResource(R.string.sos_loc_calculating)
    val locNoProfile = stringResource(R.string.sos_loc_no_profile)
    val locErrorFormat = stringResource(R.string.sos_loc_error)
    val locNoPermissionFormat = stringResource(R.string.sos_loc_no_permission)
    val locGpsFailedFormat = stringResource(R.string.sos_loc_gps_failed)
    val smsTemplateGpsFormat = stringResource(R.string.sos_sms_template_gps)
    val smsTemplateProfileFormat = stringResource(R.string.sos_sms_template_profile)
    val smsNoPermissionToast = stringResource(R.string.sos_sms_no_permission_toast)

    // --- STAN DLA OKNA DIALOGOWEGO POTWIERDZENIA SMS ---
    var showSmsSentDialog by remember { mutableStateOf(false) }

    // --- LOGIKA ANIMACJI (Pulsowanie) ---
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    // --- STAN DANYCH UŻYTKOWNIKA ---
    val profileState by userProfileViewModel.profileState.collectAsState()
    var carerNumber by remember { mutableStateOf("") }
    var profileAddress by remember { mutableStateOf("") }

    var userAddress by remember { mutableStateOf("") }
    var mapsLink by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        if (userAddress.isEmpty()) userAddress = initialLocCalculating
    }

    // Pobranie profilu po wejściu na ekran
    LaunchedEffect(Unit) {
        userProfileViewModel.loadProfile()
    }

    // Nasłuchiwanie zmian w profilu użytkownika
    LaunchedEffect(profileState) {
        if (profileState is ProfileState.Success) {
            val data = (profileState as ProfileState.Success).data
            carerNumber = data["OpiekunNumer"] as? String ?: ""

            // Pobieranie ulicy i miasta zamiast nieistniejącego pola "Adres"
            val ulica = data["Ulica"] as? String ?: ""
            val miasto = data["Miasto"] as? String ?: ""

            profileAddress = if (ulica.isNotBlank() || miasto.isNotBlank()) {
                listOf(ulica, miasto).filter { it.isNotBlank() }.joinToString(", ")
            } else {
                locNoProfile
            }
        }
    }

    // --- OBSŁUGA UPRAWNIEŃ I LOKALIZACJI ---
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
            val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

            if (fineGranted || coarseGranted) {
                scope.launch {
                    val loc = getCurrentLocationSafely(context)
                    if (loc != null) {
                        userAddress = loc.address
                        mapsLink = loc.mapsLink
                    } else {
                        userAddress = locErrorFormat.format(profileAddress)
                    }
                }
            } else {
                userAddress = locNoPermissionFormat.format(profileAddress)
            }
        }
    )

    // Automatyczne sprawdzanie lokalizacji przy starcie ekranu
    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            val loc = getCurrentLocationSafely(context)
            if (loc != null) {
                userAddress = loc.address
                mapsLink = loc.mapsLink
            } else {
                userAddress = locGpsFailedFormat.format(profileAddress)
            }
        } else {
            locationPermissionLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            )
        }
    }

    // Budowanie treści wiadomości SMS na podstawie dostępności linku do mapy
    val smsMessage = if (mapsLink.isNotBlank()) {
        smsTemplateGpsFormat.format(userAddress, mapsLink)
    } else {
        smsTemplateProfileFormat.format(profileAddress)
    }

    // --- OBSŁUGA UPRAWNIEŃ SMS ---
    val smsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                val success = sendSmsSafely(context, carerNumber, smsMessage)
                if (success) {
                    showSmsSentDialog = true
                }
            } else {
                Toast.makeText(context, smsNoPermissionToast, Toast.LENGTH_LONG).show()
            }
        }
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.error
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // SEKCJA WIZUALNA (Napisy i pulsująca ikona)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = stringResource(R.string.sos_title),
                        style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Black),
                        color = MaterialTheme.colorScheme.onError,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = stringResource(R.string.sos_subtitle),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onError.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(32.dp))

                    Box(contentAlignment = Alignment.Center) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.onError.copy(alpha = 0.2f),
                            modifier = Modifier
                                .size(180.dp)
                                .scale(scale) // Zastosowanie animacji pulsowania
                        ) {}

                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.onError,
                            modifier = Modifier.size(120.dp),
                            shadowElevation = 12.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(64.dp)
                                )
                            }
                        }
                    }
                }

                // SEKCJA INFORMACYJNA (Adres i współrzędne)
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = stringResource(R.string.helper_dashboard_loc_icon_desc),
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text(
                                text = stringResource(R.string.sos_loc_label),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                            )
                            Text(
                                text = userAddress,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontWeight = FontWeight.Bold,
                                maxLines = 4
                            )
                        }
                    }
                }

                // SEKCJA AKCJI (Przyciski kontrolne)
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL).apply { data = "tel:112".toUri() }
                            context.startActivity(intent)
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.onError,
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(72.dp),
                        elevation = ButtonDefaults.buttonElevation(8.dp)
                    ) {
                        Icon(Icons.Default.Phone, contentDescription = stringResource(R.string.sos_call_112), modifier = Modifier.size(32.dp))
                        Spacer(Modifier.width(16.dp))
                        Column(horizontalAlignment = Alignment.Start) {
                            Text(stringResource(R.string.sos_call_112), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                            Text(stringResource(R.string.sos_emergency_services), style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    // Przycisk: Wyślij SMS do opiekuna
                    Button(
                        onClick = {
                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
                                val success = sendSmsSafely(context, carerNumber, smsMessage)
                                if (success) {
                                    showSmsSentDialog = true
                                }
                            } else {
                                smsPermissionLauncher.launch(Manifest.permission.SEND_SMS)
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = carerNumber.isNotBlank()
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Message, contentDescription = stringResource(R.string.sos_sms_to_carer))
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = if (carerNumber.isNotBlank()) stringResource(R.string.sos_sms_to_carer) else stringResource(R.string.sos_no_carer_number),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Przycisk: Odwołanie alarmu
                    OutlinedButton(
                        onClick = {
                            fallDetectorViewModel.cancelAlarm()
                            navController.safePopBackStack()
                        },
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(2.dp, MaterialTheme.colorScheme.onError.copy(alpha = 0.5f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onError),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.sos_cancel_alarm))
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.sos_cancel_alarm), fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // --- OKNO DIALOGOWE POTWIERDZENIA SMS ---
            if (showSmsSentDialog) {
                AlertDialog(
                    onDismissRequest = { showSmsSentDialog = false },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        )
                    },
                    title = {
                        Text(
                            text = stringResource(R.string.sos_sms_sent_title),
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    text = {
                        Text(
                            text = stringResource(R.string.sos_sms_sent_message),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = { showSmsSentDialog = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.sos_understand))
                        }
                    }
                )
            }
        }
    }
}

/**
 * Pomocnicza funkcja bezpiecznie wysyłająca wiadomość SMS do podanego numeru telefonu.
 * Opakowuje rzeczywistą metodę wysyłającą w blok try-catch, aby zapobiec awarii aplikacji
 * w przypadku wystąpienia błędu z uprawnieniami, braku karty SIM lub nieprawidłowego numeru.
 *
 * @param context Kontekst Androida wykorzystywany do wyświetlenia komunikatu [Toast] w razie błędu.
 * @param number Numer telefonu odbiorcy, do którego ma zostać wysłana wiadomość.
 * @param message Treść wiadomości tekstowej, która zostanie przesłana.
 * @return Wartość [Boolean] oznaczająca, czy wiadomość została przekazana do systemu bez błędu.
 */
private fun sendSmsSafely(context: Context, number: String, message: String): Boolean {
    return try {
        com.zst.senior.assistant.utils.sendSms(context, number, message)
        true
    } catch (e: Exception) {
        Toast.makeText(context, "Błąd inicjalizacji SMS: ${e.message}", Toast.LENGTH_LONG).show()
        false
    }
}