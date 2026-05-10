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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.zst.senior.assistant.R
import com.zst.senior.assistant.navigation.safePopBackStack
import com.zst.senior.assistant.ui.theme.AccentGradient
import com.zst.senior.assistant.viewmodel.LeaderboardViewModel

/**
 * Zbiór zdefiniowanych kolorów wykorzystywanych w obrębie ekranu rankingu.
 * Centralizuje paletę barw dla medali (pucharów) oraz tła poszczególnych wierszy.
 */
// POPRAWKA: Wydzielenie "magicznych" kolorów do czytelnego obiektu konfiguracyjnego
object LeaderboardColors {
    val Gold = Color(0xFFFFD700)
    val Silver = Color(0xFFC0C0C0)
    val Bronze = Color(0xFFCD7F32)
    val RowBackground = Color(0xFFF5F5F5)
}

/**
 * Główny ekran przedstawiający ranking punktowy seniorów.
 *
 * Komponent ten pobiera dane z [LeaderboardViewModel] i wyświetla listę TOP 10 graczy.
 * Ponadto prezentuje stały pasek na dole ekranu z aktualnym miejscem i liczbą punktów
 * zalogowanego użytkownika. Ekran wspiera i automatycznie dostosowuje się do trybu
 * wysokiego kontrastu (High Contrast).
 *
 * @param navController Kontroler nawigacji służący do cofania się do poprzedniego widoku.
 * @param viewModel [LeaderboardViewModel] zarządzający danymi rankingu. Domyślnie tworzony przez Compose w zasięgu cyklu życia.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(
    navController: NavController,
    viewModel: LeaderboardViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val topPlayers by viewModel.topPlayers.collectAsState()
    val myRank by viewModel.currentUserRank.collectAsState()
    val myPoints by viewModel.currentUserPoints.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // POPRAWKA: Dodanie wykrywania trybu wysokiego kontrastu
    val isHighContrast = MaterialTheme.colorScheme.background == Color.Black
    val cardColor = if (isHighContrast) Color.Black else Color.White

    // Odśwież ranking przy każdym wejściu
    LaunchedEffect(Unit) {
        viewModel.fetchLeaderboard()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.leaderboard_title), fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.safePopBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.common_back), tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = if (isHighContrast) Color.Black else Color.Transparent,
        modifier = if (isHighContrast) Modifier else Modifier.background(AccentGradient)
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = if (isHighContrast) MaterialTheme.colorScheme.primary else Color.White)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Karta Top 10
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(16.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = cardColor),
                    // W wysokim kontraście dodajemy wyraźną ramkę
                    border = if (isHighContrast) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.leaderboard_top10),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(topPlayers) { player ->
                                // Przekazujemy stan wysokiego kontrastu do funkcji rysującej wiersz
                                PlayerRow(player.pozycja, player.imie, player.punkty, isHighContrast)
                            }
                        }
                    }
                }

                // Pasek z wynikiem zalogowanego Seniora na dole
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isHighContrast) Color.Black else MaterialTheme.colorScheme.secondaryContainer
                    ),
                    border = if (isHighContrast) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = stringResource(R.string.leaderboard_my_score),
                                fontWeight = FontWeight.Bold,
                                color = if (isHighContrast) Color.White else MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                text = stringResource(R.string.leaderboard_points, myPoints),
                                fontSize = 14.sp,
                                color = if (isHighContrast) Color.LightGray else Color.Black
                            )
                        }
                        Card(
                            shape = CircleShape,
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text(
                                text = "#${myRank ?: "-"}",
                                color = if (isHighContrast) Color.Black else Color.White,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Komponent reprezentujący pojedynczy element (wiersz) na liście rankingowej.
 * * Dla graczy na podium (miejsca 1-3) wyświetla odpowiednie ikony pucharów (złoty, srebrny, brązowy).
 * Dla pozostałych pozycji prezentuje zwykły numer porządkowy. Wygląd wiersza
 * jest uzależniony od tego, czy włączony jest tryb wysokiego kontrastu.
 *
 * @param rank Zajmowana przez gracza pozycja w rankingu.
 * @param name Imię lub pseudonim gracza do wyświetlenia.
 * @param points Liczba zdobytych przez gracza punktów.
 * @param isHighContrast Określa, czy interfejs powinien wygenerować kolory i obramowania o wysokim kontraście.
 */
@Composable
fun PlayerRow(rank: Int, name: String, points: Int, isHighContrast: Boolean) {
    // Dynamiczne kolory w zależności od motywu
    val rowBgColor = if (isHighContrast) Color.Black else LeaderboardColors.RowBackground
    val textColor = if (isHighContrast) Color.White else Color.Black

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(rowBgColor)
            // W trybie HC czarne tło zlewałoby się z kartą, więc dodajemy biało-żółtą ramkę oddzielającą wiersze
            .then(
                if (isHighContrast) Modifier.border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                else Modifier
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Ikonka dla Top 3, numer dla reszty
        Box(
            modifier = Modifier.size(32.dp),
            contentAlignment = Alignment.Center
        ) {
            when (rank) {
                // POPRAWKA: Czytelne etykiety dla czytników ekranu (A11y)
                1 -> Icon(Icons.Default.EmojiEvents, contentDescription = stringResource(R.string.leaderboard_gold_desc), tint = LeaderboardColors.Gold)
                2 -> Icon(Icons.Default.EmojiEvents, contentDescription = stringResource(R.string.leaderboard_silver_desc), tint = LeaderboardColors.Silver)
                3 -> Icon(Icons.Default.EmojiEvents, contentDescription = stringResource(R.string.leaderboard_bronze_desc), tint = LeaderboardColors.Bronze)
                else -> Text("$rank.", fontWeight = FontWeight.Bold, color = if (isHighContrast) Color.LightGray else Color.Gray)
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = name, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), color = textColor)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "$points", fontWeight = FontWeight.Bold, color = textColor)
            Spacer(modifier = Modifier.width(4.dp))
            // POPRAWKA: Opis gwiazdki
            Icon(Icons.Default.Star, contentDescription = stringResource(R.string.leaderboard_star_desc), tint = LeaderboardColors.Gold, modifier = Modifier.size(16.dp))
        }
    }
}