@file:Suppress("DEPRECATION")

package com.zst.senior.assistant.ui.screens

import android.speech.tts.TextToSpeech
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.OutlinedFlag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.zst.senior.assistant.R
import com.zst.senior.assistant.navigation.safePopBackStack
import com.zst.senior.assistant.ui.theme.AccentGradient
import com.zst.senior.assistant.viewmodel.GameStatus
import com.zst.senior.assistant.viewmodel.SeniordleViewModel
import java.util.Locale

// --- STAŁE KOLORY GRY ---
private val ColorCorrect = Color(0xFF4CAF50) // Zielony
private val ColorPresent = Color(0xFFFFC107) // Żółty
private val ColorAbsent = Color(0xFF757575)  // Szary

/**
 * Reprezentuje stan pojedynczej litery na klawiaturze lub na planszy po zatwierdzeniu słowa.
 *
 * * [DEFAULT] - Litera nie została jeszcze użyta.
 * * [ABSENT] - Litera nie występuje w szukanym haśle.
 * * [PRESENT] - Litera występuje w haśle, ale znajduje się na złej pozycji.
 * * [CORRECT] - Litera znajduje się na właściwej pozycji.
 */
enum class KeyState {
    DEFAULT, ABSENT, PRESENT, CORRECT
}

/**
 * Główny ekran gry "Seniordle" (wariacja gry Wordle) zoptymalizowany dla seniorów.
 *
 * Ekran integruje logikę rozgrywki poprzez [SeniordleViewModel]. Zawiera ułatwienia dostępu:
 * - Odtwarzanie głosowe (Text-To-Speech) instrukcji oraz wyników gry.
 * - Informacje zwrotne w postaci wibracji (Haptic Feedback) podczas wprowadzania liter.
 * - Dynamiczne dopasowanie kolorów do trybu wysokiego kontrastu (WCAG).
 *
 * @param navController Kontroler nawigacji do powrotu lub przejścia do rankingu.
 * @param viewModel ViewModel zarządzający stanem rozgrywki (domyślnie tworzony przez `viewModel()`).
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeniordleScreen(
    navController: NavController,
    viewModel: SeniordleViewModel = viewModel()
) {
    val context = LocalContext.current // Zostawiamy context, bo jest potrzebny do inicjalizacji TTS
    val haptic = LocalHapticFeedback.current
    val isHighContrast = MaterialTheme.colorScheme.background == Color(0xFF000000)

    val targetWord by viewModel.targetWord.collectAsState()
    val guesses by viewModel.guesses.collectAsState()
    val currentGuess by viewModel.currentGuess.collectAsState()
    val gameStatus by viewModel.gameStatus.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val hintData by viewModel.hintData.collectAsState()
    val winStreak by viewModel.winStreak.collectAsState()

    var showHelpDialog by remember { mutableStateOf(false) }
    var hasSeenRules by rememberSaveable { mutableStateOf(false) }

    val backgroundModifier = Modifier
        .fillMaxSize()
        .then(
            if (isHighContrast) Modifier.background(Color.Black) else Modifier.background(AccentGradient)
        )

    val textColor = if (isHighContrast) MaterialTheme.colorScheme.primary else Color.White

    // --- Wcześniejsze pobranie stringów do asystenta głosowego (TTS) ---
    val startTtsMsg = stringResource(R.string.game_start_tts)
    val ttsGameWon = stringResource(R.string.game_won_tts)
    val ttsGameLost = stringResource(R.string.game_lost_tts, targetWord)
    val ttsGameGiveUp = stringResource(R.string.game_give_up_tts, targetWord)

    // Konfiguracja TTS (Text-To-Speech)
    @Suppress("UNUSED_VARIABLE")
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }

    DisposableEffect(context) {
        val textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.getDefault()
                tts?.speak(startTtsMsg, TextToSpeech.QUEUE_FLUSH, null, null)
            }
        }
        tts = textToSpeech
        onDispose { tts?.shutdown() }
    }

    // Reakcja asystenta głosowego na zmianę statusu gry
    LaunchedEffect(gameStatus) {
        when (gameStatus) {
            GameStatus.WON -> tts?.speak(ttsGameWon, TextToSpeech.QUEUE_FLUSH, null, null)
            GameStatus.LOST -> tts?.speak(ttsGameLost, TextToSpeech.QUEUE_FLUSH, null, null)
            GameStatus.GIVEN_UP -> tts?.speak(ttsGameGiveUp, TextToSpeech.QUEUE_FLUSH, null, null)
            else -> {}
        }
    }

    // Automatyczne wyświetlenie instrukcji przy pierwszym uruchomieniu
    LaunchedEffect(Unit) {
        if (!hasSeenRules) {
            showHelpDialog = true
            hasSeenRules = true
        }
    }

    if (showHelpDialog) {
        HelpDialog(onDismiss = { showHelpDialog = false })
    }

    Scaffold(
        modifier = Modifier.imePadding(), // Zabezpiecza przed ucinaniem przez klawiatury systemowe
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.game_title), fontWeight = FontWeight.Bold, color = textColor) },
                navigationIcon = {
                    IconButton(onClick = { navController.safePopBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.login_back), tint = textColor)
                    }
                },
                actions = {
                    // Wyświetlenie aktualnej "serii" wygranych (streak)
                    if (winStreak > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocalFireDepartment, contentDescription = "Seria", tint = Color(0xFFFF5722))
                            Text(text = "$winStreak", color = textColor, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(16.dp))
                        }
                    }
                    IconButton(onClick = { showHelpDialog = true }) {
                        Icon(Icons.Default.Info, "Pomoc", tint = textColor, modifier = Modifier.size(32.dp))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Transparent
    ) { padding ->
        Column(
            modifier = backgroundModifier
                .padding(padding)
                .navigationBarsPadding() // KRYTYCZNE: chroni przed paskiem gestów na dole na Pixelach
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // --- SEKCJA GÓRNA: PODPOWIEDZI I ŁADOWANIE ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (isLoading) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = textColor, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(stringResource(R.string.game_loading_words), color = textColor, fontWeight = FontWeight.Medium)
                    }
                } else if (gameStatus == GameStatus.PLAYING) {
                    if (hintData == null) {
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            OutlinedButton(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.useHint()
                                },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = textColor),
                                border = BorderStroke(1.dp, textColor),
                                contentPadding = PaddingValues(horizontal = 12.dp)
                            ) {
                                Icon(Icons.Default.Lightbulb, contentDescription = "Użyj podpowiedzi", tint = Color(0xFFFFC107), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(stringResource(R.string.game_hint_button))
                            }
                            OutlinedButton(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.giveUp()
                                },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = textColor),
                                border = BorderStroke(1.dp, textColor),
                                contentPadding = PaddingValues(horizontal = 12.dp)
                            ) {
                                Icon(Icons.Default.OutlinedFlag, contentDescription = "Poddaj się", tint = textColor, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(stringResource(R.string.game_give_up_button))
                            }
                        }
                    } else {
                        val msg = if (hintData!!.allFound) {
                            stringResource(R.string.game_hint_all_letters)
                        } else {
                            stringResource(R.string.game_hint_found, hintData!!.position, hintData!!.letter)
                        }
                        Text(
                            text = "💡 $msg",
                            color = if (isHighContrast) textColor else Color(0xFFFFC107),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                }
            }

            // 1. PLANSZA GRY (Elastyczna przestrzeń ze skrolowaniem)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f) // Wypycha klawiaturę na sam dół
                    .verticalScroll(rememberScrollState()), // Przewijanie tylko planszy
                contentAlignment = Alignment.Center
            ) {
                GameBoard(
                    guesses = guesses,
                    currentGuess = currentGuess,
                    targetWord = targetWord,
                    isHighContrast = isHighContrast,
                    textColor = textColor
                )
            }

            // 2. KLAWIATURA LUB EKRAN KOŃCOWY (Zawsze przyklejone do dołu)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp, start = 8.dp, end = 8.dp)
            ) {
                if (gameStatus != GameStatus.PLAYING) {
                    GameOverSection(
                        gameStatus = gameStatus,
                        targetWord = targetWord,
                        textColor = textColor,
                        hintRevealed = hintData != null,
                        onReset = { viewModel.resetGame() },
                        onViewLeaderboard = { navController.navigate("leaderboard") }
                    )
                } else {
                    KeyboardIosStyle(
                        isHighContrast = isHighContrast,
                        guesses = guesses,
                        targetWord = targetWord,
                        onKeyClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            viewModel.onKeyPress(it)
                        },
                        onBackspace = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            viewModel.onBackspace()
                        },
                        onEnter = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.onEnter()
                        }
                    )
                }
            }
        }
    }
}

/**
 * Okno dialogowe wyświetlające zasady gry, legendę kolorów i krótki samouczek.
 *
 * @param onDismiss Funkcja wywoływana po zamknięciu okna.
 */
