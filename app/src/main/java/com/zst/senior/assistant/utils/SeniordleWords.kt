package com.zst.senior.assistant.utils

/**
 * Zbiór słów wykorzystywanych w dedykowanej mini-grze słownej (odpowiedniku popularnego Wordle).
 *
 * Obiekt ten przechowuje statyczną bazę pięcioliterowych haseł, które zostały
 * specjalnie wyselekcjonowane i zoptymalizowane pod kątem dostępności dla osób starszych.
 */
object SeniordleWords {

    /**
     * Baza słów, które w swojej poprawnej, słownikowej formie
     * NIE ZAWIERAJĄ żadnych polskich znaków diakrytycznych (ą, ć, ę, ł, ń, ó, ś, ź, ż).
     * * Lista składa się wyłącznie z naturalnych, pięcioliterowych słów wykorzystujących
     * standardowy alfabet A-Z. Taki zabieg znacząco ułatwia wprowadzanie haseł na
     * klawiaturze ekranowej smartfona, eliminując konieczność przytrzymywania klawiszy
     * w celu wywołania znaków specjalnych.
     */
    val wordsList = listOf(
        // A
        "ADRES", "AGENT", "AKCJA", "AKORD", "ALARM", "ALBUM", "ALEJA", "ALIBI", "ALOES", "AMANT",
        "ANTYK", "ARBUZ", "ARENA", "ARGON", "ATLAS", "AUDYT", "AUTOR",
        // B
        "BABKA", "BAGNO", "BAJKA", "BALON", "BANDA", "BARAN", "BARKA", "BARON", "BASEN", "BATON",
        "BAZAR", "BEKSA", "BILET", "BIURO", "BIUST", "BLASK", "BLOND", "BOMBA", "BRAMA", "BRAWA",
        "BUCIK", "BUDKA", "BULWA", "BURAK", "BURZA", "BUZIA",
        // C
        "CACKO", "CECHA", "CENNY", "CHLEB", "CHORY", "CHWYT", "CIARA", "CIOSY", "CISZA", "CIUCH",
        "COFKA", "CUDNY", "CYKLE", "CYNIK", "CYTAT", "CZARY", "CZORT", "CZYNY",
        // D
        "DACHY", "DAWKA", "DESKA", "DETAL", "DIETA", "DINGO", "DIODA", "DOBRO", "DOBRY", "DOKER",
        "DOLAR", "DOMEK", "DONOS", "DROGA", "DRUKI", "DUCHY", "DUMNY", "DUSZA", "DYWAN", "DZIAD", "DZWON",
        // E
        "EFEKT", "EKRAN", "EKIPA", "ELITA", "EMAIL", "ETAPY", "ETYKA",
        // F
        "FACET", "FAKTY", "FARBA", "FARSA", "FAUNA", "FERMA", "FIGLE", "FILAR", "FILMY", "FIRMA",
        "FLAGA", "FLOTA", "FOKUS", "FOLIA", "FORMA", "FOTEL", "FRAZA", "FRONT", "FURIA",
        // G
        "GACIE", "GADKA", "GAZEL", "GAZIK", "GESTY", "GNIEW", "GOFRY", "GOTYK", "GRABY", "GRACZ",
        "GRONO", "GRUNT", "GRUPA", "GRUZY", "GRYPA", "GRZYB", "GUZIK", "GWARA",
        // H
        "HABIT", "HALKA", "HALNY", "HEROS", "HOBBY", "HOJNY", "HOMER", "HONOR", "HORDA", "HOTEL",
        "HUBKA", "HUMOR", "HYDRA",
        // I
        "IDIOM", "IDOLE", "IKONA", "IMPAS", "INDYK", "INTEL", "ISKRA",
        // J
        "JAJKA", "JAJKO", "JASNY", "JAZDA", "JEDEN", "JENOT", "JUTRO",
        // K
        "KABEL", "KABIN", "KABZA", "KADRA", "KAFEL", "KAJAK", "KAJUT", "KAKAO", "KALKA", "KAMYK",
        "KANAR", "KANON", "KANTY", "KAPKA", "KAPOT", "KARAT", "KARMA", "KARTA", "KASZA", "KATAR",
        "KAWKA", "KEBAB", "KEFIR", "KESON", "KIBEL", "KIBIC", "KILOF", "KIOSK", "KLAPA", "KLASA",
        "KLIMA", "KLIPS", "KLUCZ", "KMIOT", "KOBRA", "KOCYK", "KODER", "KOGUT", "KOJOT", "KOKOS",
        "KOLOR", "KOMIK", "KOMIN", "KONIK", "KONTO", "KOPKA", "KORBA", "KOREK", "KORTY", "KOSZT",
        "KOTKI", "KOWAL", "KOZAK", "KRABY", "KRAJE", "KRATA", "KREDA", "KREPA", "KRETY", "KROKI",
        "KROWA", "KRYPA", "KRYZA", "KRZAK", "KRZYK", "KUBEK", "KUFER", "KULKA", "KUMAK", "KUPON",
        "KURCZ", "KUREK", "KURKA", "KUSZA", "KUZYN", "KWIAT",
        // L
        "LAMPA", "LANCE", "LASER", "LASKI", "LAURA", "LEDWO", "LEGUN", "LEKKO", "LEMON", "LETNI",
        "LEWAK", "LIDER", "LILIE", "LIMBA", "LIMIT", "LINIA", "LIPNY", "LISKI", "LISTY", "LITER",
        "LITRA", "LIZAK", "LOKAL", "LOKUM", "LOTKI", "LOTNY", "LOTUS",
        // M
        "MAGIA", "MAJKA", "MAJOR", "MAKAK", "MAKRO", "MAMMA", "MAMUT", "MANIA", "MARCA", "MAREK",
        "MARKA", "MARSZ", "MASKA", "MATKA", "MEBEL", "MELON", "METKA", "METRO", "MIANO", "MIARA",
        "MIECZ", "MIKRO", "MINOR", "MINUS", "MISIA", "MISKA", "MLEKO", "MNICH", "MNIEJ", "MOCNY",
        "MODEL", "MODNY", "MODUS", "MOJRA", "MOREL", "MORZE", "MOSTY", "MOTOR", "MROKI", "MUCHA",
        "MUREK", "MUZYK", "MYCIE", "MYSZA",
        // N
        "NACJA", "NADAL", "NAFTA", "NAGAN", "NAGLE", "NAGON", "NAKAZ", "NAPAD", "NAPIS", "NARTA",
        "NASYP", "NATKA", "NAUKA", "NAWET", "NAWYK", "NAZWA", "NEONY", "NERKA", "NERWY", "NIEBO",
        "NIEMA", "NIEMY", "NITKA", "NOCNY", "NORKA", "NOSEK", "NOSZE", "NOWAK", "NOWUM", "NUMER", "NURTY",
        // O
        "OBAWA", "OBRAZ", "OBRYS", "OCENA", "OCEAN", "OCZKO", "ODLOT", "OKRES", "OLEJE", "OPARY",
        "OPORY", "ORBIS", "ORDER", "ORGIA", "ORKAN", "OWADY", "OWIES", "OWOCE",
        // P
        "PALEC", "PANDA", "PASJA", "PASEK", "PASMO", "PASZA", "PATYK", "PECHA", "PERON", "PESEL",
        "PIECE", "PIEGI", "PIJAK", "PILNY", "PILOT", "PIONY", "PIRAT", "PISAK", "PISMO", "PIWKO",
        "PLAMY", "PLANY", "PLECY", "PLIKI", "PLONY", "PLOTY", "POBYT", "POKER", "POLAR", "POMOC",
        "POPIS", "PORAD", "PORTO", "PORYW", "POSAG", "PRASA", "PRAWY", "PRZED", "PSIAK", "PTAKI",
        "PUDEL", "PULPA", "PUMPA", "PUNKT", "PUSTY",
        // R
        "RABAT", "RACJA", "RACKA", "RADAR", "RADIO", "RAMKA", "RANGA", "REGON", "REJSY", "REKIN",
        "REMIS", "RENTA", "ROBOL", "ROBOT", "ROLKA", "ROMBY", "RONDO", "ROWER", "ROZUM", "RUCHY",
        "RUMAK", "RUNDA", "RUNKA", "RURKA", "RYBKA", "RYDZE", "RYJEK", "RYNEK", "RYNNY", "RZECZ",
        "RZEKA", "RZEPA",
        // S
        "SALDO", "SALON", "SANIE", "SARNA", "SCENA", "SEKTA", "SENNY", "SERCE", "SEREK", "SERIA",
        "SERUM", "SIANO", "SIBIR", "SILNY", "SILOS", "SIWEK", "SKALP", "SKARB", "SKIBA", "SKLEP",
        "SKOKI", "SMAKI", "SMUGA", "SNOBY", "SNOPY", "SOPEL", "SPORT", "SROKA", "STADO", "STANY",
        "STARY", "START", "STAWY", "STAZA", "STERY", "STOPY", "STRYJ", "SUFIT", "SUITA", "SUMAK",
        "SUMIN", "SUSZA", "SUTKI", "SYROP", "SZAFA", "SZALA", "SZARY", "SZEPT", "SZKIC", "SZLAK",
        "SZLAM", "SZLIF", "SZNUR", "SZOPA", "SZOSA", "SZYBA", "SZYJA", "SZYKI", "SZYNA",
        // T
        "TAKSI", "TALAR", "TALIA", "TAMTA", "TANIO", "TANIE", "TARAN", "TARAS", "TARKA", "TASAK",
        "TATAR", "TATRY", "TEATR", "TEKST", "TEMAT", "TEMPO", "TENIS", "TEREN", "TESCO", "TESTY",
        "TOMIK", "TONUS", "TORBA", "TORTY", "TOWAR", "TRACZ", "TRAKT", "TRAMP", "TRANS", "TRASA",
        "TRAWA", "TRENU", "TRUPA", "TUMAN", "TUNEL", "TURBO", "TUREK", "TUSZA", "TWARZ", "TWOJE",
        "TWORY", "TYLKO", "TYTAN",
        // U
        "UGORY", "ULICA", "UMOWA", "URAZY", "URODA", "UROKI", "USZKO", "UWAGA",
        // W
        "WABIK", "WACIK", "WAGON", "WALEC", "WALKA", "WALOR", "WANDA", "WAPNO", "WARAN", "WARGA",
        "WARKA", "WARTA", "WASAL", "WAZON", "WCZAS", "WDECH", "WIATA", "WICIE", "WIDEO", "WIDOK",
        "WIGOR", "WILKI", "WINDA", "WITAM", "WIZJA", "WNUKI", "WOBEC", "WOJNA", "WOREK", "WORKI",
        "WROGI", "WROTA", "WRYTY", "WUJEK", "WYCIE", "WYDMA", "WYKAZ", "WYKON", "WYLOT", "WYNIK",
        "WYPIS", "WYRAZ", "WYSPA",
        // Z
        "ZAKAZ", "ZAKUP", "ZAMEK", "ZAPAS", "ZAPIS", "ZARYS", "ZBIEG", "ZEBRA", "ZEGAR", "ZENIT",
        "ZGAGA", "ZGODA", "ZIMNY", "ZJAWA", "ZJAZD", "ZMORA", "ZNANY", "ZUCHY", "ZULUS", "ZUPKA",
        "ZWROT"
    )
}