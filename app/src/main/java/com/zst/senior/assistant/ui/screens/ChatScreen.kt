package com.zst.senior.assistant.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CommentsDisabled
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NoAccounts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.zst.senior.assistant.R
import com.zst.senior.assistant.model.UserRole
import com.zst.senior.assistant.navigation.safePopBackStack
import com.zst.senior.assistant.ui.theme.BrandBlue
import com.zst.senior.assistant.viewmodel.AuthViewModel
import com.zst.senior.assistant.viewmodel.ChatMessage
import com.zst.senior.assistant.viewmodel.ChatViewModel
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Główny ekran ogólnego czatu aplikacji.
 *
 * Wyświetla listę zaszyfrowanych (AES-256-GCM) wiadomości w czasie rzeczywistym.
 * Komponent ładuje wiadomości za pomocą [ChatViewModel], obsługuje automatyczne
 * przewijanie listy w dół po dodaniu nowych wiadomości oraz oferuje pole tekstowe
 * do wprowadzania i wysyłania treści. Daje również dostęp do opcji zarządzania
 * wiadomościami (usuwanie, wyciszanie, blokowanie) po przytrzymaniu (long-click).
 *
 * @param navController Kontroler nawigacji do zarządzania przejściami między ekranami.
 * @param authViewModel ViewModel udostępniający dane obecnie zalogowanego użytkownika (rola, email, nazwa).
 * @param chatViewModel ViewModel czatu, domyślnie tworzony przez fabrykę [viewModel]. Zarządza stanem wiadomości.
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    chatViewModel: ChatViewModel = viewModel()
) {
    val messages by chatViewModel.messages.collectAsState()
    val currentUserName by authViewModel.currentUserName.collectAsState()
    val currentUserEmail by authViewModel.currentUserEmail.collectAsState()
    val currentUserRole by authViewModel.userRole.collectAsState()

    var text by rememberSaveable { mutableStateOf("") }
    val listState = rememberLazyListState()
    val isDarkTheme = MaterialTheme.colorScheme.background == Color.Black

    // Wcześniejsze pobranie zasobów tekstowych
    val anonUserNameText = stringResource(R.string.chat_user_anon)

    // Stan dla dialogu akcji
    var selectedMessage by rememberSaveable { mutableStateOf<ChatMessage?>(null) }
    var showActionDialog by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(stringResource(R.string.chat_title), fontWeight = FontWeight.Bold)
                            Spacer(Modifier.width(8.dp))
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = stringResource(R.string.chat_encrypted_msg),
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                            )
                        }
                        Text(
                            stringResource(R.string.chat_encrypted_msg),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                            fontSize = 10.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.safePopBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.login_back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(messages) { message ->
                    MessageBubble(
                        message = message,
                        isMyMessage = message.userName == currentUserName,
                        isDarkTheme = isDarkTheme,
                        onLongClick = {
                            selectedMessage = message
                            showActionDialog = true
                        }
                    )
                }
            }
            MessageInputField(text, { text = it }, {
                if (text.isNotBlank()) {
                    val userName = currentUserName ?: anonUserNameText
                    chatViewModel.sendMessage(text, userName, currentUserEmail ?: "")
                    text = ""
                }
            }, isDarkTheme)
        }

        if (showActionDialog && selectedMessage != null) {
            ChatActionDialog(
                message = selectedMessage!!,
                isAdmin = currentUserRole == UserRole.ADMIN.collectionName,
                isOwnMessage = selectedMessage?.userName == currentUserName,
                chatViewModel = chatViewModel,
                authViewModel = authViewModel,
                onDelete = {
                    chatViewModel.deleteMessage(selectedMessage!!.id)
                    showActionDialog = false
                },
                onMute = { hours ->
                    chatViewModel.muteUser(selectedMessage!!.userEmail, hours)
                    showActionDialog = false
                },
                onUnmute = {
                    chatViewModel.unmuteUser(selectedMessage!!.userEmail)
                    showActionDialog = false
                },
                onBan = { days ->
                    authViewModel.banUserByEmail(selectedMessage!!.userEmail, days)
                    showActionDialog = false
                },
                onUnban = {
                    authViewModel.unbanUserByEmail(selectedMessage!!.userEmail)
                    showActionDialog = false
                },
                onDismiss = { showActionDialog = false }
            )
        }
    }
}

