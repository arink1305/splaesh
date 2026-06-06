## Introduksjon
Dette dokumentet er beregnet på utviklere som skal fortsette å jobbe med,
vedlikeholde eller videreutvikle applikasjonen. 
Det beskriver arkitekturen, prosjektstrukturen 
og de viktigste valgene som er gjort underveis i utviklingen,
slik at nye bidragsytere raskt kan sette seg inn i prosjektet og fortsette å jobbe i en konsistent stil.

Appen er en Android-applikasjon skrevet i Kotlin. 
Den lar brukere utforske hardkodede badeplasser i Norge,
se værkartlag og varsler (som stormvarsler) på et interaktivt kart,
lagre favorittsteder på en egen favorittside,
se badeforhold-score for badeplasser,
og få anbefalinger basert på score og brukerens posisjon.

---

## Teknologitabell

| Teknologi           | Formål                   |
|:--------------------|:-------------------------|
| Kotlin              | Promgrammeringsspråk     |
| Jetpack Compose     | UI-rammeverk             |
| Mapbox SDK          | Kart                     |
| MetAlerts 2.0       | API for farevarsler      |
| LocationForcast 2.0 | API for værinfo          |
| Open-Meteo          | API for UV               |
| OceanForecasts 2.0  | API for badetemperatur   |
| Victoria WMS        | API for kartlag          |
| API level 36        | android platform versjon |

---

## Hvorfor valgte vi disse teknologiene 

**Kotlin** Primært programmeringsspråk, Kotlin passer best siden vi jobber i android studio og bruke composable objekter.

**Jetpack Compose** Passer godt til android studio, redusere koding og lettere å lese. 

**Mapbox SDK** Fineste kart api vi fant. Den er en fint kart med navn på områder og ikke for mye farge. Samtidig kan vi bruke mørk og lys tema som vi hadde planlagt.

**MetAlterts 2.0** Brukes til å hente farevarsler. Farevarsler lag på kartet var en av funksjonelle krav som vår aktør hadde i case beskrivelse. 

**LocationForcast 2.0**  Vær info var ikke akkurat funksjonelle krav, likevel tenker vi at de hadde vært fint got brukeren kunne få opp en pent UI for korte værinfo i tillegg til værlag fra victoria.

**Open-Meteo** Vår app er et badeapp, UV er også en viktig info. I likhet med værinfo var ikke dette en funksjonelle krav. UV skal vises i mini UI sammen med værinfo og i dropdown meny i badeplasser.

**OceanForcasts** En av vår viktigste informasjon for bade app. Oceanforcasts brukes til å hente badetemperatur så brukeren kan bestemme om de ha lyst å bade i det stedet dem sjekker. 

**Victoria WMS** API for kartlag til allmennhet kart. I tillegg til å vise badeplasser, skal den hente kartlag for temperature,nedbør og vind. 

**API level 36/Android SDK 36** Dette API-nivå var en minimum bestemt i case beskrivelsen. 

## Mappe Struktur

```text
no.uio.ifi.in2000.aryanma.splaesh
├──MainActivity
│ 
│ 
├── api/
│   ├── MetAlertsApi
│   ├── MetWeatherApi
│   ├── OceanForecastApi
│   ├── RetrofitClient
│   └── UvApi
│
├── data/
│   ├── HomeRepository
│   ├── LocationRepository
│   ├── RecommendationsRepository
│   ├── UserLocationRepository
│   ├── Uv
│   ├── UvRepository
│   └── WarningsRepository
│
├── model/
│   ├── BathingScore
│   ├── BathingScoreProfile
│   ├── ForecastModels
│   ├── Location
│   ├── MetAlertsResponse
│   ├── OceanForecastModels
│   ├── RecommendationPlace
│   ├── SeaInfo
│   └── Warning
│
├── ui/
│   ├── components
│   │   ├── BathingScoreCard
│   │   ├── BeachLiveInfoCards
│   │   ├── InteractiveVictoriaMap
│   │   ├── MapSearchOverlay
│   │   ├── TimeScroller
│   │   ├── UserLocationPuck
│   │   ├── WarningComponents
│   │   ├── WeatherData
│   │   └── ZoomButton
│   ├── favorites
│   │   ├── FavoritesScreen
│   │   └── FavoritesViewModel
│   ├── home
│   │   ├── HomeFilterMenu
│   │   ├── HomeScreen
│   │   ├── HomeScreenPanels
│   │   └── HomesScreenViewModel
│   ├── navigation
│   │   └── AppNavigation
│   ├── recommendations
│   │   ├── RecommendationPlaceCard
│   │   ├── RecommendationsScreen
│   │   └── RecommendationsViewModel
│   ├── settings
│   │   └── SettingsScreen
│   └── theme
│       ├── Color
│       ├── Theme
│       └── Type
│
└── utils/
    ├── BathingScore
    ├── FarevarselPolygon
    ├── ForecastAggregator
    └── WarningSeverityResolver
```
--- 
## Pakkens ansvar