@Composable
private fun HelpDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.game_help_title), fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(stringResource(R.string.game_help_desc))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(20.dp).background(ColorCorrect, RoundedCornerShape(4.dp)))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.game_help_green))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(20.dp).background(ColorPresent, RoundedCornerShape(4.dp)))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.game_help_yellow))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(20.dp).background(ColorAbsent, RoundedCornerShape(4.dp)))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.game_help_gray))
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) { Text(stringResource(R.string.game_understand)) }
        }
    )
}

/**
 * Plansza gry (siatka 6 wierszy x 5 kolumn), na której wyświetlane są wpisane słowa oraz aktualna próba.
 * Renderuje odpowiednie kolory dla zatwierdzonych prób na podstawie tego, jak bardzo
 * zbliżone są do szukanego hasła.
 *
 * @param guesses Lista dotychczas odgadniętych słów.
 * @param currentGuess Aktualnie wpisywane, ale jeszcze niezatwierdzone słowo.
 * @param targetWord Szukane słowo, weryfikujące kolory podpowiedzi.
 * @param isHighContrast Określa, czy włączony jest tryb o wysokim kontraście.
 * @param textColor Główny kolor tekstu kafelków dopasowany do bieżącego trybu.
 */
@Composable
private fun GameBoard(
    guesses: List<String>,
    currentGuess: String,
    targetWord: String,
    isHighContrast: Boolean,
    textColor: Color
) {
    val tileEmptyColor = if (isHighContrast) Color.DarkGray else Color.White.copy(alpha = 0.3f)

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        for (i in 0 until 6) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                for (j in 0 until 5) {
                    val letter = when {
                        i < guesses.size -> guesses[i].getOrNull(j)?.toString() ?: ""
                        i == guesses.size && j < currentGuess.length -> currentGuess.getOrNull(j)?.toString() ?: ""
                        else -> ""
                    }

                    var boxColor = tileEmptyColor
                    var boxTextColor = if (isHighContrast) textColor else Color.Black
                    var border = if (isHighContrast) BorderStroke(1.dp, textColor) else null

                    // Określanie kolorów jeśli słowo z tego wiersza zostało już zatwierdzone
                    if (i < guesses.size) {
                        val char = guesses[i].getOrNull(j) ?: ' '
                        if (targetWord.getOrNull(j) == char) {
                            boxColor = ColorCorrect
                            boxTextColor = Color.White
                            border = null
                        } else if (targetWord.contains(char)) {
                            boxColor = ColorPresent
                            boxTextColor = Color.Black
                            border = null
                        } else {
                            boxColor = ColorAbsent
                            boxTextColor = Color.White
                            border = null
                        }
                    } else if (letter.isNotEmpty()) {
                        // Kolorowanie kafelków w obecnie modyfikowanym wierszu
                        border = BorderStroke(2.dp, textColor)
                        boxTextColor = textColor
                    }

                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(boxColor)
                            .border(border ?: BorderStroke(0.dp, Color.Transparent), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = letter,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = boxTextColor
                        )
                    }
                }
            }
        }
    }
}

