package com.zst.senior.assistant.ui.screens

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.navigation.NavController
import com.zst.senior.assistant.R
import com.zst.senior.assistant.navigation.safePopBackStack
import com.zst.senior.assistant.ui.theme.BrandBlue
import com.zst.senior.assistant.ui.theme.WcagBlack

/**
 * Główny ekran "O aplikacji" (AboutScreen).
 *
 * Prezentuje użytkownikowi najważniejsze informacje o projekcie "Senior Assistant",
 * w tym logo, wersję, dokumenty prawne (regulamin, polityka prywatności), zespół twórców,
 * wykorzystane technologie oraz dane kontaktowe. Ekran w pełni wspiera tryb wysokiego kontrastu (HC),
 * automatycznie dostosowując paletę barw i obramowania.
 *
 * @param navController Kontroler nawigacji używany do powrotu do poprzedniego ekranu
 * oraz otwierania ekranów z dokumentacją prawną.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AboutScreen(navController: NavController) {
    val context = LocalContext.current
    val isHighContrast = MaterialTheme.colorScheme.background == WcagBlack
    val backgroundColor = if (isHighContrast) Color.Black else Color(0xFFF5F7FA)
    val cardColor = if (isHighContrast) WcagBlack else Color.White
    val textColor = if (isHighContrast) Color.White else Color.Black
    val primaryColor = if (isHighContrast) MaterialTheme.colorScheme.primary else BrandBlue

    // Pobranie stringów używanych wewnątrz Intencji
    val emailSubject = stringResource(R.string.about_email_subject)
    val emailChooser = stringResource(R.string.about_email_chooser)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.about_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.safePopBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.about_back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundColor,
                    titleContentColor = textColor,
                    navigationIconContentColor = textColor
                )
            )
        },
        containerColor = backgroundColor
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo i Nazwa
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.icon),
                        contentDescription = "Logo Senior Assistant",
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Senior Assistant",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = primaryColor
                    )
                    Text(
                        text = stringResource(R.string.about_version),
                        style = MaterialTheme.typography.bodyMedium,
                        color = textColor.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Dokumenty prawne
            AboutSection(
                title = stringResource(R.string.about_section_docs),
                icon = Icons.Default.Description,
                isHighContrast = isHighContrast
            ) {
                DocumentItem(stringResource(R.string.about_policy), isHighContrast) {
                    navController.navigate("privacy_policy")
                }
                DocumentItem(stringResource(R.string.about_terms), isHighContrast) {
                    navController.navigate("terms_of_service")
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Twórcy i Szkoła
            AboutSection(
                title = stringResource(R.string.about_section_team),
                icon = Icons.Default.School,
                isHighContrast = isHighContrast
            ) {
                Text(
                    text = stringResource(R.string.about_team_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                CreatorItem("Filip Zawisza", isHighContrast)
                CreatorItem("Dominik Szymczak", isHighContrast)
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Informacje techniczne
            AboutSection(
                title = stringResource(R.string.about_section_tech),
                icon = Icons.Default.Code,
                isHighContrast = isHighContrast
            ) {
                Text(
                    text = stringResource(R.string.about_tech_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = textColor.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val techs = listOf("Kotlin", "Jetpack Compose", "Firebase (Auth, Firestore)", "MVVM Architecture", "Coroutines", "Google Maps")
                    techs.forEach { tech ->
                        TechTag(tech, isHighContrast)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Kontakt i Kod
            AboutSection(
                title = stringResource(R.string.about_section_contact),
                icon = Icons.Default.Language,
                isHighContrast = isHighContrast
            ) {
                val email = "kontakt@seniorassistant.pl"
                val website = "seniorassistant.pl"
                val github = "github.com/FilipZawisza/SeniorAssistant"
                val githubUrl = "https://github.com/FilipZawisza/SeniorAssistant"

                ClickableLinkItem(email, Icons.Default.Email, isHighContrast) {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = "mailto:$email".toUri()
                        putExtra(Intent.EXTRA_SUBJECT, emailSubject)
                    }
                    context.startActivity(Intent.createChooser(intent, emailChooser))
                }

                Spacer(modifier = Modifier.height(12.dp))

                ClickableLinkItem(website, Icons.Default.Language, isHighContrast) {
                    val intent = Intent(Intent.ACTION_VIEW, "https://$website".toUri())
                    context.startActivity(intent)
                }

                Spacer(modifier = Modifier.height(12.dp))

                ClickableLinkItem(github, Icons.Default.Code, isHighContrast) {
                    val intent = Intent(Intent.ACTION_VIEW, githubUrl.toUri())
                    context.startActivity(intent)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Opis
            AboutSection(
                title = stringResource(R.string.about_section_project),
                icon = Icons.Default.Info,
                isHighContrast = isHighContrast
            ) {
                Text(
                    text = stringResource(R.string.about_project_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 22.sp,
                    color = textColor,
                    textAlign = TextAlign.Justify
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = stringResource(R.string.about_copyright),
                style = MaterialTheme.typography.labelMedium,
                color = textColor.copy(alpha = 0.5f)
            )
        }
    }
}

/**
 * Kontener wielokrotnego użytku budujący jednolitą kartę sekcji informacyjnej.
 *
 * @param title Tytuł wyświetlany w nagłówku sekcji.
 * @param icon Ikona wektorowa wyświetlana obok tytułu.
 * @param isHighContrast Flaga określająca użycie schematu kolorów o wysokim kontraście.
 * @param content Funkcja Composable zawierająca właściwą treść sekcji (np. teksty, listy, przyciski).
 */
