# EKnjiznica – Sistem za upravljanje digitalne knjižnice
## Informacijski sistemi

## Avtorji

- **70095915 - Miloš Mladenović**
- **63240392 - Luka Đorđević**

## Opis projekta

EKnjiznica je informacijski sistem za upravljanje digitalne knjižnice, ki omogoča učinkovito upravljanje s knjigami, člani, izposojami in rezervacijami. Sistem vključuje:

- **Spletno aplikacijo** (ASP.NET Core) - za knjižničarje in administratorje
- **Android mobilno aplikacijo** - za člane knjižnice
- **REST API** - za komunikacijo med spletno in mobilno aplikacijo

## Tehnologije

### Spletna aplikacija
- ASP.NET Core 8.0
- Entity Framework Core 8.0
- SQL Server (Azure SQL Database)
- ASP.NET Core Identity
- JWT Bearer Authentication
- Bootstrap 5

### Mobilna aplikacija
- Java
- Android SDK
- Retrofit 2.9
- Gson
- Material Design Components

### Deployment
- Microsoft Azure (App Service + SQL Database)

## Struktura projekta

```
SISTEM/
├── EKnjiznica/              # Spletna aplikacija
│   ├── EKnjiznica/
│   │   ├── Controllers/     # MVC in API kontrolerji
│   │   ├── Models/          # Modeli podatkov
│   │   ├── Views/           # Razor views
│   │   ├── Data/            # DbContext in migracije
│   │   └── wwwroot/         # Statične datoteke
│   └── EKnjiznica.sln
│
└── Mobile/                  # Android mobilna aplikacija
    └── app/
        ├── src/main/java/
        │   ├── activities/  # Android aktivnosti
        │   ├── adapters/    # RecyclerView adapterji
        │   ├── api/         # API servisi
        │   ├── models/      # Modeli podatkov
        │   └── utils/       # Pomožne razrede
        └── src/main/res/    # Resursi
```

## Funkcionalnosti in Uporabniške vloge

### Za knjižničarje (Librarian)
- Upravljanje s knjigami (dodajanje, urejanje, brisanje)
- Upravljanje z izposojami (ustvarjanje, vračilo, pregled)
- Upravljanje z rezervacijami (pregled, odobritev)
- Upravljanje z globami (ustvarjanje, označevanje kot plačane)
- Upravljanje s člani
- Pregled statistike
- Samodejno označevanje zapadlih izposoj

### Za člane (Member)
- Pregled kataloga knjig
- Iskanje knjig
- Rezerviranje razpoložljivih knjig
- Pregled lastnih izposoj
- Pregled lastnih rezervacij
- Pregled glob
- Recenziranje knjig (spletna aplikacija)

## Baza podatkov

### Entitete

**Knjiga (Book)**: ID, naslov, avtor, leto izdaje, žanr, razpoložljivost

**Izposoja (Loan)**: ID, ID uporabnika, ID knjige, datum izposoje, datum zapadlosti, datum vračila, status

**Rezervacija (Reservation)**: ID, ID uporabnika, ID knjige, datum rezervacije, datum poteka, status odobritve

**Globa (Fine)**: ID, ID uporabnika, znesek, razlog, datum izdaje, status plačila, datum plačila

**Recenzija (Review)**: ID, ID knjige, ID uporabnika, ocena (1-5), komentar, datum recenzije

## API Endpoints

### Avtentikacija
- `POST /api/AuthApi/login` - Prijava uporabnika
- `POST /api/AuthApi/register` - Registracija novega uporabnika

### Knjige
- `GET /api/BooksApi` - Seznam vseh knjig
- `GET /api/BooksApi/{id}` - Podrobnosti knjige
- `POST /api/BooksApi` - Dodajanje knjige (Librarian)
- `PUT /api/BooksApi/{id}` - Urejanje knjige (Librarian)
- `DELETE /api/BooksApi/{id}` - Brisanje knjige (Librarian)

### Izposoje
- `GET /api/LoansApi` - Vse izposoje (Librarian)
- `GET /api/LoansApi/my` - Moje izposoje (Member)
- `POST /api/LoansApi/create` - Ustvarjanje izposoje (Librarian)
- `POST /api/LoansApi/return/{loanId}` - Vračilo izposoje (Librarian)

### Rezervacije
- `POST /api/ReservationsApi/{bookId}` - Rezerviranje knjige (Member)
- `GET /api/ReservationsApi` - Vse rezervacije (Librarian)
- `GET /api/ReservationsApi/my` - Moje rezervacije (Member)
- `POST /api/ReservationsApi/approve/{reservationId}` - Odobritev rezervacije (Librarian)

## Privzeti uporabniki

**Knjižničar:**
- Email: `librarian@lib.com`
- Geslo: `Test123!`

**Član:**
- Registruju ih knjizničari

## Deployment

Spletna aplikacija je nameščena na **Microsoft Azure**:
- Web App: Azure App Service
- Database: Azure SQL Database
- URL: `https://eknjiznica20260107181458.azurewebsites.net/`