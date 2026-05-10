package com.zst.senior.assistant.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.ui.res.stringResource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zst.senior.assistant.R

/**
 * Niestandardowy komponent pola tekstowego (oparty na [OutlinedTextField]),
 * ujednolicający wygląd i zachowanie inputów w całej aplikacji.
 * * Zawiera wbudowaną logikę kompatybilności wstecznej dla opcji klawiatury – jeśli
 * nie podano [keyboardOptions], generuje je automatycznie na podstawie przekazanego [keyboardType].
 *
 * @param value Aktualny tekst wpisany w polu.
 * @param onValueChange Funkcja wywoływana przy każdej zmianie tekstu przez użytkownika.
 * @param label Etykieta pola tekstowego wyświetlana jako placeholder/nad polem.
 * @param modifier Modyfikator układu dla pola. Domyślnie rozszerza się na pełną szerokość ([fillMaxWidth]).
 * @param enabled Określa, czy pole tekstowe jest aktywne i przyjmuje interakcje.
 * @param leadingIcon Opcjonalna ikona wyświetlana na początku pola.
 * @param trailingIcon Opcjonalna ikona wyświetlana na końcu pola.
 * @param visualTransformation Pozwala na wizualną zmianę tekstu (np. ukrywanie znaków hasła). Domyślnie brak.
 * @param keyboardType Typ klawiatury do wyświetlenia. Pozostawiony dla kompatybilności ze starszym kodem.
 * @param keyboardOptions Opcje konfigurujące zachowanie klawiatury wirtualnej. Jeśli null, używany jest [keyboardType].
 * @param keyboardActions Akcje wywoływane przez klawiaturę (np. przycisk "Dalej", "Gotowe").
 * @param colors Kolorystyka pola tekstowego we wszystkich jego stanach (aktywne, błąd, wyłączone itd.).
 */
@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,

    // --- FIX: Przywracamy parametr keyboardType dla kompatybilności ---
    // Dzięki temu linijki z "keyboardType = ..." przestaną wywalać błąd
    keyboardType: KeyboardType = KeyboardType.Text,

    // --- FIX: Dodajemy keyboardOptions jako opcjonalny (nullable) ---
    // Dzięki temu linijki z "keyboardOptions = ..." zaczną działać
    keyboardOptions: KeyboardOptions? = null,
    keyboardActions: KeyboardActions = KeyboardActions.Default,

    colors: TextFieldColors? = null
) {
    val isHighContrast = MaterialTheme.colorScheme.background == Color(0xFF000000)
    val primaryColor = MaterialTheme.colorScheme.primary

    // Domyślne kolory z uwzględnieniem High Contrast
    val finalColors = colors ?: OutlinedTextFieldDefaults.colors(
        focusedBorderColor = primaryColor,
        unfocusedBorderColor = if (isHighContrast) primaryColor.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline,
        focusedLabelColor = primaryColor,
        cursorColor = primaryColor,
        focusedTextColor = if (isHighContrast) Color.White else MaterialTheme.colorScheme.onSurface,
        unfocusedTextColor = if (isHighContrast) Color.White else MaterialTheme.colorScheme.onSurface,
        disabledTextColor = if (isHighContrast) Color.Gray else MaterialTheme.colorScheme.onSurface,
        disabledBorderColor = if (isHighContrast) Color.DarkGray else MaterialTheme.colorScheme.outline,
        disabledLabelColor = if (isHighContrast) Color.Gray else MaterialTheme.colorScheme.onSurfaceVariant,
        disabledLeadingIconColor = if (isHighContrast) Color.Gray else MaterialTheme.colorScheme.onSurfaceVariant,
        disabledTrailingIconColor = if (isHighContrast) Color.Gray else MaterialTheme.colorScheme.onSurfaceVariant,
        focusedContainerColor = if (isHighContrast) Color.Black else Color.White,
        unfocusedContainerColor = if (isHighContrast) Color.Black else Color.White,
        disabledContainerColor = if (isHighContrast) Color.Black else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    )

    // Logika naprawcza:
    // Jeśli podałeś 'keyboardOptions' (nowy kod), używamy ich.
    // Jeśli ich nie podałeś (null), tworzymy je na podstawie starego 'keyboardType'.
    val finalKeyboardOptions = keyboardOptions ?: KeyboardOptions(keyboardType = keyboardType)

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),

        // Przekazujemy gotowe opcje i akcje
        keyboardOptions = finalKeyboardOptions,
        keyboardActions = keyboardActions,

        singleLine = true,
        enabled = enabled,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        visualTransformation = visualTransformation,
        colors = finalColors,
        shape = RoundedCornerShape(16.dp),
        textStyle = TextStyle(
            fontSize = 18.sp,
            fontWeight = FontWeight.Normal
        )
    )
}

/**
 * Niestandardowy komponent pola tekstowego przeznaczony specjalnie do wprowadzania haseł.
 * Opakowuje [CustomTextField], dostarczając wbudowaną funkcjonalność przełączania widoczności
 * hasła (ukrywanie/odkrywanie) za pomocą ikony oka (TrailingIcon).
 *
 * @param value Aktualny tekst (hasło) wpisany w polu.
 * @param onValueChange Funkcja wywoływana przy każdej zmianie tekstu przez użytkownika.
 * @param label Etykieta pola tekstowego.
 * @param enabled Określa, czy pole tekstowe jest aktywne.
 * @param leadingIcon Opcjonalna ikona wyświetlana na początku pola (np. kłódka).
 * @param keyboardOptions Opcje konfigurujące zachowanie klawiatury. Domyślnie ustawione na [KeyboardType.Password].
 * @param keyboardActions Akcje wywoływane przez klawiaturę.
 * @param colors Kolorystyka pola tekstowego.
 */
@Composable
fun CustomPasswordTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    enabled: Boolean = true,
    leadingIcon: @Composable (() -> Unit)? = null,

    // Obsługa nowych parametrów w haśle
    keyboardOptions: KeyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
    keyboardActions: KeyboardActions = KeyboardActions.Default,

    colors: TextFieldColors? = null
) {
    val isHighContrast = MaterialTheme.colorScheme.background == Color(0xFF000000)
    val primaryColor = MaterialTheme.colorScheme.primary

    // Stan zarządzający tym, czy znaki hasła mają być widoczne
    var passwordVisible by remember { mutableStateOf(false) }

    CustomTextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        enabled = enabled,
        leadingIcon = leadingIcon,

        // Przekazujemy parametry dalej - tu używamy keyboardOptions,
        // więc parametr keyboardType w CustomTextField zostanie zignorowany (i dobrze)
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,

        colors = colors,
        // Zamaskowanie tekstu, jeśli hasło jest ukryte
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        // Ikona przełączająca widoczność hasła
        trailingIcon = {
            val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
            val description = if (passwordVisible) stringResource(R.string.comp_text_field_hide_password) else stringResource(R.string.comp_text_field_show_password)

            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(
                    imageVector = image,
                    contentDescription = description,
                    tint = if (isHighContrast) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
    )
}