**`MainActivity.kt`**
Appens eneste inngangspunkt.

**`api/`**
Inneholder API-klienter og tjenestegrensesnitt som håndterer kommunikasjon med eksterne datakilder: MET Norway, Victoria WMS og vanntemperatur-APIet.

**`data/`**
Håndterer datatilgang og lagring. Repositories befinner seg her og fungerer som den eneste kilden til sannhet for resten av appen. Her koordineres lokal lagring, datatilgang, anbefalinger og brukerposisjon.

**`models/`**
Inneholder enkle Kotlin-dataklasser som representerer kjerneentitetene i appen — som badeplasser og værvarsler. Disse deles på tvers av lag og har ingen avhengigheter til Android eller UI-rammeverk.

**`ui/`**
All Compose UI-kode. Delt inn i skjermer og `components/` for mindre, gjenbrukbare Compose-komponenter. Her ligger blant annet hjemskjerm, favoritter, anbefalinger og innstillinger.

**`utils/`**
Hjelpekode som ikke tilhører et bestemt lag — for eksempel logikk for å tegne varselpolygoner på kartet, kode for værvarsler og beregning av badeforhold-score.

---
## MVVM 
[![](https://mermaid.ink/img/pako:eNpdkl9v2jAUxb-KdZ-YFCjBJIE8TEKlD31gmgp0Uuc9uM0tZHHsyHa6P5Tvtfd9sV0H0qLlIYnj37nnHDkHeDIFQg47K5s92yyFZnRtb78KoPtQyZ0QenBt6sY4-ajQsSu2rr6jrdF-EPDtxN-vAn9f4o8VjVO9bFuuvfQYsZsX1N6988vFZhEUS-nlmb1Dcii9sSVegIvPXRJ69DNXN5uInJ4CKdmX1ZpWUmuPdXOS9Q3YcPjxVcDJmg0ebVuh3UtdqFLvuvCvlLvPf6bPidnAl8p5gjtse_sfFnI_G4uu-fvHOoU2UKHUe70L8tLpYusTev-CtnKVVCow1PKt9Zm5IwujXT8eIjqosoDc2xYjoDOoZVjCIQgF-D3WKCCn10LaSoDQR9I0Uj8YU_cya9rdHvJnqRyt2qagxstS0i9Qv321qAu016bVHvLpNOmGQH6An5BPUj7iMZ_EPOWczxIewS_I4zgdJWk2i9MkmyaTeJ4eI_jd2Y5H84xPaWs8nqezWcaz4z-ZUMLh?type=png)](https://mermaid.live/edit#pako:eNpdkl9v2jAUxb-KdZ-YFCjBJIE8TEKlD31gmgp0Uuc9uM0tZHHsyHa6P5Tvtfd9sV0H0qLlIYnj37nnHDkHeDIFQg47K5s92yyFZnRtb78KoPtQyZ0QenBt6sY4-ajQsSu2rr6jrdF-EPDtxN-vAn9f4o8VjVO9bFuuvfQYsZsX1N6988vFZhEUS-nlmb1Dcii9sSVegIvPXRJ69DNXN5uInJ4CKdmX1ZpWUmuPdXOS9Q3YcPjxVcDJmg0ebVuh3UtdqFLvuvCvlLvPf6bPidnAl8p5gjtse_sfFnI_G4uu-fvHOoU2UKHUe70L8tLpYusTev-CtnKVVCow1PKt9Zm5IwujXT8eIjqosoDc2xYjoDOoZVjCIQgF-D3WKCCn10LaSoDQR9I0Uj8YU_cya9rdHvJnqRyt2qagxstS0i9Qv321qAu016bVHvLpNOmGQH6An5BPUj7iMZ_EPOWczxIewS_I4zgdJWk2i9MkmyaTeJ4eI_jd2Y5H84xPaWs8nqezWcaz4z-ZUMLh)

---

## UDF
Appen benytter **Unidirectional Data Flow (UDF)** som et arkitekturprinsipp for å sikre en forutsigbar og oversiktlig dataflyt gjennom applikasjonen. UDF innebærer at data alltid flyter i én retning, fra datalag til UI og at brukerhandlinger alltid sendes oppover som events til ViewModel.

**Tilstand flyter nedover:**
ViewModel eksponerer ett `UiState`-objekt som representerer hele tilstanden til en skjerm. Composables observerer dette objektet og tegner seg selv på nytt automatisk når tilstanden endres. UI-laget har aldri ansvar for å endre tilstand direkte.

**Events flyter oppover:**
Når brukeren gjør noe, for eksempel trykker på en badeplass eller legger til en favoritt, sendes dette som en event opp til ViewModel. ViewModel behandler eventen, oppdaterer tilstanden og eksponerer det nye `UiState`-objektet tilbake til UI-et.

Dette mønsteret gir flere fordeler for vedlikehold og videreutvikling:
- **Forutsigbarhet** — siden all tilstand eies av ViewModel og aldri endres direkte av UI, er det alltid tydelig hva som er kilden til en gitt tilstand.
- **Testbarhet** — ViewModel kan testes isolert ved å sende inn events og verifisere at riktig `UiState` produseres.
- **Enkelhet** — nye utviklere trenger kun å forstå to ting per skjerm: hvilke events som kan sendes, og hvilket `UiState` som returneres.

---

## Objektorienterte prinsipper

### Lav kobling

Hvert lag kommuniserer kun med laget direkte under seg. UI-et kjenner til ViewModels, men ikke til repositories eller APIer. ViewModels kjenner til repositories, men ikke til Retrofit eller nettverksdetaljer. Dette gjør at komponenter kan endres eller byttes ut uten å påvirke resten av kodebasen.

Konkret:
- API-klienter i `api/` brukes kun av `data/`-laget.
- Dataklassene i `models/` har ingen Android-avhengigheter, noe som gjør dem portable og enkle å teste.
- `utils/` inneholder frittstående hjelpere uten avhengigheter til UI eller ViewModels.

### Høy kohesjon

Hver pakke og klasse har ett veldefinert ansvar:
- `api/` håndterer kun nettverkskommunikasjon.
- `models/` definerer kun datastrukturer.
- `ui/` definerer kun UI-rammeverk.
- ViewModels håndterer kun UI-tilstand for sin respektive skjerm.

Dette gjør det enkelt å finne hvor en bestemt type logikk hører hjemme, og reduserer risikoen for utilsiktede konsekvenser ved endringer.

---

## Konvensjoner for nye utviklere

Følg dette mønsteret når du legger til ny funksjonalitet:

1. **Legg til en dataklasse** i `models/` hvis en ny entitet er nødvendig.
2. **Legg til en API-klient** i `api/` hvis en ny ekstern datakilde er nødvendig.
3. **Legg til eller utvid et repository** i `data/` for å eksponere dataene.
4. **Opprett eller utvid en ViewModel** som henter data via repositoryet og eksponerer et `UiState`.
5. **Bygg composable** i `ui/` eller `ui/components/` hvis dere skal bruke den flere ganger. 

Hold lagene adskilt. Unngå å referere til API-klienter direkte fra composables eller ViewModels. Gå alltid gjennom repositoryet.