/**
 * Wirtualna klawiatura ekranowa inspirowana stylem systemu iOS.
 * Dynamicznie wylicza kolory klawiszy (zielony, żółty, szary) na podstawie historii zgadywań.
 *
 * @param isHighContrast Wymusza paletę o wysokim kontraście.
 * @param guesses Zbiór zatwierdzonych prób (służy do wyliczania kolorów klawiszy).
 * @param targetWord Szukane słowo potrzebne do ewaluacji klawiszy.
 * @param onKeyClick Callback po wciśnięciu litery.
 * @param onBackspace Callback po wciśnięciu przycisku usuwania.
 * @param onEnter Callback po wciśnięciu przycisku zatwierdzenia.
 */
@Composable
private fun KeyboardIosStyle(
    isHighContrast: Boolean,
    guesses: List<String>,
    targetWord: String,
    onKeyClick: (String) -> Unit,
    onBackspace: () -> Unit,
    onEnter: () -> Unit
) {
    val rows = listOf(
        listOf("Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P"),
        listOf("A", "S", "D", "F", "G", "H", "J", "K", "L"),
        listOf("Z", "X", "C", "V", "B", "N", "M")
    )

    // Obliczanie najlepszego znanego stanu poszczególnych liter z użytych prób
    val keyStates = remember(guesses, targetWord) {
        val states = mutableMapOf<Char, KeyState>()
        for (guess in guesses) {
            for (i in guess.indices) {
                val char = guess[i]
                val isCorrect = targetWord.getOrNull(i) == char
                val isPresent = targetWord.contains(char)

                val currentState = states[char] ?: KeyState.DEFAULT
                if (isCorrect) {
                    states[char] = KeyState.CORRECT
                } else if (isPresent && currentState != KeyState.CORRECT) {
                    states[char] = KeyState.PRESENT
                } else if (!isPresent && currentState == KeyState.DEFAULT) {
                    states[char] = KeyState.ABSENT
                }
            }
        }
        states
    }

    val defaultBgColor = if (isHighContrast) Color.DarkGray else Color.White
    val defaultTextColor = if (isHighContrast) Color.White else Color.Black
    val actionKeyBgColor = if (isHighContrast) Color.Gray else Color(0xFFDCDCDC)

    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        rows.forEachIndexed { index, row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Przycisk Zatwierdź (Enter) wstawiany po lewej stronie ostatniego wiersza
                if (index == 2) {
                    ActionKey(
                        icon = Icons.Filled.Check,
                        bgColor = actionKeyBgColor,
                        textColor = defaultTextColor,
                        modifier = Modifier.weight(1.5f).padding(end = 4.dp),
                        onClick = onEnter
                    )
                }

                // Generowanie klawiszy numerycznych/literowych z dynamicznym tłem
                row.forEach { letter ->
                    val char = letter.first()
                    val state = keyStates[char] ?: KeyState.DEFAULT

                    val (bgColor, textColor) = when (state) {
                        KeyState.CORRECT -> ColorCorrect to Color.White
                        KeyState.PRESENT -> ColorPresent to Color.Black
                        KeyState.ABSENT -> ColorAbsent to Color.White
                        KeyState.DEFAULT -> defaultBgColor to defaultTextColor
                    }

                    Key(
                        text = letter,
                        bgColor = bgColor,
                        textColor = textColor,
                        modifier = Modifier.weight(1f).padding(horizontal = 3.dp),
                        onClick = { onKeyClick(letter) }
                    )
                }

                // Przycisk Usuń (Backspace) wstawiany po prawej stronie ostatniego wiersza
                if (index == 2) {
                    ActionKey(
                        icon = Icons.Default.Backspace,
                        bgColor = actionKeyBgColor,
                        textColor = defaultTextColor,
                        modifier = Modifier.weight(1.5f).padding(start = 4.dp),
                        onClick = onBackspace
                    )
                }
            }
        }
    }
}

