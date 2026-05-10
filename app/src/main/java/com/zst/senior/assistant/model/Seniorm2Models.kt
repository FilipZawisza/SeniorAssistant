package com.zst.senior.assistant.model

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.DayOfWeek
import java.util.UUID

/**
 * Model danych dla modułu "Seniorm2", reprezentujący cykliczną pozycję (np. przypomnienie o lekach).
 *
 * Klasa przechowuje informacje niezbędne do zarządzania i wyświetlania cyklicznych powiadomień.
 *
 * @property id Unikalny identyfikator pozycji, domyślnie generowany jako UUID.
 * @property name Nazwa pozycji lub opis przypomnienia (np. "Leki rano").
 * @property time Godzina przypomnienia w formacie HH:mm.
 * @property daysOfWeek Zbiór dni tygodnia (DayOfWeek), w których przypomnienie ma być aktywne.
 */
@RequiresApi(Build.VERSION_CODES.O)
data class Seniorm2Item(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val time: String,
    val daysOfWeek: Set<DayOfWeek>
)