/**
 * Komponent renderujący pojedynczy dymek z wiadomością (tzw. Message Bubble).
 *
 * Dostosowuje swoje położenie, kolory oraz kształt zależnie od tego,
 * czy autorem wiadomości jest obecny użytkownik, czy inna osoba z czatu.
 * Obsługuje interakcję długiego kliknięcia (long click).
 *
 * @param message Obiekt [ChatMessage] zawierający treść wiadomości, nazwę użytkownika oraz znacznik czasu.
 * @param isMyMessage Flaga informująca, czy wiadomość została wysłana przez aktualnie zalogowanego użytkownika.
 * @param isDarkTheme Flaga informująca, czy aplikacja jest aktualnie w trybie ciemnym.
 * @param onLongClick Callback wywoływany w przypadku przytrzymania wiadomości palcem (uruchamia np. dialog akcji).
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageBubble(
    message: ChatMessage,
    isMyMessage: Boolean,
    isDarkTheme: Boolean,
    onLongClick: () -> Unit
) {
    val sdf = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    val bubbleColor = when {
        isMyMessage && isDarkTheme -> MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
        isMyMessage && !isDarkTheme -> BrandBlue
        !isMyMessage && isDarkTheme -> MaterialTheme.colorScheme.surface
        else -> Color.White
    }
    val textColor = if (isMyMessage) Color.White else MaterialTheme.colorScheme.onSurface

    val shape = if (isMyMessage) {
        RoundedCornerShape(20.dp, 20.dp, 4.dp, 20.dp)
    } else {
        RoundedCornerShape(20.dp, 20.dp, 20.dp, 4.dp)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = if (isMyMessage) 48.dp else 0.dp,
                end = if (isMyMessage) 0.dp else 48.dp
            ),
        horizontalAlignment = if (isMyMessage) Alignment.End else Alignment.Start
    ) {
        Card(
            shape = shape,
            colors = CardDefaults.cardColors(containerColor = bubbleColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.combinedClickable(
                onClick = {},
                onLongClick = onLongClick
            )
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                if (!isMyMessage) {
                    Text(
                        text = message.userName,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
                Text(text = message.text, style = MaterialTheme.typography.bodyLarge, color = textColor)
            }
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = sdf.format(message.timestamp.toDate()),
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
    }
}

/**
 * Dialog administracyjny/zarządzający dla klikniętej wiadomości.
 *
 * Umożliwia zwykłemu użytkownikowi usunięcie swojej wiadomości.
 * W przypadku ról z wyższymi uprawnieniami (np. Admin), daje pełną kontrolę
 * prewencyjno-moderacyjną (usuwanie wiadomości, tymczasowe/trwałe wyciszanie lub blokowanie konta).
 *
 * @param message Wybrana wiadomość, dla której otwarto menu opcji.
 * @param isAdmin Wskazuje, czy obecnie zalogowany użytkownik posiada rolę Administratora.
 * @param isOwnMessage Wskazuje, czy wiadomość została napisana przez obecnie zalogowanego użytkownika.
 * @param chatViewModel Zapewnia dostęp do logiki dotyczącej moderacji wiadomości/użytkowników.
 * @param authViewModel Zapewnia dostęp do globalnego zarządzania stanem banów użytkowników.
 * @param onDelete Funkcja wywoływana po wybraniu opcji usunięcia wiadomości.
 * @param onMute Funkcja wywoływana po wybraniu czasu wyciszenia wybranego użytkownika.
 * @param onUnmute Funkcja wywoływana do ściągnięcia wyciszenia z użytkownika.
 * @param onBan Funkcja wywoływana do nałożenia bana globalnego (null = permanentny).
 * @param onUnban Funkcja wywoływana do odbanowania użytkownika.
 * @param onDismiss Funkcja zamykająca dialog bez podejmowania akcji.
 */
