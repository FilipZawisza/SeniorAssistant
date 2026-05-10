package com.zst.senior.assistant.ui.screens

import android.content.Context
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import com.zst.senior.assistant.R
import com.zst.senior.assistant.navigation.AppRoutes
import com.zst.senior.assistant.navigation.safeNavigate
import com.zst.senior.assistant.ui.theme.AuthBackgroundGradient
import com.zst.senior.assistant.ui.theme.BrandBlue
import com.zst.senior.assistant.ui.theme.WcagBlack
import com.zst.senior.assistant.viewmodel.SettingsViewModel
import androidx.core.content.edit

/**
 * Główny ekran wprowadzający (Onboarding) dla nowych użytkowników aplikacji.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(navController: NavController, settingsViewModel: SettingsViewModel) {
    val pagerState = rememberPagerState(pageCount = { 5 })
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val isHighContrast = MaterialTheme.colorScheme.background == WcagBlack

    // STANY AKCEPTACJI
    var isPrivacyAccepted by rememberSaveable { mutableStateOf(false) }
    var isTermsAccepted by rememberSaveable { mutableStateOf(false) }

    val backgroundModifier = if (isHighContrast) {
        Modifier.background(Color.Black)
    } else {
        Modifier.background(brush = AuthBackgroundGradient)
    }

    val navContentColor = if (isHighContrast) MaterialTheme.colorScheme.primary else BrandBlue

    fun finishOnboarding() {
        val sharedPrefs = context.getSharedPreferences("OnboardingPrefs", Context.MODE_PRIVATE)
        sharedPrefs.edit { putBoolean("hasSeenOnboarding", true) }

        if (navController.currentDestination?.route == AppRoutes.ONBOARDING) {
            navController.safeNavigate(AppRoutes.LOGIN) {
                popUpTo(AppRoutes.ONBOARDING) { inclusive = true }
            }
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val currentPage = pagerState.currentPage
                val isLastPage = currentPage == pagerState.pageCount - 1

                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (currentPage > 0 && !isLastPage) {
                        TextButton(onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.pageCount - 1)
                            }
                        }) {
                            Text(
                                stringResource(R.string.onboarding_skip),
                                color = if (isHighContrast) navContentColor else Color.Gray,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isHighContrast) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(pagerState.pageCount) { iteration ->
                        val isSelected = currentPage == iteration
                        val width by animateDpAsState(if (isSelected) 24.dp else 8.dp, label = "dotWidth")

                        val dotColor = if (isHighContrast) {
                            if (isSelected) MaterialTheme.colorScheme.primary else Color.DarkGray
                        } else {
                            if (isSelected) BrandBlue else Color.LightGray
                        }

                        Box(
                            modifier = Modifier
                                .padding(2.dp)
                                .height(8.dp)
                                .width(width)
                                .clip(CircleShape)
                                .background(dotColor)
                        )
                    }
                }

                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    if (currentPage > 0 && !isLastPage) {
                        TextButton(onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(currentPage + 1)
                            }
                        }) {
                            Text(
                                stringResource(R.string.onboarding_next),
                                color = navContentColor,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = backgroundModifier.padding(paddingValues)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (page) {
                    0 -> LanguageSelectionPage(
                        isHighContrast = isHighContrast,
                        onLanguageSelected = { lang ->
                            settingsViewModel.setLanguage(lang)
                            scope.launch { pagerState.animateScrollToPage(1) }
                        }
                    )
                    1 -> OnboardingPage(
                        title = stringResource(R.string.onboarding_welcome_title),
                        description = stringResource(R.string.onboarding_welcome_desc),
                        icon = R.drawable.icon,
                        isHighContrast = isHighContrast
                    )
                    2 -> OnboardingPage(
                        title = stringResource(R.string.onboarding_why_title),
                        description = stringResource(R.string.onboarding_why_desc),
                        icon = Icons.Default.Shield,
                        isHighContrast = isHighContrast
                    )
                    3 -> OnboardingPage(
                        title = stringResource(R.string.onboarding_safety_title),
                        description = stringResource(R.string.onboarding_safety_desc),
                        icon = Icons.Default.Description,
                        isHighContrast = isHighContrast
                    )
                    4 -> OnboardingPage(
                        title = stringResource(R.string.onboarding_ready_title),
                        description = stringResource(R.string.onboarding_ready_desc),
                        icon = Icons.Default.Celebration,
                        isFinalPage = true,
                        isPrivacyAccepted = isPrivacyAccepted,
                        onPrivacyAcceptedChange = { isPrivacyAccepted = it },
                        isTermsAccepted = isTermsAccepted,
                        onTermsAcceptedChange = { isTermsAccepted = it },
                        onPolicyClick = { navController.safeNavigate(AppRoutes.PRIVACY_POLICY) },
                        onTermsClick = { navController.safeNavigate(AppRoutes.TERMS_OF_SERVICE) },
                        onStartClick = { finishOnboarding() },
                        isHighContrast = isHighContrast
                    )
                }
            }
        }
    }
}

/**
 * Pierwsza strona onboardingu umożliwiająca wybór języka aplikacji.
 *
 * @param isHighContrast Czy aktywny jest tryb wysokiego kontrastu.
 * @param onLanguageSelected Funkcja wywoływana po wybraniu języka.
 */
