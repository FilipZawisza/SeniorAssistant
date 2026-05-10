<div align="center">
  <img src="assets/logo.png" alt="Logo aplikacji" width="150">
</div>

# 📱 Senior Assistant

## Profesjonalna aplikacja mobilna wspierająca seniorów i wolontariuszy

**Senior Assistant** to nowoczesna aplikacja mobilna dla systemu
Android, której celem jest usprawnienie komunikacji pomiędzy seniorami a
wolontariuszami, organizowanie codziennej pomocy oraz wspieranie
użytkowników w zarządzaniu obowiązkami (np. przypomnienia o lekach czy
wizytach lekarskich).

Projekt został w całości zaimplementowany w języku Kotlin z
wykorzystaniem środowiska Android Studio oraz systemu budowania Gradle.
Repozytorium jest gotowe do bezpośredniego uruchomienia i dalszego
rozwoju.

------------------------------------------------------------------------

## 📌 Opis projektu

Senior Assistant powstał jako odpowiedź na realne potrzeby osób
starszych, które wymagają wsparcia w codziennych czynnościach oraz
prostego i intuicyjnego sposobu komunikacji z wolontariuszami.

Aplikacja umożliwia:

-   szybkie zgłaszanie próśb o pomoc,
-   oferowanie wsparcia przez wolontariuszy,
-   zarządzanie przypomnieniami,
-   utrzymywanie aktualnych danych profilowych,
-   integrację z asystentem AI (po skonfigurowaniu klucza API).

------------------------------------------------------------------------

## 🚀 Główne funkcjonalności

### 🔐 Rejestracja i logowanie

-   Ekran startowy z podstawowymi informacjami o aplikacji.
-   Rejestracja nowego konta użytkownika.
-   Logowanie do istniejącego konta.
-   Wybór typu profilu: Senior lub Wolontariusz.

### 📝 Zarządzanie profilem

-   Formularz rejestracyjny z podstawowymi danymi.
-   Możliwość edycji danych po utworzeniu konta.
-   Aktualizacja informacji kontaktowych.

### 🏠 Panel główny

Po zalogowaniu użytkownik uzyskuje dostęp do:

-   Dodawania i przeglądania próśb o pomoc (Senior).
-   Przeglądania i realizowania zgłoszeń (Wolontariusz).
-   Ustawiania przypomnień (np. leki, wizyty).
-   Zarządzania profilem i ustawieniami.

------------------------------------------------------------------------

## 👥 Role użytkowników

### 👵 Senior

-   Tworzenie próśb o pomoc.
-   Ustawianie przypomnień.
-   Edycja danych profilowych.

### 🤝 Wolontariusz

-   Przeglądanie dostępnych zgłoszeń.
-   Odpowiadanie na prośby seniorów.
-   Zarządzanie własnym profilem.

### 🔨 Administrator

-   Dostęp do konta administracyjnego.
-   Możliwość testowania funkcjonalności aplikacji.

------------------------------------------------------------------------

## 🧪 Profile testowe

**Senior**\
E-mail: test.senior1@seniorassistant.pl\
Hasło: test123

**Wolontariusz**\
E-mail: test.wolontariusz1@seniorassistant.pl\
Hasło: test123

**Administrator**\
E-mail: test.admin1@seniorassistant.pl\
Hasło: test123

------------------------------------------------------------------------

## 🛠️ Technologie

-   Język programowania: Kotlin\
-   Środowisko: Android Studio\
-   System budowania: Gradle\
-   Minimalna wersja Androida: 7.0 (API 24)

------------------------------------------------------------------------

## 💻 Wymagania systemowe

-   Android Studio (zalecana najnowsza stabilna wersja)
-   Zainstalowane SDK Android 7.0 lub wyższe
-   Emulator Android lub urządzenie fizyczne

------------------------------------------------------------------------

## 📥 Instalacja i uruchomienie

1.  Sklonuj repozytorium:

    git clone https://github.com/FilipZawisza/SeniorAssistant.git

2.  Otwórz projekt w Android Studio.

3.  Poczekaj na zakończenie synchronizacji Gradle.

4.  Skonfiguruj klucz API (instrukcja poniżej).

5.  Uruchom aplikację na emulatorze lub urządzeniu fizycznym.

------------------------------------------------------------------------

## 🔑 Konfiguracja klucza API

Aby aktywować funkcję asystenta AI, należy dodać w pliku
`local.properties` następującą linię:

GEMINI_API_KEY=twoj_klucz_api

Bez tej konfiguracji funkcjonalność asystenta AI nie będzie działać.

------------------------------------------------------------------------

## 📂 Struktura projektu

Projekt posiada standardową strukturę aplikacji Android:

-   app/ -- główny moduł aplikacji\
-   manifests/ -- pliki konfiguracyjne Android\
-   java/ -- kod źródłowy Kotlin\
-   res/ -- zasoby (layouty, grafiki, stringi)\
-   Gradle Scripts -- konfiguracja budowania projektu

------------------------------------------------------------------------

## 📄 Licencja

Projekt przeznaczony do celów edukacyjnych i demonstracyjnych.
