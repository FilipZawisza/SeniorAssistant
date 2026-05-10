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
 * Ekran prezentujący Politykę Prywatności aplikacji SeniorAssistant.
 *
 * Komponent ten wyświetla statyczny, przewijany w pionie tekst dokumentu prawnego,
 * podzielony na czytelne sekcje. Ekran zawiera górny pasek nawigacyjny (TopAppBar)
 * z przyciskiem powrotu, wykorzystującym bezpieczną nawigację wstecz.
 *
 * @param navController Kontroler nawigacji służący do obsługi powrotu do poprzedniego ekranu.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(navController: NavController) {
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.privacy_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.safePopBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.privacy_back))
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
                text = stringResource(R.string.privacy_header),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            PolicySection(
                title = stringResource(R.string.privacy_section1_title),
                content = stringResource(R.string.privacy_section1_content)
            )

            PolicySection(
                title = stringResource(R.string.privacy_section2_title),
                content = stringResource(R.string.privacy_section2_content)
            )

            PolicySection(
                title = stringResource(R.string.privacy_section3_title),
                content = stringResource(R.string.privacy_section3_content)
            )

            PolicySection(
                title = stringResource(R.string.privacy_section4_title),
                content = stringResource(R.string.privacy_section4_content)
            )

            PolicySection(
                title = stringResource(R.string.privacy_section5_title),
                content = stringResource(R.string.privacy_section5_content)
            )

            PolicySection(
                title = stringResource(R.string.privacy_section6_title),
                content = stringResource(R.string.privacy_section6_content)
            )

            PolicySection(
                title = stringResource(R.string.privacy_section7_title),
                content = stringResource(R.string.privacy_section7_content)
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

/**
 * Komponent pomocniczy renderujący pojedynczą sekcję dokumentu z zachowaniem
 * spójnego formatowania.
 *
 * @param title Pogrubiony nagłówek sekcji (np. "1. Administrator Danych Osobowych").
 * @param content Treść właściwa danej sekcji dokumentu.
 */
@Composable
fun PolicySection(title: String, content: String) {
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