package com.zst.senior.assistant.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.zst.senior.assistant.R
import com.zst.senior.assistant.model.ZlecenieRef
import com.zst.senior.assistant.navigation.AppRoutes
import com.zst.senior.assistant.navigation.safeNavigate
import com.zst.senior.assistant.navigation.safePopBackStack
import com.zst.senior.assistant.ui.components.ZlecenieItemPublic
import com.zst.senior.assistant.ui.theme.volunteerGradient
import com.zst.senior.assistant.utils.ConnectivityObserver
import com.zst.senior.assistant.viewmodel.HelpRequestViewModel
import com.zst.senior.assistant.viewmodel.ZleceniaListUiState
import java.util.Locale
import kotlin.math.*

/**
 * Określa typ sortowania listy dostępnych zleceń.
 *
 * @property android.R.attr.label Etykieta tekstowa wyświetlana w interfejsie użytkownika.
 */
enum class SortType(val labelRes: Int) {
    DISTANCE_ASC(R.string.helper_dashboard_sort_dist_asc),
    DISTANCE_DESC(R.string.helper_dashboard_sort_dist_desc),
    TIME_ASC(R.string.helper_dashboard_sort_time_asc),
    TIME_DESC(R.string.helper_dashboard_sort_time_desc)
}

/**
 * Oblicza odległość w linii prostej między dwoma punktami geograficznymi.
 *
 * Funkcja wykorzystuje wzór haversine do precyzyjnego określenia dystansu
 * na powierzchni kuli ziemskiej.
 *
 * @param lat1 Szerokość geograficzna punktu początkowego.
 * @param lon1 Długość geograficzna punktu początkowego.
 * @param lat2 Szerokość geograficzna punktu docelowego.
 * @param lon2 Długość geograficzna punktu docelowego.
 * @return Odległość między podanymi współrzędnymi wyrażona w kilometrach.
 */
fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val r = 6371.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = sin(dLat / 2).pow(2) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
    return r * 2 * atan2(sqrt(a), sqrt(1 - a))
}

