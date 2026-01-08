# EKnjiznica Mobile Application

Android mobilna aplikacija za EKnjiznica (Digital Library Management System).

## Funkcionalnosti

Aplikacija pruža iste funkcionalnosti kao web aplikacija:

### Za članove (Members):
- **Pregled knjiga** - Lista svih knjiga u biblioteci
- **Rezervacija knjiga** - Rezervisanje dostupnih knjiga
- **Moje pozajmice** - Pregled aktivnih pozajmica
- **Moje rezervacije** - Pregled statusa rezervacija
- **Moje kazne** - Pregled kazni (kada API endpoint bude dostupan)

### Za bibliotekare (Librarians):
- **Upravljanje knjigama** - Dodavanje, izmena i brisanje knjiga
- **Upravljanje pozajmicama** - Pregled svih pozajmica, vraćanje knjiga
- **Upravljanje rezervacijama** - Pregled i odobravanje rezervacija
- **Upravljanje kaznama** - Pregled i upravljanje kaznama (kada API endpoint bude dostupan)
- **Upravljanje članovima** - (Za buduću implementaciju)

## Konfiguracija

### 1. API Base URL

Pre nego što pokrenete aplikaciju, **MORATE** ažurirati API base URL u fajlu:

`app/src/main/java/com/example/eknjiznica/api/RetrofitClient.java`

Pronađite liniju:
```java
private static final String BASE_URL = "https://your-app-name.azurewebsites.net/";
```

I zamenite sa stvarnom Azure URL adresom vaše web aplikacije, na primer:
```java
private static final String BASE_URL = "https://eknjiznica.azurewebsites.net/";
```

**VAŽNO**: URL mora da se završava sa `/` (slash)!

### 2. Build i Pokretanje

1. Otvorite projekat u Android Studio
2. Sinkronizujte Gradle fajlove
3. Ažurirajte API base URL (vidi gore)
4. Pokrenite aplikaciju na emulatoru ili fizičkom uređaju

## Struktura Projekta

```
app/src/main/java/com/example/eknjiznica/
├── activities/          # Sve aktivnosti aplikacije
│   ├── LoginActivity.java
│   ├── RegisterActivity.java
│   ├── HomeActivity.java
│   ├── BooksActivity.java
│   ├── BookDetailsActivity.java
│   ├── AddEditBookActivity.java
│   ├── MyLoansActivity.java
│   ├── MyReservationsActivity.java
│   ├── MyFinesActivity.java
│   ├── AllLoansActivity.java
│   ├── AllReservationsActivity.java
│   └── AllFinesActivity.java
├── adapters/           # RecyclerView adapteri
│   ├── BookAdapter.java
│   ├── LoanAdapter.java
│   ├── ReservationAdapter.java
│   └── FineAdapter.java
├── api/                # API servisi
│   ├── ApiService.java
│   └── RetrofitClient.java
├── models/             # Modeli podataka
│   ├── Book.java
│   ├── Loan.java
│   ├── Reservation.java
│   ├── Review.java
│   ├── Fine.java
│   ├── ApiResponse.java
│   ├── LoginRequest.java
│   ├── RegisterRequest.java
│   └── LoginResponse.java
└── utils/              # Pomoćne klase
    └── SharedPreferencesHelper.java
```

## API Endpoints

Aplikacija koristi sledeće API endpoint-e:

### Autentifikacija
- `POST /api/AuthApi/login` - Prijava
- `POST /api/AuthApi/register` - Registracija

### Knjige
- `GET /api/BooksApi` - Lista svih knjiga
- `GET /api/BooksApi/{id}` - Detalji knjige
- `POST /api/BooksApi` - Dodavanje knjige (Librarian)
- `PUT /api/BooksApi/{id}` - Izmena knjige (Librarian)
- `DELETE /api/BooksApi/{id}` - Brisanje knjige (Librarian)

### Pozajmice
- `GET /api/LoansApi` - Sve pozajmice (Librarian)
- `GET /api/LoansApi/my` - Moje pozajmice (Member)
- `POST /api/LoansApi/create` - Kreiranje pozajmice (Librarian)
- `POST /api/LoansApi/return/{loanId}` - Vraćanje pozajmice (Librarian)

### Rezervacije
- `POST /api/ReservationsApi/{bookId}` - Rezervisanje knjige (Member)
- `GET /api/ReservationsApi` - Sve rezervacije (Librarian)
- `GET /api/ReservationsApi/my` - Moje rezervacije (Member)
- `POST /api/ReservationsApi/approve/{reservationId}` - Odobravanje rezervacije (Librarian)

### Kazne
- **Napomena**: API endpoint-i za kazne još nisu implementirani u web aplikaciji. Aktivnosti su pripremljene i biće funkcionalne kada API bude dostupan.

## Zavisnosti

Aplikacija koristi sledeće biblioteke:

- **Retrofit 2.9.0** - Za HTTP pozive
- **Gson 2.10.1** - Za JSON parsiranje
- **Material Design Components** - Za UI komponente
- **RecyclerView** - Za liste
- **CardView** - Za kartice

## Autentifikacija

Aplikacija koristi JWT token za autentifikaciju. Token se čuva u SharedPreferences i automatski se dodaje u header svakog API poziva.

## Test Korisnici

Prema web aplikaciji, postoji default bibliotekar:
- **Email**: librarian@lib.com
- **Password**: Test123!

Novi korisnici se registruju kroz aplikaciju i automatski dobijaju ulogu "Member".

## Napomene

1. **Fines API**: Aktivnosti za kazne su pripremljene, ali će biti potpuno funkcionalne kada se API endpoint-i dodaju u web aplikaciju.

2. **Reviews**: Funkcionalnost za recenzije knjiga je pripremljena u modelima, ali API endpoint-i još nisu dostupni.

3. **Network Security**: Ako testirate na Android 9+ sa HTTP (ne HTTPS), možda ćete morati da konfigurišete network security config.

## Razvoj

Za dalji razvoj:
1. Dodati API endpoint-e za Fines i Reviews u web aplikaciju
2. Implementirati funkcionalnost za upravljanje članovima
3. Dodati push notifikacije za obaveštenja
4. Implementirati offline podršku
