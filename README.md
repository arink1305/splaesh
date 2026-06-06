# Splæsh - Team 41

Dette er Team 41 sitt prosjekt i faget IN2000.
Appen gir brukeren en interaktiv oversikt over badeplasser i Norge, kombinert med sanntids værdata, sjøtemperaturer, UV-varsel, badeforhold-score, anbefalinger og viktige farevarsler fra Meteorologisk institutt.

---

## Hvordan kjøre appen

For å kjøre appen på din egen maskin, følger du disse stegene:

1. Last ned Android Studio (helst nyeste versjon)
2. Last ned prosjektmappen eller klon repositoriet til din lokale maskin via Git:
 
```bash
   git clone https://github.com/arink1305/splaesh.git
   ```

3. Åpne prosjektet i Android Studio
4. Legg inn dine egne Mapbox-verdier i `local.properties`
5. La Android Studio laste ned nødvendige avhengigheter og fullføre Gradle Sync
6. Koble til en fysisk Android-enhet eller opprett en emulator, og trykk på **Run**

### Mapbox-token

Denne offentlige versjonen av prosjektet inneholder ikke ekte Mapbox-verdier i repoet.
Du kan ta utgangspunkt i `local.properties.example`, og legge disse inn i din egen `local.properties`:

```properties
MAPBOX_ACCESS_TOKEN=your_public_mapbox_access_token
MAPBOX_DOWNLOADS_TOKEN=your_mapbox_downloads_token
```

- `MAPBOX_ACCESS_TOKEN` brukes av selve appen ved runtime.
- `MAPBOX_DOWNLOADS_TOKEN` brukes av Gradle for å hente Mapbox-avhengigheter.

Hvis du bare vil teste appen som bruker, anbefales APK-en under GitHub `Releases` i stedet for lokal bygging fra kildekode.

---

## Krav og tillatelser

Appen krever kontinuerlig internettilgang for å hente værdata fra MET.no, UV-data fra Open-Meteo, og for å laste inn kartet og WMS-kartlag.
Appen kan også bruke brukerens GPS-posisjon for å vise live posisjon på kartet og gi anbefalinger til nærliggende badeplasser. Derfor ber appen om lokasjonstilgang.

---

## Biblioteker og arkitektur

Appen følger anbefalt Android-arkitektur med Jetpack Compose for brukergrensesnitt, Coroutines for asynkrone oppgaver og ViewModel for tilstandshåndtering.
Til nettverkskall mot API-ene brukes Retrofit med Gson-konvertering.

JUnit-biblioteket brukes til enhetstesting av appen. 

### Mapbox Maps SDK for Android (Compose Extension)

Et kartbibliotek. Vi trengte et kart som støtter Jetpack Compose direkte, og som lar oss legge API-data, som farevarsler i form av polygoner, og WMS-lag, som værkart for temperatur, nedbør og vind, oppå kartet sømløst.
Mapbox gir oss også mulighet til å bytte mellom lys og mørk kartstil for en bedre brukeropplevelse, samt vise live posisjon på kartet.

### Coil (Coil-Compose)

Et bibliotek for bildeinnlasting i Android, bygget spesielt for Kotlin og Coroutines. Biblioteket brukes i Favoritter-skjermen (`FavoritesScreen`) for å asynkront laste ned og vise bilder av badeplassene fra nettadresser (`AsyncImage`). Dette forhindrer at appen fryser mens den venter på at store bilder skal lastes ned.

### Kotlinx Serialization

Kotlins offisielle bibliotek for å gjøre data om til JSON og motsatt. Vi bruker dette i `LocationRepository` for å lese og parse den lokale `badeplasser.json`-filen vår. Det er raskt og integrerer godt med Kotlin sine egne dataklasser.

### OkHttp og Logging Interceptor

OkHttp er et bibliotek som brukes sammen med Retrofit for å håndtere HTTP-kallene appen gjør mot API-ene. Det gir oss mer kontroll over nettverksforespørsler, for eksempel ved å legge til nødvendige headere. 
Logging Interceptor brukes for å logge nettverkskall under utvikling, slik at vi lettere kan se hvilke forespørsler som sendes og hvilke svar appen får tilbake.

### Google Play Services Location

Google Play Services Location brukes for å hente brukerens GPS-posisjon. 
Dette gjør at appen kan vise brukerens live posisjon på kartet, og brukes også som grunnlag for funksjoner som anbefalinger av nærliggende badeplasser. Siden dette biblioteket bruker lokasjon, krever appen at brukeren gir lokasjonstillatelse.

---

## API-er som er brukt

- **Meteorologisk institutt (Locationforecast 2.0):** For lufttemperatur, vind og nedbør.
- **Meteorologisk institutt (Oceanforecast 2.0):** For sjøtemperatur og bølgehøyde.
- **Meteorologisk institutt (WMS-kartlag):** For å vise værdata direkte oppå kartet.
- **Meteorologisk institutt (MetAlerts 2.0):** For gjeldende farevarsler, inkludert polygoner på kart.
- **Open-Meteo API:** For live og daglig maksmåling av UV-indeks.
