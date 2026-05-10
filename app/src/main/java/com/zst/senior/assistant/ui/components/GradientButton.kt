package com.zst.senior.assistant.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.zst.senior.assistant.ui.theme.AppGradient

/**
 * Niestandardowy komponent przycisku wspierający tło w postaci gradientu oraz
 * automatycznie dostosowujący się do trybu wysokiego kontrastu (HC).
 *
 * ZAKTUALIZOWANO (Krok 2):
 * 1. Usunięto sztywną wysokość (height) na rzecz [defaultMinSize].
 * 2. Zmieniono kształt z procentowego (50) na stały (16.dp), aby przycisk wyglądał dobrze
 * nawet gdy tekst zawinie się do wielu linii.
 * 3. Dodano wewnętrzne paddingi, aby duży tekst nie dotykał krawędzi.
 */
@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null
) {
    // Zmieniamy na stały promień. RoundedCornerShape(50) przy wielu liniach tekstu
    // tworzy dziwny kształt "jajka". 16.dp-24.dp jest bezpieczniejsze dla dostępności.
    val buttonShape = RoundedCornerShape(16.dp)

    // --- DETEKCJA TRYBU HC ---
    val isHighContrast = MaterialTheme.colorScheme.background == Color.Black

    // --- LOGIKA KOLORÓW ---
    val contentColor = if (enabled) {
        if (isHighContrast) MaterialTheme.colorScheme.primary else Color.White
    } else {
        Color.Gray
    }

    val borderStroke = if (isHighContrast) {
        val strokeColor = if (enabled) MaterialTheme.colorScheme.primary else Color.Gray
        BorderStroke(2.dp, strokeColor)
    } else {
        null
    }

    val backgroundModifier = if (isHighContrast) {
        Modifier.background(Color.Black, shape = buttonShape)
    } else {
        if (enabled) {
            Modifier.background(brush = AppGradient, shape = buttonShape)
        } else {
            Modifier.background(color = Color.LightGray, shape = buttonShape)
        }
    }

    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            // KLUCZOWA ZMIANA: Zamiast .height(50.dp) używamy minimum.
            // 56.dp to standardowa wysokość dużego przycisku, który seniorom łatwo kliknąć.
            .defaultMinSize(minHeight = 56.dp)
            .then(backgroundModifier)
            .then(
                if (borderStroke != null) Modifier.border(borderStroke, buttonShape) else Modifier
            ),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = contentColor,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = contentColor
        ),
        enabled = enabled,
        shape = buttonShape,
        // Zwiększamy padding, aby przy zawijaniu tekstu przycisk "oddychał"
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    // Ikona również powinna być nieco większa dla seniora
                    modifier = Modifier.size(24.dp),
                    tint = contentColor
                )
                Spacer(modifier = Modifier.width(12.dp))
            }

            Text(
                text = text,
                // Typografia automatycznie przeskalowana przez nasz nowy AppTheme
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = contentColor,
                textAlign = TextAlign.Center,
                // Pozwalamy na zawijanie tekstu, jeśli nie mieści się w jednej linii
                maxLines = Int.MAX_VALUE,
                softWrap = true
            )
        }
    }
}