@Composable
fun LanguageSelectionPage(
    isHighContrast: Boolean,
    onLanguageSelected: (String) -> Unit
) {
    val scrollState = rememberScrollState()
    val primaryColor = if (isHighContrast) MaterialTheme.colorScheme.primary else BrandBlue
    val textColor = if (isHighContrast) MaterialTheme.colorScheme.primary else Color.Black

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 32.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = CircleShape,
            color = if (isHighContrast) Color.Black else Color.White.copy(alpha = 0.6f),
            border = if (isHighContrast) BorderStroke(3.dp, primaryColor) else null,
            modifier = Modifier.size(160.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = primaryColor
                )
            }
        }

        Spacer(Modifier.height(32.dp))

        Text(
            text = stringResource(R.string.onboarding_lang_select_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
            color = textColor
        )

        Spacer(Modifier.height(48.dp))

        Button(
            onClick = { onLanguageSelected("pl") },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isHighContrast) Color.Black else BrandBlue,
                contentColor = if (isHighContrast) MaterialTheme.colorScheme.primary else Color.White
            ),
            border = if (isHighContrast) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
        ) {
            Text(stringResource(R.string.onboarding_lang_pl), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = { onLanguageSelected("en") },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isHighContrast) Color.Black else BrandBlue,
                contentColor = if (isHighContrast) MaterialTheme.colorScheme.primary else Color.White
            ),
            border = if (isHighContrast) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
        ) {
            Text(stringResource(R.string.onboarding_lang_en), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        }
    }
}

/**
 * Komponent reprezentujący pojedynczą stronę informacyjną w procesie onboardingu.
 *
 * @param title Tytuł strony.
 * @param description Opis funkcjonalności lub informacji.
 * @param icon Ikona (ImageVector lub ID zasobu) wyświetlana na środku.
 * @param isFinalPage Czy jest to ostatnia strona z formularzem akceptacji regulaminów.
 * @param isPrivacyAccepted Stan checkboxa polityki prywatności.
 * @param onPrivacyAcceptedChange Akcja zmiany stanu checkboxa polityki.
 * @param isTermsAccepted Stan checkboxa regulaminu.
 * @param onTermsAcceptedChange Akcja zmiany stanu checkboxa regulaminu.
 * @param onPolicyClick Akcja otwarcia dokumentu polityki prywatności.
 * @param onTermsClick Akcja otwarcia dokumentu regulaminu.
 * @param onStartClick Akcja zakończenia onboardingu i przejścia do logowania.
 * @param isHighContrast Czy aktywny jest tryb wysokiego kontrastu.
 */
@Composable
fun OnboardingPage(
    title: String,
    description: String,
    icon: Any,
    isFinalPage: Boolean = false,
    isPrivacyAccepted: Boolean = false,
    onPrivacyAcceptedChange: (Boolean) -> Unit = {},
    isTermsAccepted: Boolean = false,
    onTermsAcceptedChange: (Boolean) -> Unit = {},
    onPolicyClick: () -> Unit = {},
    onTermsClick: () -> Unit = {},
    onStartClick: () -> Unit = {},
    isHighContrast: Boolean
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 32.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {

        Spacer(modifier = Modifier.height(16.dp))

        val circleColor = if (isHighContrast) Color.Black else Color.White.copy(alpha = 0.6f)
        val iconTint = if (isHighContrast) MaterialTheme.colorScheme.primary else BrandBlue
        val border = if (isHighContrast) BorderStroke(3.dp, MaterialTheme.colorScheme.primary) else null

        Surface(
            shape = CircleShape,
            color = circleColor,
            border = border,
            modifier = Modifier.size(200.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                when (icon) {
                    is ImageVector -> {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier.size(100.dp),
                            tint = iconTint
                        )
                    }
                    is Int -> {
                        Image(
                            painter = painterResource(id = icon),
                            contentDescription = null,
                            modifier = Modifier.size(120.dp)
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(32.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
            color = if (isHighContrast) MaterialTheme.colorScheme.primary else Color.Black
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = description,
            style = MaterialTheme.typography.bodyLarge,
            color = if (isHighContrast) Color.White else Color.Black.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            fontWeight = if (isHighContrast) FontWeight.Bold else FontWeight.Normal,
            lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.2
        )

        Spacer(Modifier.height(32.dp))

        if (isFinalPage) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                Checkbox(
                    checked = isPrivacyAccepted,
                    onCheckedChange = onPrivacyAcceptedChange,
                    colors = CheckboxDefaults.colors(
                        checkedColor = if (isHighContrast) MaterialTheme.colorScheme.primary else BrandBlue
                    )
                )
                Text(
                    modifier = Modifier.padding(end = 15.dp),
                    text = stringResource(R.string.onboarding_accept_prefix),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isHighContrast) Color.White else Color.Black
                )
                Text(
                    text = stringResource(R.string.onboarding_policy_link),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (isHighContrast) MaterialTheme.colorScheme.primary else BrandBlue
                    ),
                    modifier = Modifier.clickable { onPolicyClick() }
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                Checkbox(
                    checked = isTermsAccepted,
                    onCheckedChange = onTermsAcceptedChange,
                    colors = CheckboxDefaults.colors(
                        checkedColor = if (isHighContrast) MaterialTheme.colorScheme.primary else BrandBlue
                    )
                )
                Text(
                    modifier = Modifier.padding(end = 15.dp),
                    text = stringResource(R.string.onboarding_accept_prefix),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isHighContrast) Color.White else Color.Black
                )
                Text(
                    text = stringResource(R.string.onboarding_terms_link),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (isHighContrast) MaterialTheme.colorScheme.primary else BrandBlue
                    ),
                    modifier = Modifier.clickable { onTermsClick() }
                )
            }

            val canStart = isPrivacyAccepted && isTermsAccepted

            Button(
                onClick = onStartClick,
                enabled = canStart,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isHighContrast) Color.Black else BrandBlue,
                    contentColor = if (isHighContrast) MaterialTheme.colorScheme.primary else Color.White,
                    disabledContainerColor = Color.Gray,
                    disabledContentColor = Color.LightGray
                ),
                border = if (isHighContrast && canStart) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = stringResource(R.string.onboarding_start_button),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}