package com.zst.senior.assistant.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.zst.senior.assistant.R
import com.zst.senior.assistant.model.mockServices
import com.zst.senior.assistant.navigation.AppRoutes
import com.zst.senior.assistant.navigation.safePopBackStack
import com.zst.senior.assistant.ui.components.AddHelpRequestDialog
import com.zst.senior.assistant.ui.components.PermissionDisclosureDialog
import com.zst.senior.assistant.ui.components.ServiceItem
import com.zst.senior.assistant.ui.theme.AccentGradient
import com.zst.senior.assistant.ui.theme.SosRed
import com.zst.senior.assistant.ui.theme.WcagBlack
import com.zst.senior.assistant.utils.ConnectivityObserver
import com.zst.senior.assistant.viewmodel.FallDetectorViewModel
import com.zst.senior.assistant.viewmodel.HelpRequestViewModel
import com.zst.senior.assistant.viewmodel.ProfileState
import com.zst.senior.assistant.viewmodel.UserProfileViewModel

/**
 * Główny panel (Dashboard) dedykowany dla użytkowników o roli "Senior".
 *
 * Ekran ten stanowi centrum dowodzenia aplikacją z perspektywy seniora. Zapewnia dostęp do:
 * * **Szybkiego wezwania pomocy (SOS):** Przycisk alarmowy wymagający pełnych uprawnień lokalizacyjnych
 * i SMS-owych, który przenosi do dedykowanego ekranu kryzysowego.
 * * **Modułu detekcji upadków:** Automatycznie uruchamia śledzenie upadków w tle (`FallDetectorViewModel`),
 * jeśli przyznano odpowiednie uprawnienia.
 * * **Listy kategorii pomocy:** Pozwala na wybranie rodzaju wsparcia (np. zakupy, sprzątanie)
 * i wywołanie okna dialogowego w celu dodania zgłoszenia, do którego dołączana jest aktualna lokalizacja.
 *
 * **Zarządzanie uprawnieniami:**
 * Komponent implementuje złożony proces uzyskiwania uprawnień (Location, Background Location, SMS).
 * Wykorzystuje [PermissionDisclosureDialog] do wyjaśnienia użytkownikowi, dlaczego te uprawnienia
 * są krytyczne. Ponadto nasłuchuje cyklu życia aplikacji (`LifecycleEventObserver`), aby zaktualizować
 * status, gdy użytkownik wróci z ustawień systemowych.
 *
 * @param navController Kontroler nawigacji służący do przechodzenia do ekranu SOS lub cofania się.
 * @param helpRequestViewModel ViewModel zarządzający dodawaniem nowych próśb o pomoc.
 * @param userProfileViewModel ViewModel zarządzający profilem (używany do pobrania numeru telefonu opiekuna).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeniorDashboardScreen(
    navController: NavController,
    helpRequestViewModel: HelpRequestViewModel,
    userProfileViewModel: UserProfileViewModel
) {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    var showAddRequestDialog by rememberSaveable { mutableStateOf(false) }
    var selectedServiceTitleRes by rememberSaveable { mutableIntStateOf(0) }

    var showProminentDisclosure by remember { mutableStateOf(false) }
    var showPermissionDialog by remember { mutableStateOf(false) }

    // --- OBSERWACJA INTERNETU ---
    val connectivityObserver = remember { ConnectivityObserver(context) }
    val isConnected by connectivityObserver.isConnected.collectAsState(initial = true)
    var showNoInternetDialog by remember { mutableStateOf(false) }

    LaunchedEffect(isConnected) {
        if (!isConnected) {
            showNoInternetDialog = true
        }
    }

    var seniorLocation by remember { mutableStateOf<Location?>(null) }
    var isLocationLoading by remember { mutableStateOf(false) }
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val application = LocalContext.current.applicationContext as Application
    val fallDetectorViewModel: FallDetectorViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return FallDetectorViewModel(application) as T
            }
        }
    )
    val profileState by userProfileViewModel.profileState.collectAsState()

    // --- BEZPIECZNE URUCHAMIANIE SERWISU ---
    fun startFallDetectionSafely() {
        fallDetectorViewModel.startMonitoring()
    }

    @SuppressLint("MissingPermission")
    fun updateSeniorLocation() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            isLocationLoading = true
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener { loc ->
                    seniorLocation = loc
                    isLocationLoading = false
                }
                .addOnFailureListener {
                    isLocationLoading = false
                }
        }
    }

    LaunchedEffect(profileState) {
        if (profileState is ProfileState.Success) {
            val data = (profileState as ProfileState.Success).data
            val nrOpiekuna = data["OpiekunNumer"] as? String
            if (!nrOpiekuna.isNullOrBlank()) {
                fallDetectorViewModel.setGuardianPhoneNumber(nrOpiekuna)
            }
        }
    }

    fun checkAllPermissions(): Boolean {
        val sms = ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED
        val loc = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val bgLoc = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
        } else true
        return sms && loc && bgLoc
    }

    val backgroundPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            updateSeniorLocation()
            startFallDetectionSafely() // Odpalamy dopiero po zgodzie na lokalizację w tle!
        } else {
            showPermissionDialog = true
        }
    }

    val foregroundPermissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val smsGranted = permissions[Manifest.permission.SEND_SMS] ?: false
        val locGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false

        if (smsGranted && locGranted) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val bgLocGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
                if (!bgLocGranted) {
                    backgroundPermissionLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                } else {
                    updateSeniorLocation()
                    startFallDetectionSafely()
                }
            } else {
                updateSeniorLocation()
                startFallDetectionSafely()
            }
        } else {
            showPermissionDialog = true
        }
    }

    LaunchedEffect(Unit) {
        if (!checkAllPermissions()) {
            showProminentDisclosure = true
        } else {
            updateSeniorLocation()
            startFallDetectionSafely() // Odpalamy od razu, jeśli apka już ma uprawnienia
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                if (checkAllPermissions()) {
                    showPermissionDialog = false
                    updateSeniorLocation()
                    startFallDetectionSafely() // Odpalamy gdy user wróci z ustawień systemu
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val isHighContrast = MaterialTheme.colorScheme.background == WcagBlack
    val primaryColor = MaterialTheme.colorScheme.primary
    val backgroundModifier = Modifier.fillMaxSize().then(
        if (isHighContrast) Modifier.background(Color.Black) else Modifier.background(AccentGradient)
    )
    val contentColor = if (isHighContrast) primaryColor else Color.White
    val sosGradient = Brush.verticalGradient(colors = listOf(Color(0xFFEF5350), SosRed, Color(0xFFB71C1C)))

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.senior_dashboard_title), fontWeight = if (isHighContrast) FontWeight.Black else FontWeight.Bold, color = contentColor) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent, navigationIconContentColor = contentColor),
                navigationIcon = {
                    IconButton(onClick = { navController.safePopBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.common_back))
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = backgroundModifier.padding(paddingValues)) {
            Column(modifier = Modifier.fillMaxSize()) {
                // --- PRZYCISK SOS ---
                Card(
                    modifier = Modifier.fillMaxWidth().padding(16.dp).height(110.dp)
                        .clickable {
                            if (checkAllPermissions()) {
                                navController.navigate(AppRoutes.SOS_EMERGENCY)
                            } else {
                                showProminentDisclosure = true
                            }
                        },
                    shape = RoundedCornerShape(24.dp),
                    border = if (isHighContrast) BorderStroke(2.dp, Color.White) else null,
                    elevation = CardDefaults.cardElevation(10.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize().background(sosGradient), contentAlignment = Alignment.Center) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                            Surface(color = Color.White.copy(alpha = 0.2f), shape = CircleShape, modifier = Modifier.size(64.dp)) {
                                Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.NotificationsActive, stringResource(R.string.senior_dashboard_sos_desc), Modifier.size(32.dp), tint = Color.White) }
                            }
                            Spacer(Modifier.width(20.dp))
                            Column {
                                Text(stringResource(R.string.common_sos), color = Color.White, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Black)
                                Text(stringResource(R.string.senior_dashboard_sos_call), color = Color.White.copy(alpha = 0.9f), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                Text(stringResource(R.string.senior_dashboard_categories), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = contentColor, modifier = Modifier.padding(horizontal = 24.dp))
                Spacer(Modifier.height(12.dp))

                // --- LISTA KATEGORII POMOCY ---
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(mockServices) { service ->
                        if (isHighContrast) {
                            HighContrastServiceTile(stringResource(service.titleRes), service.icon, primaryColor) {
                                selectedServiceTitleRes = service.titleRes
                                showAddRequestDialog = true
                            }
                        } else {
                            ServiceItem(service) {
                                selectedServiceTitleRes = service.titleRes
                                showAddRequestDialog = true
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddRequestDialog) {
        AddHelpRequestDialog(
            serviceTitleRes = selectedServiceTitleRes,
            onDismiss = { showAddRequestDialog = false },
            helpRequestViewModel = helpRequestViewModel,
            seniorLocation = seniorLocation,
            isLocationLoading = isLocationLoading
        )
    }

    if (showProminentDisclosure) {
        PermissionDisclosureDialog(
            onAccept = {
                showProminentDisclosure = false
                foregroundPermissionsLauncher.launch(
                    arrayOf(Manifest.permission.SEND_SMS, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                )
            },
            onDecline = {
                showProminentDisclosure = false
                showPermissionDialog = true
            }
        )
    }

    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { },
            icon = { Icon(Icons.Default.Settings, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text(stringResource(R.string.senior_dashboard_perm_config_title)) },
            text = {
                Column {
                    Text(stringResource(R.string.senior_dashboard_perm_config_message))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(stringResource(R.string.senior_dashboard_perm_config_hint))
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    }
                ) {
                    Text(stringResource(R.string.senior_dashboard_open_settings))
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showPermissionDialog = false }) {
                    Text(stringResource(R.string.senior_dashboard_cancel_sos))
                }
            }
        )
    }

    if (showNoInternetDialog) {
        AlertDialog(
            onDismissRequest = { showNoInternetDialog = false },
            icon = { Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = SosRed) },
            title = { Text(stringResource(R.string.senior_dashboard_no_internet_title)) },
            text = {
                Text(stringResource(R.string.senior_dashboard_no_internet_message))
            },
            confirmButton = {
                Button(onClick = { showNoInternetDialog = false }) {
                    Text(stringResource(R.string.common_understand))
                }
            }
        )
    }
}

/**
 * Specjalny wariant kafelka usługi/pomocy zaprojektowany z myślą o osobach
 * z wadami wzroku, korzystający ze schematu wysokiego kontrastu (WCAG).
 *
 * Renderuje wyraźne obramowanie, czarne tło i neonowy główny kolor tekstu/ikon,
 * co ułatwia dostrzeżenie i nawigację po liście dostępnych form pomocy.
 *
 * @param title Nazwa kategorii pomocy (np. "Zrób mi zakupy").
 * @param icon Ikona reprezentująca daną kategorię.
 * @param primaryColor Główny, wyrazisty kolor używany do obramowań i tekstu (w trybie HC).
 * @param onClick Funkcja wywoływana po kliknięciu w kafelek.
 */
@Composable
fun HighContrastServiceTile(title: String, icon: ImageVector, primaryColor: Color, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().height(80.dp).border(2.dp, primaryColor, RoundedCornerShape(16.dp)).clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = CircleShape, color = Color.Black, border = BorderStroke(2.dp, primaryColor), modifier = Modifier.size(48.dp)) {
                    Box(contentAlignment = Alignment.Center) { Icon(icon, null, tint = primaryColor, modifier = Modifier.size(24.dp)) }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = primaryColor)
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = primaryColor)
        }
    }
}