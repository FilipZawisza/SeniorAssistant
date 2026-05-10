package com.zst.senior.assistant.ui.screens

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.zst.senior.assistant.R
import com.zst.senior.assistant.navigation.safePopBackStack
import com.zst.senior.assistant.ui.components.CustomTextField
import com.zst.senior.assistant.ui.components.GradientButton
import com.zst.senior.assistant.ui.theme.AccentGradient
import com.zst.senior.assistant.viewmodel.AssistantViewModel
import com.zst.senior.assistant.viewmodel.AssistantViewModelFactory
import com.zst.senior.assistant.viewmodel.SeniorCalendarViewModel
import com.zst.senior.assistant.viewmodel.Seniorm2ViewModel
import java.util.Locale

/**
 * Ekran asystenta opartego na sztucznej inteligencji, pozwalający użytkownikowi (seniorowi)
 * na dodawanie wydarzeń i przypomnień do kalendarza za pomocą języka naturalnego (tekstowo lub głosowo).
 *
 * Komponent integruje się z systemowym mechanizmem rozpoznawania mowy (Speech-to-Text)
 * oraz z modelem AI (poprzez `AssistantViewModel`), który przetwarza wprowadzone komendy
 * i ekstrahuje z nich strukturalne dane (daty, godziny, tytuły).
 * Zapewnia również pełną obsługę trybu wysokiego kontrastu (WCAG) oraz responsywny układ
 * dostosowujący się do różnych rozmiarów ekranu i wysuwanej klawiatury ekranowej.
 *
 * @param navController Kontroler nawigacji Compose używany do powrotu na poprzedni ekran.
 * @param calendarViewModel ViewModel odpowiedzialny za zarządzanie standardowymi (jednorazowymi) wydarzeniami w kalendarzu.
 * @param seniorm2ViewModel ViewModel odpowiedzialny za zarządzanie wydarzeniami cyklicznymi (np. codzienne przypomnienia o lekach).
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssistantScreen(
    navController: NavController,
    calendarViewModel: SeniorCalendarViewModel,
    seniorm2ViewModel: Seniorm2ViewModel,
    settingsViewModel: com.zst.senior.assistant.viewmodel.SettingsViewModel
) {
    val application = LocalContext.current.applicationContext as android.app.Application
    val assistantViewModel: AssistantViewModel = viewModel(
        factory = AssistantViewModelFactory(application, calendarViewModel, seniorm2ViewModel)
    )
    var textInput by rememberSaveable { mutableStateOf("") }
    val lastResponse by assistantViewModel.lastResponse.collectAsState()
    val isLoading by assistantViewModel.isLoading.collectAsState()
    val language by settingsViewModel.language.collectAsState()

    val pendingEvent by assistantViewModel.pendingEvent.collectAsState()
    val pendingRecurring by assistantViewModel.pendingRecurring.collectAsState()

    val isHighContrast = MaterialTheme.colorScheme.background == Color(0xFF000000)
    val primaryColor = MaterialTheme.colorScheme.primary
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

    // Wcześniejsze pobranie stringów dla rozpoznawania mowy i powiadomień
    val micPromptText = stringResource(R.string.assistant_mic_prompt)
    val micErrorText = stringResource(R.string.assistant_mic_error)

    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.get(0)
            if (!spokenText.isNullOrBlank()) {
                textInput = spokenText
            }
        }
    }

    val backgroundModifier = Modifier.fillMaxSize().then(
        if (isHighContrast) Modifier.background(Color.Black) else Modifier.background(AccentGradient)
    )
    val topContentColor = if (isHighContrast) primaryColor else Color.White
    val iconBorderStroke: BorderStroke? = if (isHighContrast) BorderStroke(3.dp, MaterialTheme.colorScheme.primary) else null
    val cardShape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
    val cardModifier = Modifier.fillMaxWidth().then(
        if (isHighContrast) Modifier.border(3.dp, MaterialTheme.colorScheme.outline, cardShape) else Modifier
    )

    val inputColorHC = MaterialTheme.colorScheme.secondary
    val inputColorStd = MaterialTheme.colorScheme.primary

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = if (isHighContrast) Color.Black else Color.White,
        unfocusedContainerColor = if (isHighContrast) Color.Black else Color.White,
        disabledContainerColor = if (isHighContrast) Color.Black else Color.White,
        focusedTextColor = if (isHighContrast) inputColorHC else Color.Black,
        unfocusedTextColor = if (isHighContrast) inputColorHC else Color.Black,
        cursorColor = if (isHighContrast) inputColorHC else inputColorStd,
        focusedBorderColor = if (isHighContrast) inputColorHC else inputColorStd,
        unfocusedBorderColor = if (isHighContrast) inputColorHC else Color.Gray,
        focusedLabelColor = if (isHighContrast) inputColorHC else inputColorStd,
        unfocusedLabelColor = if (isHighContrast) inputColorHC else Color.Gray
    )

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.assistant_title),
                        color = topContentColor,
                        fontWeight = if (isHighContrast) FontWeight.Black else FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    navigationIconContentColor = topContentColor
                ),
                navigationIcon = {
                    IconButton(onClick = { if (!isLoading) navController.safePopBackStack() }) { // Założenie, że safePopBackStack istnieje
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.assistant_back),
                            modifier = if (isHighContrast) Modifier.size(32.dp) else Modifier.size(24.dp)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        // imePadding sprawia, że interfejs podnosi się nad klawiaturę
        Box(modifier = backgroundModifier.padding(paddingValues).imePadding()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Górna, przewijana sekcja zajmująca dostępną przestrzeń
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f) // Wypycha kartę z wpisywaniem na sam dół
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(Modifier.height(24.dp))

                    Surface(
                        shape = CircleShape,
                        color = if (isHighContrast) Color.Black else Color.White.copy(alpha = 0.2f),
                        border = iconBorderStroke,
                        modifier = Modifier.size(120.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = stringResource(R.string.assistant_icon_desc),
                                tint = if (isHighContrast) MaterialTheme.colorScheme.primary else Color.White,
                                modifier = Modifier.size(60.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Text(
                        text = stringResource(R.string.assistant_hint_text),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        fontWeight = if (isHighContrast) FontWeight.Bold else FontWeight.Normal,
                        color = if (isHighContrast) MaterialTheme.colorScheme.onBackground else Color.White,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                    )

                    Spacer(Modifier.height(24.dp))

                    AnimatedVisibility(
                        visible = lastResponse != null,
                        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                        exit = fadeOut(animationSpec = tween(durationMillis = 150))
                    ) {
                        lastResponse?.let {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Card(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    elevation = CardDefaults.cardElevation(8.dp)
                                ) {
                                    Text(
                                        text = it.text,
                                        modifier = Modifier.padding(16.dp),
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }

                                AnimatedVisibility(visible = pendingEvent != null || pendingRecurring != null) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceEvenly
                                    ) {
                                        Button(
                                            onClick = {
                                                if (pendingEvent != null) assistantViewModel.confirmEvent()
                                                if (pendingRecurring != null) assistantViewModel.confirmRecurring()
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color(0xFF2E7D32),
                                                contentColor = Color.White
                                            )
                                        ) {
                                            Icon(Icons.Default.Check, contentDescription = stringResource(R.string.assistant_confirm_a11y))
                                            Spacer(Modifier.size(8.dp))
                                            Text(stringResource(R.string.assistant_confirm_btn), fontWeight = FontWeight.Bold)
                                        }

                                        Button(
                                            onClick = { assistantViewModel.cancelAction() },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color(0xFFC62828),
                                                contentColor = Color.White
                                            )
                                        ) {
                                            Icon(Icons.Default.Close, contentDescription = stringResource(R.string.assistant_cancel_a11y))
                                            Spacer(Modifier.size(8.dp))
                                            Text(stringResource(R.string.assistant_cancel_btn), fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp)) // Odstęp pod wiadomością
                }

                // Dolna sekcja (zawsze na dole ekranu lub nad klawiaturą)
                Card(
                    modifier = cardModifier,
                    shape = cardShape,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val onSend = {
                            if (textInput.isNotBlank() && !isLoading) {
                                assistantViewModel.sendMessage(textInput, language)
                                textInput = ""
                                keyboardController?.hide()
                            }
                        }

                        CustomTextField(
                            value = textInput,
                            onValueChange = { textInput = it },
                            label = stringResource(R.string.assistant_input_label),
                            enabled = !isLoading,
                            colors = textFieldColors,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(onSend = { onSend() }),
                            trailingIcon = {
                                IconButton(onClick = {
                                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                                        putExtra(RecognizerIntent.EXTRA_PROMPT, micPromptText)
                                    }
                                    try {
                                        speechRecognizerLauncher.launch(intent)
                                    } catch (_: Exception) {
                                        Toast.makeText(context, micErrorText, Toast.LENGTH_SHORT).show()
                                    }
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Mic,
                                        contentDescription = stringResource(R.string.assistant_mic_a11y),
                                        tint = if (isHighContrast) inputColorHC else inputColorStd
                                    )
                                }
                            }
                        )

                        Spacer(Modifier.height(24.dp))

                        if (isHighContrast) {
                            Button(
                                onClick = onSend,
                                enabled = !isLoading && textInput.isNotBlank(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp)),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary,
                                    disabledContainerColor = Color.DarkGray,
                                    disabledContentColor = Color.Gray
                                )
                            ) {
                                Text(
                                    text = if (isLoading) stringResource(R.string.assistant_loading_text) else stringResource(R.string.assistant_send_btn),
                                    fontWeight = FontWeight.Black,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        } else {
                            GradientButton(
                                text = if (isLoading) stringResource(R.string.assistant_loading_text) else stringResource(R.string.assistant_send_btn),
                                onClick = onSend,
                                enabled = !isLoading && textInput.isNotBlank()
                            )
                        }

                        Spacer(Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}