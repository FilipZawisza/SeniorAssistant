package com.zst.senior.assistant.model

/**
 * Lekki model referencyjny zlecenia, używany przy listowaniu i obliczeniach odległości.
 *
 * Służy do szybkiego przesyłania podstawowych informacji o zleceniu, minimalizując zużycie danych.
 *
 * @property id Unikalny identyfikator zlecenia w bazie.
 * @property seniorId ID seniora, który utworzył zlecenie.
 * @property wolontariuszId ID wolontariusza przypisanego do zlecenia (może być null, jeśli wolne).
 * @property opis Krótki opis potrzeby seniora.
 * @property status Obecny stan zlecenia (np. "Aktywne", "Zakończone").
 * @property seniorLat Szerokość geograficzna lokalizacji seniora w momencie zgłoszenia.
 * @property seniorLng Długość geograficzna lokalizacji seniora w momencie zgłoszenia.
 * @property miasto Nazwa miasta, w którym zgłoszono zlecenie.
 * @property timestamp Czas utworzenia zlecenia w milisekundach.
 * @property odlegloscOdWolontariuszaKm Pole wyliczane dynamicznie, przechowujące dystans do wolontariusza.
 */
data class ZlecenieRef(
    val id: String,
    val seniorId: String?,
    val wolontariuszId: String?,
    val opis: String?,
    val status: String?,
    val seniorLat: Double? = null,
    val seniorLng: Double? = null,
    val miasto: String = "",
    val timestamp: Long = 0L,
    var odlegloscOdWolontariuszaKm: Double? = null
)

/**
 * Pełny model zlecenia przeznaczony dla widoku administratora.
 *
 * Zawiera komplet danych umożliwiający nadzór nad realizacją zlecenia.
 *
 * @property zlecenieId ID dokumentu zlecenia.
 * @property seniorImieNazwisko Pełne imię i nazwisko seniora.
 * @property adresZProfilu Stały adres zamieszkania seniora pobrany z jego profilu.
 * @property adresAktualny Adres dynamiczny, jeśli senior zgłosił zlecenie z innej lokalizacji.
 * @property seniorTelefon Numer kontaktowy do seniora.
 * @property opisZlecenia Szczegółowy opis prośby o pomoc.
 * @property status Aktualny status (np. "Oczekujące", "W realizacji").
 * @property seniorId Unikalny identyfikator seniora.
 * @property timestamp Czas utworzenia.
 */
data class PelneZlecenie(
    val zlecenieId: String,
    val seniorImieNazwisko: String,
    val adresZProfilu: String,
    val adresAktualny: String?,
    val seniorTelefon: String,
    val opisZlecenia: String,
    val status: String,
    val seniorId: String,
    val timestamp: Long = 0L
)

/**
 * Perspektywa zlecenia z punktu widzenia Seniora.
 *
 * Zawiera informacje o osobie, która podjęła się pomocy.
 *
 * @property zlecenieId ID zlecenia.
 * @property opis Treść zgłoszonej prośby.
 * @property status Stan zlecenia.
 * @property wolontariuszImieNazwisko Dane wolontariusza realizującego zlecenie.
 * @property wolontariuszTelefon Kontakt do wolontariusza.
 * @property wolontariuszOcena Średnia ocena wolontariusza w systemie.
 * @property wolontariuszLiczbaZlecen Całkowita liczba zleceń ukończonych przez tego wolontariusza.
 * @property timestamp Czas utworzenia.
 */
data class PelneZlecenieDlaSeniora(
    val zlecenieId: String,
    val opis: String,
    val status: String,
    val wolontariuszImieNazwisko: String,
    val wolontariuszTelefon: String,
    val wolontariuszOcena: Double = 0.0,
    val wolontariuszLiczbaZlecen: Int = 0,
    val timestamp: Long = 0L
)

/**
 * Perspektywa zlecenia dedykowana dla Wolontariusza.
 *
 * Zawiera dane kontaktowe i lokalizacyjne niezbędne do dotarcia do seniora.
 *
 * @property zlecenieId ID zlecenia.
 * @property opis Szczegóły zadania.
 * @property seniorImieNazwisko Imię i nazwisko osoby potrzebującej pomocy.
 * @property seniorAdresStały Domowy adres seniora.
 * @property seniorAdresAktualny Dokładne miejsce, z którego wysłano prośbę (jeśli inne niż dom).
 * @property seniorTelefon Numer do kontaktu w razie trudności z odnalezieniem seniora.
 * @property status Obecny stan.
 * @property timestamp Czas utworzenia.
 */
data class PelneZlecenieDlaWolontariusza(
    val zlecenieId: String,
    val opis: String,
    val seniorImieNazwisko: String,
    val seniorAdresStały: String,
    val seniorAdresAktualny: String?,
    val seniorTelefon: String,
    val status: String,
    val timestamp: Long = 0L
)
