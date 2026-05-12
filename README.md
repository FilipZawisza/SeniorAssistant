<div align="center">
  <img src="assets/logo.png" alt="Senior Assistant Banner" width="100%">
</div>

<h1 align="center">Senior Assistant</h1>

<p align="center">
  Aplikacja mobilna wspierająca seniorów oraz wolontariuszy
</p>

<p align="center">
  Kotlin • Android Studio • Gradle • Android SDK
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android-green" alt="Android">
  <img src="https://img.shields.io/badge/Kotlin-1.9-purple" alt="Kotlin">
  <img src="https://img.shields.io/badge/API-24%2B-blue" alt="API">
  <img src="https://img.shields.io/badge/Status-Development-orange" alt="Status">
</p>

---

## Spis treści

- [O projekcie](#o-projekcie)
- [Cel projektu](#cel-projektu)
- [Główne funkcjonalności](#główne-funkcjonalności)
- [Dostępność](#dostępność)
- [Prezentacja projektu](#prezentacja-projektu)
- [Technologie](#technologie)
- [Wymagania](#wymagania)
- [Uruchomienie projektu](#uruchomienie-projektu)
- [Konta testowe](#konta-testowe)
- [Autorzy](#autorzy)
- [Licencja](#licencja)

---

## O projekcie

**Senior Assistant** to aplikacja mobilna dla systemu Android, której celem jest usprawnienie komunikacji pomiędzy seniorami a wolontariuszami oraz wsparcie użytkowników w codziennym funkcjonowaniu.

Projekt umożliwia zarządzanie zgłoszeniami pomocy, przypomnieniami oraz podstawowymi informacjami użytkownika w prosty i intuicyjny sposób.

Aplikacja została napisana w języku **Kotlin** z wykorzystaniem środowiska **Android Studio** oraz systemu budowania **Gradle**.

---

## Cel projektu

Celem projektu było stworzenie intuicyjnej aplikacji wspierającej osoby starsze w codziennym funkcjonowaniu oraz ułatwiającej organizację pomocy wolontariackiej.

Projekt skupia się na:

- prostocie obsługi,
- czytelnym interfejsie,
- dostępności dla seniorów,
- szybkim kontakcie pomiędzy użytkownikami,
- wsparciu codziennych obowiązków.

---

## Główne funkcjonalności

### System kont użytkowników

- rejestracja i logowanie użytkowników,
- bezpieczne uwierzytelnianie przy użyciu Firebase Authentication,
- wybór typu konta:
  - Senior,
  - Wolontariusz,
  - Administrator,
- obsługa różnych poziomów uprawnień użytkowników,
- możliwość zarządzania profilem użytkownika,
- personalizacja funkcjonalności w zależności od typu konta.

### Funkcjonalności dla seniorów

- tworzenie próśb o pomoc,
- szybki kontakt z opiekunem lub wolontariuszem,
- zarządzanie przypomnieniami,
- harmonogram wizyt lekarskich,
- przypomnienia o przyjmowaniu leków,
- edycja danych profilowych,
- prosty i intuicyjny interfejs dostosowany do seniorów,
- możliwość korzystania z asystenta AI w formie tekstowej i głosowej.

### Funkcjonalności dla wolontariuszy

- przeglądanie zgłoszeń seniorów,
- odpowiadanie na prośby o pomoc,
- monitorowanie statusu zgłoszeń,
- odbieranie powiadomień alarmowych i zgłoszeń SOS,
- możliwość zdalnego wsparcia seniora,
- zarządzanie profilem użytkownika.

### Automatyczny Detektor Upadków

- działanie w tle przy użyciu Android Foreground Service,
- analiza danych z akcelerometru urządzenia,
- wykrywanie potencjalnych upadków seniora,
- uruchamianie procedury alarmowej,
- możliwość anulowania alarmu przez użytkownika,
- automatyczne wysyłanie lokalizacji GPS do opiekuna.

### Integracja AI

- integracja z asystentem AI przy użyciu Gemini API,
- możliwość naturalnej komunikacji z użytkownikiem,
- obsługa pytań tekstowych i głosowych,
- pomoc w codziennych czynnościach,
- wsparcie w obsłudze telefonu i aplikacji,
- obsługa konfiguracji klucza API lokalnie w projekcie,
- możliwość dalszej rozbudowy funkcji AI.

---

## Dostępność

Interfejs aplikacji został zaprojektowany z myślą o seniorach:

- czytelny układ aplikacji,
- duże i intuicyjne elementy interfejsu,
- uproszczona nawigacja,
- ograniczenie zbędnych elementów UI,
- łatwy dostęp do najważniejszych funkcji.

---

## Prezentacja projektu

Szczegółowa prezentacja projektu dostępna jest pod adresem:

**https://showcase.seniorassistant.pl**

---

## Technologie

| Technologia | Zastosowanie |
|---|---|
| Kotlin | Główny język aplikacji |
| Android Studio | Środowisko programistyczne |
| Gradle | System budowania projektu |
| Android SDK | Platforma mobilna |
| Gemini API | Integracja asystenta AI |

---

## Wymagania

Do uruchomienia projektu wymagane są:

- Android Studio,
- Android SDK 24 lub nowszy,
- emulator Androida lub urządzenie fizyczne.

---

## Uruchomienie projektu

### 1. Klonowanie repozytorium

```bash
git clone https://github.com/FilipZawisza/SeniorAssistant.git
```

### 2. Otwarcie projektu

Otwórz projekt w Android Studio.

### 3. Synchronizacja Gradle

Poczekaj na zakończenie synchronizacji zależności.

### 4. Konfiguracja API

W pliku `local.properties` dodaj:

```properties
GEMINI_API_KEY=twoj_klucz_api
```

Bez konfiguracji klucza API funkcjonalności AI pozostaną wyłączone.

### 5. Uruchomienie aplikacji

Uruchom aplikację na emulatorze lub urządzeniu fizycznym.

---

## Konta testowe

### Senior

| Pole | Wartość |
|---|---|
| Email | `test.senior1@seniorassistant.pl` |
| Hasło | `test123` |

### Wolontariusz

| Pole | Wartość |
|---|---|
| Email | `test.wolontariusz1@seniorassistant.pl` |
| Hasło | `test123` |

### Administrator

| Pole | Wartość |
|---|---|
| Email | `test.admin1@seniorassistant.pl` |
| Hasło | `test123` |

---

## Autorzy

Projekt został przygotowany przez:

- Filip Zawisza
- Dominik Szymczak

---

## Licencja

Projekt został przygotowany w celach edukacyjnych i demonstracyjnych.