@Composable
fun ChatActionDialog(
    message: ChatMessage,
    isAdmin: Boolean,
    isOwnMessage: Boolean,
    chatViewModel: ChatViewModel,
    authViewModel: AuthViewModel,
    onDelete: () -> Unit,
    onMute: (Int) -> Unit,
    onUnmute: () -> Unit,
    onBan: (Int?) -> Unit,
    onUnban: () -> Unit,
    onDismiss: () -> Unit
) {
    var isCurrentlyMuted by rememberSaveable { mutableStateOf(false) }
    var isCurrentlyBanned by rememberSaveable { mutableStateOf(false) }
    var showMuteOptions by rememberSaveable { mutableStateOf(false) }
    var showBanOptions by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(message.userEmail) {
        isCurrentlyMuted = chatViewModel.isUserMuted(message.userEmail)
        isCurrentlyBanned = authViewModel.isUserBanned(message.userEmail)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (showMuteOptions) stringResource(R.string.chat_dialog_mute) else if (showBanOptions) stringResource(R.string.chat_dialog_ban) else stringResource(R.string.chat_dialog_title)) },
        text = {
            Column {
                Text(stringResource(R.string.chat_user_label, message.userName))
                Text(
                    text = "\"" + if (message.text.length > 50) message.text.take(50) + "..." else message.text + "\"",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        },
        confirmButton = {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (showMuteOptions) {
                    Text(stringResource(R.string.chat_mute_question), fontWeight = FontWeight.Bold)
                    listOf(
                        1 to stringResource(R.string.chat_time_hour),
                        2 to stringResource(R.string.chat_time_hours, 2),
                        24 to stringResource(R.string.chat_time_hours, 24),
                        48 to stringResource(R.string.chat_time_hours, 48)
                    ).forEach { (h, label) ->
                        OutlinedButton(onClick = { onMute(h) }, modifier = Modifier.fillMaxWidth()) {
                            Text(label)
                        }
                    }
                    TextButton(onClick = { showMuteOptions = false }, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.profile_cancel))
                    }
                } else if (showBanOptions) {
                    Text(stringResource(R.string.chat_ban_question), fontWeight = FontWeight.Bold)
                    listOf(
                        1 to stringResource(R.string.chat_time_day),
                        7 to stringResource(R.string.chat_time_days, 7),
                        30 to stringResource(R.string.chat_time_days, 30)
                    ).forEach { (d, label) ->
                        OutlinedButton(onClick = { onBan(d) }, modifier = Modifier.fillMaxWidth()) {
                            Text(label)
                        }
                    }
                    OutlinedButton(onClick = { onBan(null) }, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.chat_time_forever))
                    }
                    TextButton(onClick = { showBanOptions = false }, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.profile_cancel))
                    }
                } else {
                    if (isAdmin || isOwnMessage) {
                        Button(
                            onClick = onDelete,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.chat_delete_msg))
                        }
                    }

                    if (isAdmin && !isOwnMessage) {
                        if (isCurrentlyMuted) {
                            OutlinedButton(onClick = onUnmute, modifier = Modifier.fillMaxWidth()) {
                                Icon(Icons.AutoMirrored.Filled.Comment, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text(stringResource(R.string.chat_unmute_user))
                            }
                        } else {
                            Button(onClick = { showMuteOptions = true }, modifier = Modifier.fillMaxWidth()) {
                                Icon(Icons.Default.CommentsDisabled, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text(stringResource(R.string.chat_mute_user))
                            }
                        }

                        if (isCurrentlyBanned) {
                            OutlinedButton(onClick = onUnban, modifier = Modifier.fillMaxWidth()) {
                                Icon(Icons.Default.NoAccounts, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text(stringResource(R.string.chat_unban_user))
                            }
                        } else {
                            Button(
                                onClick = { showBanOptions = true },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                            ) {
                                Icon(Icons.Default.Gavel, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text(stringResource(R.string.chat_ban_user))
                            }
                        }
                    }
                    TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.chat_close))
                    }
                }
            }
        },
        dismissButton = null
    )
}

/**
 * Komponent paska wprowadzania wiadomości dolnej części ekranu czatu.
 *
 * Zawiera pole tekstowe pozwalające na wprowadzanie wielolinijkowych tekstów
 * (do 4 linii przed przewinięciem), oraz przycisk pozwalający na ich wysłanie.
 * Obsługuje wysyłanie wiadomości również za pomocą akcji na wirtualnej klawiaturze (np. przycisk Wyślij/Gotowe).
 * Reaguje na stany motywu i modyfikuje paletę barw w celu zachowania wysokiego kontrastu i czytelności.
 *
 * @param text Obecna zawartość pola tekstowego.
 * @param onValueChange Funkcja wywoływana przy zmianie zawartości tekstu.
 * @param onSend Funkcja realizująca proces wysłania wiadomości (wywoływana przyciskiem na UI lub na klawiaturze).
 * @param isDarkTheme Flaga określająca motyw (umożliwia dynamiczne dostosowanie tła).
 */
@Composable
fun MessageInputField(
    text: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    isDarkTheme: Boolean
) {
    val backgroundColor = if (isDarkTheme) MaterialTheme.colorScheme.surface else Color.White

    Surface(
        tonalElevation = 3.dp,
        modifier = Modifier.fillMaxWidth(),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .navigationBarsPadding()
                .imePadding(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = text,
                onValueChange = onValueChange,
                placeholder = { Text(stringResource(R.string.chat_hint_input)) },
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp)),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    unfocusedContainerColor = if (isDarkTheme) Color.White.copy(alpha = 0.05f) else Color.Gray.copy(alpha = 0.1f),
                    focusedContainerColor = if (isDarkTheme) Color.White.copy(alpha = 0.05f) else Color.Gray.copy(alpha = 0.1f)
                ),
                maxLines = 4,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Send
                ),
                keyboardActions = KeyboardActions(
                    onSend = {
                        if (text.isNotBlank()) {
                            onSend()
                        }
                    }
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = onSend,
                enabled = text.isNotBlank(),
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (text.isNotBlank()) BrandBlue else Color.Gray.copy(alpha = 0.3f))
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Wyślij", tint = Color.White)
            }
        }
    }
}