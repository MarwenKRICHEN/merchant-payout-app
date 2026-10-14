# 💳 Merchant Payout App (Android)

A merchant dashboard and payout flow built with **Kotlin** and **Jetpack Compose**, backed by a local mock banking API.

The merchant can:

* see the account balances and the 3 most recent transactions;
* browse the full activity, grouped by day, with infinite scrolling;
* send a payout to an IBAN (GBP or EUR), confirm it and see the outcome;
* have every payout tagged with a stable device identifier;
* authenticate with a fingerprint for payouts over 1,000.00;
* rely on the Payout screens being protected from screenshots.

## 📑 Table of Contents

- [Getting started](#-getting-started)
- [Tech stack](#️-tech-stack)
- [Features](#-features)
- [Architecture](#️-architecture)
- [Principles and patterns](#-principles-and-patterns)
- [Testing](#-testing)
- [Mock API](#-mock-api)
- [Emulator tips](#-emulator-tips)

## 🚀 Getting started

Open the project in Android Studio and run the `app` configuration, or:

```bash
./gradlew :app:installDebug
```

```bash
./gradlew :app:testDebugUnitTest
```

The mock server starts with the app (`InterviewApplication`); every request and response is logged in Logcat under the tag `MockWebServer`. Requires Android 14 (API 34) or later.

## 🛠️ Tech stack

| Concern | Choice |
| --- | --- |
| UI | Jetpack Compose, Material 3 |
| Navigation | Navigation Compose with type-safe `@Serializable` routes |
| Dependency injection | Hilt (`@Provides @Singleton` modules per feature) |
| Networking | Ktor client (OkHttp engine) + kotlinx.serialization |
| Asynchrony | Coroutines, `StateFlow`, `Channel` |
| Local storage | `SharedPreferences` (device id) |
| Security | androidx.biometric `BiometricPrompt`, `FLAG_SECURE`, `ScreenCaptureCallback` |
| Tests | JUnit 4, kotlinx-coroutines-test, Ktor `MockEngine`, hand-written fakes |

## ✨ Features

### Home
Balances (available / pending) and the 3 most recent transactions from `GET /api/merchant`, with loading and error states (retry). **Show More** opens the full activity.

### Activity list
Cursor-based pagination on `GET /api/merchant/activity`: the next page is requested when the user scrolls within 5 items of the end; items are grouped into day sections (*Today*, *Yesterday*, date). Each item shows its type, status and signed amount. Loading, error and end-of-list states exist both for the first page and for each appended page. The list is a full-screen dialog destination over Home.

### Payout
* **Form**: amount + currency (GBP / EUR) + IBAN. The amount accepts digits with one `.` or `,` and at most 2 decimals; the IBAN is validated with its format and the ISO 13616 mod-97 checksum, with an inline error as soon as the user types. **Confirm** is enabled only when both are valid.
* **Confirmation dialog**: amount, currency and masked IBAN (`GB29**************6819`); not dismissible while the request is in flight.
* **Outcome**: *Payout Completed* (with *Create Another Payout*) or *Unable to Process Payout* with the reason: network, service unavailable (503), insufficient funds (400 `INSUFFICIENT_FUNDS`), biometric outcomes, unexpected. *Try Again* goes back to the form with the inputs kept.
* Bottom navigation (Home / Payouts) keeps each tab's state.

### Device identity
`GET /api/devices` is called the first time a payout needs it; the id is then stored in `SharedPreferences` and reused, so it stays stable across launches. A failure is not stored (next payout retries) and the payout is not sent without an id.

### Biometric authentication over 1,000.00
Payouts strictly above 100,000 minor units (1,000.00 in either currency) require strong biometrics before any API call. If no fingerprint is enrolled the payout fails with an **Open Settings** shortcut to the enrolment screen. Cancelling, lockout and other errors abort the payout with their own message. The prompt survives rotation.

### Screenshot protection
The Payout tab sets `FLAG_SECURE` (screenshots and recordings are black, dialogs included) and listens for system screenshots (`Activity.ScreenCaptureCallback`) to show a *Security Reminder* dialog. Both are scoped to the Payout route: Home is captured normally.

## 🏛️ Architecture

Feature-first packages, split in layers where the feature has them. `authentication/` is platform glue rather than a data feature: the pure-Kotlin `AuthenticationProvider` and `AuthenticationBridge` at its root, the Android implementation in `biometric/`, and its Hilt module in `di/`.

```
com.example.androidinterview
├── core/            infrastructure, no feature code
│   ├── network/     Ktor client, getJson / postJson, safeApiCall, HttpException
│   ├── result/      DataResult / DataError
│   ├── format/      date formatting
│   └── time/        Clock provider
├── money/           Money, Currency, formatting (shared by merchant and payout)
├── merchant/        Home + Activity             data / domain / ui / di
├── payout/          Payout form and flow        data / domain / ui / di
├── device/          Device identity             data / domain / di
├── authentication/  Biometric authentication    provider + bridge / biometric / di
├── navigation/      NavHost, routes, bottom bar
├── ui/              theme, security effects, helpers
└── mock/            MockWebServer (provided)
```

```
UI (Compose)  ──▶  ViewModel  ──▶  Use case (when it orchestrates)  ──▶  Repository (interface, domain)
   ▲  state          │ actions                                              │
   └─ StateFlow ◀────┘                                                      ▼
                                                             Default*Repository (data)
                                                             ├── Ktor*RemoteDataSource ──▶ DTOs ──▶ mappers
                                                             └── *LocalDataSource
```

* **domain**: models (`Money`, `Payout`, `Activity`…), repository interfaces, business rules (`IbanValidator`, `AmountParser`, `CreatePayoutUseCase`). Pure Kotlin, tested on the JVM.
* **data**: DTOs, mappers, remote/local data sources, repository implementations. Transport errors are translated here (`DataError` → `PayoutError`).
* **ui**: `*UiState`, `*ViewModel`, a stateful `*Route` and a stateless `*Screen`, small `components/`, and previews.
* **di**: one Hilt module per feature.

`CreatePayoutUseCase` is the only use case because it is the only place that orchestrates several sources: authentication (above the threshold), then the device id, then the payout API. A use case that would only forward to a repository is not created.

## 📐 Principles and patterns

**State and events**
* The ViewModel owns all UI state and handles all user actions; composables are stateless and only render state and forward callbacks. No `remember { mutableStateOf }` for screen state.
* Screen state is a sealed hierarchy that only allows valid combinations, e.g. `PayoutContent` = `Editing(form, confirmation?)` / `Completed(amount)` / `Failed(form, reason)`, wrapped in `PayoutUiState` when screen-wide state (the screenshot reminder) overlays every step.
* State is exposed as `StateFlow`, updated atomically with `update { }`, and collected with `collectAsStateWithLifecycle()`.
* One-shot events (navigation, opening Settings) go through a `Channel` + `receiveAsFlow()` and are collected by a private `*Events` composable with `repeatOnLifecycle(STARTED)`.
* UI state classes are `@Stable`.

**No text in ViewModels**
* Errors reach the UI as per-screen `Reason` types (`Network`, `InsufficientFunds`, `BiometricCancelled`…); the composable maps them to string resources. Money and dates are formatted in the UI.

**Errors as values**
* `safeApiCall` turns exceptions into `DataResult.Failure(DataError)` (`Network`, `Server(code, apiCode)`, `Unknown`) and always rethrows `CancellationException`.
* The data layer maps `DataError` to a feature's domain error using the API contract (`INSUFFICIENT_FUNDS`, `503`); domain results are sealed (`PayoutResult.Success / Failure`).

**Money**
* Amounts are `Long` minor units with a `Currency`; parsing uses `BigDecimal` (never floating point) and rejects more than 2 decimals.

**Networking**
* `getJson` / `postJson` run on `Dispatchers.IO` (resolving the mock base URL touches the network) and decode with a shared `NetworkJson` (`ignoreUnknownKeys`, `explicitNulls = false`).
* API paths and parameter names are `private const val`s next to the data source.

**Platform bridges**
* Code that needs the Activity is kept out of ViewModels and use cases. Biometric authentication is a suspend `AuthenticationProvider.authenticate()`; a singleton `AuthenticationBridge` publishes the pending request, and `BiometricPromptController` (created in `MainActivity.onCreate`, so the library reattaches it after rotation) shows the prompt and answers it.
* Window-level behaviour is expressed as scoped composable effects (`SecureWindowEffect`, `ScreenshotDetectedEffect`), active only while their screen is composed.

**Navigation**
* Type-safe routes; each destination owns its ViewModel through its back stack entry; tabs use `popUpTo(start) { saveState }` + `restoreState` + `launchSingleTop`.

**Conventions**
* KDoc on every declaration, with `@param` for each parameter.
* Previews live in a separate `*Previews.kt` file per screen and cover every state.
* Material 3 default typography and color scheme; colors for amounts and outcomes come from the theme.
* Accessibility: screen titles are headings and payout outcomes are announced (`liveRegion`).

## 🧪 Testing

About 100 JVM unit tests (`app/src/test`), no Android framework required:

| Layer | What is tested |
| --- | --- |
| Domain | `IbanValidator`, `AmountParser`, `CreatePayoutUseCase` (threshold, authentication outcomes, device failures), `MoneyFormat`, date formatting |
| Data | Repositories with fake data sources (mapping, error translation, device id caching / concurrency), Ktor data sources with `MockEngine` (method, path, body, decoding, HTTP errors), `safeApiCall` |
| UI | Every ViewModel: validation, state transitions, double-submit protection, pagination, error reasons, events |
| Platform glue | `AuthenticationBridge` (pending request, cancellation, serialisation), biometric error mapping |

Fakes (`FakePayoutRepository`, `FakeDeviceRepository`, `FakeAuthenticationProvider`…) are preferred over mocking libraries; `MainDispatcherRule` swaps `Dispatchers.Main`. The prompt, the window flag and the screenshot callback are thin platform code verified on the emulator.

## 📡 Mock API

The local MockWebServer (`mock/`) serves:

| Endpoint | Method | Description |
| --- | --- | --- |
| `/api/merchant` | `GET` | `available_balance`, `pending_balance`, `currency`, `activity` (3 most recent) |
| `/api/merchant/activity` | `GET` | Paginated activity: `cursor`, `limit` (default 15) → `{ items, next_cursor, has_more }` |
| `/api/payouts` | `POST` | `{ amount, currency, iban, device_id }` → created payout |
| `/api/payouts/:id` | `GET` | Status of a payout |
| `/api/devices` | `GET` | `{ device_id }` |

Amounts are in the lowest denomination (pence / cents). Error triggers: amount **999.99** → 503 *Service unavailable*, **888.88** → 400 `INSUFFICIENT_FUNDS`. Each request has a random 0.5–2 s latency.

## 📱 Emulator tips

**Fingerprint**
1. Set a screen lock: `adb shell locksettings set-pin 1234`.
2. *Settings → Security → Fingerprint* (or *Open Settings* in the app), then touch the sensor repeatedly until the fingerprint is added: `adb -e emu finger touch 1` or *Extended controls → Fingerprint → Touch Sensor*.
3. When the prompt appears for a payout over 1,000.00, touch with the same finger id; another id is a wrong finger.

**Screenshots**
* `adb shell input keyevent KEYCODE_SYSRQ` takes a system screenshot: on Payouts the image is black and the *Security Reminder* appears; on Home it is captured normally.
* `adb exec-out screencap -p > /tmp/payout_screenshot.png` checks the black capture but is not reported to the app (neither is the emulator toolbar camera).