/**
 * Komponent pomocniczy dla pojedynczego klawisza literowego na klawiaturze wirtualnej.
 */
@Composable
private fun Key(
    text: String,
    bgColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.heightIn(min = 40.dp, max = 52.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = bgColor,
            contentColor = textColor
        ),
        elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(text = text, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
    }
}

/**
 * Komponent pomocniczy dla klawiszy funkcyjnych z ikonami (Backspace, Enter).
 */
@Composable
private fun ActionKey(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    bgColor: Color,
    textColor: Color,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.heightIn(min = 40.dp, max = 52.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = bgColor,
            contentColor = textColor
        ),
        elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(24.dp))
    }
}

/**
 * Sekcja wyświetlana po zakończeniu rozgrywki, zastępująca klawiaturę.
 * Informuje użytkownika o liczbie zdobytych punktów lub ujawnia szukane słowo.
 *
 * @param gameStatus Określa w jaki sposób zakończyła się gra.
 * @param targetWord Słowo, którego użytkownik szukał.
 * @param textColor Zmienny kolor tekstu (HC support).
 * @param hintRevealed Flaga mówiąca czy użytkownik użył podpowiedzi (mniejsza punktacja).
 * @param onReset Funkcja resetująca grę do nowej partii.
 * @param onViewLeaderboard Akcja nawigacji do ekranu rankingu.
 */
@Composable
private fun GameOverSection(
    gameStatus: GameStatus,
    targetWord: String,
    textColor: Color,
    hintRevealed: Boolean,
    onReset: () -> Unit,
    onViewLeaderboard: () -> Unit
) {
    Column(
        modifier = Modifier.background(Color.Transparent),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Logika przyznawania punktów za zwycięstwo
        val points = if (hintRevealed) 5 else 10

        val message = when (gameStatus) {
            GameStatus.WON -> stringResource(R.string.game_points_won, points)
            GameStatus.GIVEN_UP -> stringResource(R.string.game_gave_up_msg, targetWord)
            else -> stringResource(R.string.game_word_was, targetWord)
        }

        Text(
            text = message,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onReset,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (MaterialTheme.colorScheme.background == Color(0xFF000000)) MaterialTheme.colorScheme.primary else Color.White,
                contentColor = if (MaterialTheme.colorScheme.background == Color(0xFF000000)) Color.Black else MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.height(60.dp).fillMaxWidth(0.8f),
            shape = RoundedCornerShape(30.dp),
            border = BorderStroke(2.dp, if (MaterialTheme.colorScheme.background == Color(0xFF000000)) Color.Black else MaterialTheme.colorScheme.primary)
        ) {
            Text(stringResource(R.string.game_play_again), fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(12.dp))
        TextButton(onClick = onViewLeaderboard) {
            Text(stringResource(R.string.game_view_leaderboard), color = textColor, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}