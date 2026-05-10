package com.zst.senior.assistant.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.zst.senior.assistant.R
import com.zst.senior.assistant.navigation.safePopBackStack

/**
 * Ekran wyświetlający pełny Regulamin korzystania z aplikacji SeniorAssistant.
 *
 * Prezentuje zbiór zasad, praw i obowiązków użytkowników (Seniorów, Wolontariuszy, Administratorów)
 * w formie statycznego, przewijanego widoku. Ekran zawiera górny pasek nawigacyjny (TopAppBar)
 * umożliwiający bezpieczny powrót do poprzedniego widoku.
 * Cała treść dokumentu jest zorganizowana logicznie przy użyciu komponentu [TermsSection].
 *
 * @param navController Kontroler nawigacji służący do obsługi stosu ekranów (np. działanie przycisku "Wróć").
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermsOfServiceScreen(navController: NavController) {
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.tos_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.safePopBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.common_back))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.tos_header),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            TermsSection(
                title = stringResource(R.string.tos_section1_title),
                content = stringResource(R.string.tos_section1_content)
            )

            TermsSection(
                title = stringResource(R.string.tos_section2_title),
                content = stringResource(R.string.tos_section2_content)
            )

            TermsSection(
                title = stringResource(R.string.tos_section3_title),
                content = stringResource(R.string.tos_section3_content)
            )

            TermsSection(
                title = stringResource(R.string.tos_section4_title),
                content = stringResource(R.string.tos_section4_content)
            )

            TermsSection(
                title = stringResource(R.string.tos_section5_title),
                content = stringResource(R.string.tos_section5_content)
            )

            TermsSection(
                title = stringResource(R.string.tos_section6_title),
                content = stringResource(R.string.tos_section6_content)
            )

            TermsSection(
                title = stringResource(R.string.tos_section7_title),
                content = stringResource(R.string.tos_section7_content)
            )

            TermsSection(
                title = stringResource(R.string.tos_section8_title),
                content = stringResource(R.string.tos_section8_content)
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

/**
 * Komponent pomocniczy (Helper Composable) ułatwiający formatowanie poszczególnych punktów regulaminu.
 * * Odpowiada za ujednolicony wygląd dokumentu, oddzielając odpowiednimi marginesami
 * i stylami typograficznymi tytuł sekcji od jej zawartości. Zwiększona interlinia (lineHeight)
 * ułatwia czytanie bloków tekstu, co jest szczególnie ważne ze względu na grupę docelową aplikacji.
 *
 * @param title Tytuł lub numer sekcji (np. "1. Postanowienia ogólne"), wyświetlany pogrubioną czcionką.
 * @param content Właściwa treść sekcji regulaminu.
 */
@Composable
fun TermsSection(title: String, content: String) {
    Column(modifier = Modifier.padding(bottom = 20.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = content,
            style = MaterialTheme.typography.bodyMedium,
            lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.2
        )
    }
}