/**
 * Ekran głównego pulpitu wolontariusza (pomocnika).
 *
 * Komponent ten odpowiada za wyświetlanie listy dostępnych zleceń od seniorów.
 * Zarządza uprawnieniami lokalizacyjnymi, pobiera aktualną pozycję użytkownika (wolontariusza)
 * i umożliwia filtrowanie (po dystansie) oraz sortowanie widocznych zgłoszeń.
 * Reaguje na cykl życia aplikacji, automatycznie odświeżając dane v razie potrzeby.
 *
 * @param navController Kontroler nawigacji służący do przechodzenia do szczegółów zlecenia.
 * @param helpRequestViewModel ViewModel zarządzający stanem interfejsu oraz danymi zleceń.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelperDashboardScreen(
    navController: NavController,
    helpRequestViewModel: HelpRequestViewModel
) {
    val uiState by helpRequestViewModel.zleceniaListUiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // --- OBSERWACJA INTERNETU ---
    val connectivityObserver = remember { ConnectivityObserver(context) }
    val isConnected by connectivityObserver.isConnected.collectAsState(initial = true)
    var showNoInternetDialog by remember { mutableStateOf(false) }

    LaunchedEffect(isConnected) {
        if (!isConnected) {
            showNoInternetDialog = true
        }
    }

    var locationPermissionGranted by remember { mutableStateOf(false) }
    var hasRequestedPermission by remember { mutableStateOf(false) }
    var userLocation by remember { mutableStateOf<Location?>(null) }
    var userAddress by remember { mutableStateOf<String?>(null) }
    var maxDistance by remember { mutableDoubleStateOf(5.0) }
    var filterMenuExpanded by remember { mutableStateOf(false) }
    var sortMenuExpanded by remember { mutableStateOf(false) }
    var currentSort by remember { mutableStateOf(SortType.DISTANCE_ASC) }

    var isListeningStarted by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val isHighContrast = MaterialTheme.colorScheme.background == Color.Black
    val contentColor = if (isHighContrast) MaterialTheme.colorScheme.primary else Color.White
    val subTitleColor = if (isHighContrast) MaterialTheme.colorScheme.onSurface else Color.White.copy(alpha = 0.8f)

    val backgroundModifier = if (isHighContrast) Modifier.background(Color.Black) else Modifier.background(volunteerGradient)

    /**
     * Aktualizuje lokalizację użytkownika i pobiera publiczne zlecenia.
     */
    @SuppressLint("MissingPermission")
    fun updateLocationAndFetch() {
        isRefreshing = true
        if (locationPermissionGranted) {
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener { loc ->
                    userLocation = loc
                    helpRequestViewModel.fetchPublicZlecenia()
                }
                .addOnFailureListener {
                    helpRequestViewModel.fetchPublicZlecenia()
                }
        } else {
            helpRequestViewModel.fetchPublicZlecenia()
        }
    }

    LaunchedEffect(uiState) {
        if (uiState !is ZleceniaListUiState.Loading && isRefreshing) {
            isRefreshing = false
        }
    }

    LaunchedEffect(userLocation) {
        userLocation?.let { loc ->
            if (!isListeningStarted) {
                helpRequestViewModel.startListeningForNearbyZlecenia(
                    context = context,
                    wolontariuszLat = loc.latitude,
                    wolontariuszLng = loc.longitude,
                    maxPromienKm = maxDistance
                )
                isListeningStarted = true
            }

            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(loc.latitude, loc.longitude, 1)
                userAddress = addresses?.firstOrNull()?.getAddressLine(0) ?: "Nieznana lokalizacja"
            } catch (_: Exception) {
                userAddress = String.format(Locale.getDefault(), "%.4f, %.4f", loc.latitude, loc.longitude)
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        locationPermissionGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        updateLocationAndFetch()
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    locationPermissionGranted = true
                    updateLocationAndFetch()
                } else if (!hasRequestedPermission) {
                    permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                    hasRequestedPermission = true
                } else {
                    helpRequestViewModel.fetchPublicZlecenia()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.helper_dashboard_title), color = contentColor, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    navigationIconContentColor = contentColor,
                    actionIconContentColor = contentColor,
                    titleContentColor = contentColor
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.safePopBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.helper_dashboard_back))
                    }
                },
                actions = {
                    IconButton(onClick = { updateLocationAndFetch() }, enabled = !isRefreshing) {
                        Icon(Icons.Default.Refresh, stringResource(R.string.helper_dashboard_refresh))
                    }
                    Box {
                        IconButton(onClick = { sortMenuExpanded = true }, enabled = !isRefreshing) {
                            Icon(Icons.AutoMirrored.Filled.Sort, stringResource(R.string.helper_dashboard_sort))
                        }
                        DropdownMenu(expanded = sortMenuExpanded, onDismissRequest = { sortMenuExpanded = false }) {
                            SortType.entries.forEach { sort ->
                                DropdownMenuItem(
                                    text = { Text(stringResource(sort.labelRes)) },
                                    // POPRAWKA: Zamiana null na "Zaznaczono opcję" dla czytników ekranu
                                    trailingIcon = if (currentSort == sort) { { Icon(Icons.Default.Check, contentDescription = stringResource(R.string.helper_dashboard_option_selected)) } } else null,
                                    onClick = {
                                        currentSort = sort
                                        sortMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    Box {
                        IconButton(onClick = { filterMenuExpanded = true }, enabled = !isRefreshing) {
                            BadgedBox(
                                badge = { Badge(containerColor = MaterialTheme.colorScheme.primary) { Text("${maxDistance.toInt()}") } }
                            ) { Icon(Icons.Default.FilterAlt, stringResource(R.string.helper_dashboard_filter_dist)) }
                        }
                        DropdownMenu(expanded = filterMenuExpanded, onDismissRequest = { filterMenuExpanded = false }) {
                            listOf(5.0, 10.0, 15.0, 20.0, 25.0).forEach { dist ->
                                DropdownMenuItem(
                                    text = { Text("${dist.toInt()} km") },
                                    // POPRAWKA: Zamiana null na "Wybrano ten dystans"
                                    trailingIcon = if (maxDistance == dist) { { Icon(Icons.Default.Check, contentDescription = stringResource(R.string.helper_dashboard_dist_selected)) } } else null,
                                    onClick = {
                                        maxDistance = dist
                                        filterMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            )
        }
    )
{ paddingValues ->
        Box(modifier = Modifier.fillMaxSize().then(backgroundModifier).padding(paddingValues)) {
            when (val state = uiState) {
                is ZleceniaListUiState.Loading -> { /* Handled by Dialog overlay */ }
                is ZleceniaListUiState.Error -> {
                    Column(modifier = Modifier.align(Alignment.Center).padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        // POPRAWKA: Dodanie opisu do ikony ostrzeżenia
                        Icon(Icons.Default.Warning, contentDescription = stringResource(R.string.helper_dashboard_error_icon_desc), tint = contentColor, modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(8.dp))
                        Text(stringResource(R.string.helper_dashboard_error_title), style = MaterialTheme.typography.titleMedium, color = contentColor, fontWeight = FontWeight.Bold)
                        Text(state.message, color = subTitleColor, textAlign = TextAlign.Center)
                    }
                }
                is ZleceniaListUiState.Success -> {
                    val wszystkieZlecenia = state.zlecenia.filterIsInstance<ZlecenieRef>()

                    wszystkieZlecenia.forEach { zlecenie ->
                        val sLat = zlecenie.seniorLat
                        val sLng = zlecenie.seniorLng
                        if (userLocation != null && sLat != null && sLng != null) {
                            zlecenie.odlegloscOdWolontariuszaKm = calculateDistance(
                                userLocation!!.latitude, userLocation!!.longitude, sLat, sLng
                            )
                        }
                    }

                    val filteredZlecenia = wszystkieZlecenia.filter { zlecenie ->
                        val dystans = zlecenie.odlegloscOdWolontariuszaKm
                        if (userLocation != null && dystans != null) dystans <= maxDistance else true
                    }.let { list ->
                        when (currentSort) {
                            SortType.DISTANCE_ASC -> list.sortedBy { it.odlegloscOdWolontariuszaKm ?: Double.MAX_VALUE }
                            SortType.DISTANCE_DESC -> list.sortedByDescending { it.odlegloscOdWolontariuszaKm ?: -1.0 }
                            SortType.TIME_ASC -> list.sortedBy { it.timestamp }
                            SortType.TIME_DESC -> list.sortedByDescending { it.timestamp }
                        }
                    }

                    Column(modifier = Modifier.fillMaxSize()) {
                        if (userLocation != null) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // POPRAWKA: Dodanie opisu ikony lokalizacji
                                Icon(Icons.Default.LocationOn, contentDescription = stringResource(R.string.helper_dashboard_loc_icon_desc), tint = contentColor, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = userAddress ?: stringResource(R.string.helper_dashboard_loc_searching),
                                    color = subTitleColor,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }

                        if (filteredZlecenia.isEmpty()) {
                            val circleBg = if (isHighContrast) Color.Black else Color.White.copy(alpha = 0.15f)
                            val circleBorder = if (isHighContrast) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null

                            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                                Surface(shape = CircleShape, color = circleBg, border = circleBorder, modifier = Modifier.size(120.dp)) {
                                    Box(contentAlignment = Alignment.Center) {
                                        // POPRAWKA: Dodanie opisu ikony wolontariatu
                                        Icon(Icons.Default.VolunteerActivism, contentDescription = stringResource(R.string.helper_dashboard_empty_icon_desc), tint = contentColor, modifier = Modifier.size(60.dp))
                                    }
                                }
                                Spacer(Modifier.height(24.dp))
                                Text(stringResource(R.string.helper_dashboard_empty_title), color = contentColor, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                Text(
                                    text = stringResource(R.string.helper_dashboard_empty_subtitle, maxDistance.toInt()),
                                    color = subTitleColor, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 32.dp, vertical = 8.dp)
                                )
                            }
                        } else {
                            LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                items(filteredZlecenia) { zlecenie ->
                                    Column {
                                        val dist = zlecenie.odlegloscOdWolontariuszaKm
                                        if (dist != null) {
                                            Text(
                                                text = stringResource(R.string.helper_dashboard_dist_format, dist, zlecenie.miasto),
                                                color = contentColor,
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
                                            )
                                        }

                                        ZlecenieItemPublic(
                                            zlecenie = zlecenie,
                                            isAdminView = false,
                                            onClick = {
                                                if (!isRefreshing) {
                                                    navController.safeNavigate("${AppRoutes.ZLECENIE_DETAILS_ROUTE}/${zlecenie.id}")
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (isRefreshing || uiState is ZleceniaListUiState.Loading) {
        Dialog(
            onDismissRequest = { },
            properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false, usePlatformDefaultWidth = false)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { }
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(64.dp), strokeWidth = 6.dp)
            }
        }
    }

    if (showNoInternetDialog) {
        AlertDialog(
            onDismissRequest = { showNoInternetDialog = false },
            icon = { Icon(Icons.Default.CloudOff, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text(stringResource(R.string.helper_dashboard_offline_title)) },
            text = {
                Text(stringResource(R.string.helper_dashboard_offline_message))
            },
            confirmButton = {
                Button(onClick = { showNoInternetDialog = false }) {
                    Text(stringResource(R.string.helper_dashboard_understand))
                }
            }
        )
    }
}