@Composable
fun AboutSection(
    title: String,
    icon: ImageVector,
    isHighContrast: Boolean,
    content: @Composable ColumnScope.() -> Unit
) {
    val cardColor = if (isHighContrast) WcagBlack else Color.White
    val primaryColor = if (isHighContrast) MaterialTheme.colorScheme.primary else BrandBlue

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = stringResource(R.string.about_section_icon_desc, title), tint = primaryColor, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor
                )
            }
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = if (isHighContrast) primaryColor.copy(alpha = 0.3f) else Color.LightGray.copy(alpha = 0.5f)
            )
            content()
        }
    }
}

/**
 * Komponent wyświetlający imię i nazwisko twórcy aplikacji poprzedzone graficznym punktatorem.
 *
 * @param name Imię i nazwisko twórcy.
 * @param isHighContrast Flaga określająca użycie schematu kolorów o wysokim kontraście.
 */
@Composable
fun CreatorItem(name: String, isHighContrast: Boolean) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(
                    if (isHighContrast) MaterialTheme.colorScheme.primary else BrandBlue,
                    CircleShape
                )
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = name,
            style = MaterialTheme.typography.bodyLarge,
            color = if (isHighContrast) Color.White else Color.Black
        )
    }
}

/**
 * Interaktywny element listy służący do nawigacji do ważnych dokumentów (RODO, Regulamin).
 *
 * @param title Nazwa dokumentu do wyświetlenia.
 * @param isHighContrast Flaga określająca użycie schematu kolorów o wysokim kontraście.
 * @param onClick Funkcja wywoływana po kliknięciu elementu (zazwyczaj inicjująca nawigację).
 */
@Composable
fun DocumentItem(title: String, isHighContrast: Boolean, onClick: () -> Unit) {
    val textColor = if (isHighContrast) Color.White else Color.Black
    val primaryColor = if (isHighContrast) MaterialTheme.colorScheme.primary else BrandBlue

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = title, color = textColor, style = MaterialTheme.typography.bodyMedium)
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = stringResource(R.string.about_open_doc_desc),
            tint = primaryColor,
            modifier = Modifier.size(20.dp)
        )
    }
}

/**
 * Wizualna etykieta (chip/tag) służąca do zaprezentowania pojedynczej technologii z użytego stosu.
 *
 * @param tech Nazwa technologii (np. "Kotlin").
 * @param isHighContrast Flaga określająca użycie schematu kolorów o wysokim kontraście.
 */
@Composable
fun TechTag(tech: String, isHighContrast: Boolean) {
    val primaryColor = if (isHighContrast) MaterialTheme.colorScheme.primary else BrandBlue
    Surface(
        color = if (isHighContrast) Color.Black else primaryColor.copy(alpha = 0.1f),
        border = if (isHighContrast) BorderStroke(1.dp, primaryColor) else null,
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = tech,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium,
            color = if (isHighContrast) Color.White else primaryColor
        )
    }
}

/**
 * Interaktywny wiersz służący jako odnośnik wywołujący zewnętrzną akcję (np. wysłanie e-maila, otwarcie strony WWW).
 *
 * @param text Tekst wyświetlany jako hiperłącze (np. adres email lub URL).
 * @param icon Ikona wektorowa dopasowana do kontekstu łącza.
 * @param isHighContrast Flaga określająca użycie schematu kolorów o wysokim kontraście.
 * @param onClick Funkcja wywoływana po kliknięciu (np. odpalenie `Intent` otwierającego przeglądarkę).
 */
@Composable
fun ClickableLinkItem(text: String, icon: ImageVector, isHighContrast: Boolean, onClick: () -> Unit) {
    val primaryColor = if (isHighContrast) MaterialTheme.colorScheme.primary else BrandBlue
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clickable { onClick() }
    ) {
        Icon(icon, contentDescription = stringResource(R.string.about_link_icon_desc, text), tint = primaryColor.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = primaryColor,
            textDecoration = TextDecoration.Underline
        )
    }
}