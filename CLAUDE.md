# SoftWork — Employee Client (Elysium)

Native Android client for the **Employee/Worker** side of Elysium's SoftWork product. This
repository is **exclusively** the Employee experience; the HR/Admin client lives elsewhere.

---

## Tech Stack (Locked)

| Concern        | Choice                                            |
|----------------|---------------------------------------------------|
| Language       | Kotlin (2.3.21) — `JavaVersion.VERSION_11`        |
| UI             | Jetpack Compose + Material 3 (BOM 2026.05.00)     |
| Navigation     | androidx.navigation:navigation-compose 2.9.8      |
| Networking     | Retrofit 3 + Converter-Gson                       |
| Persistence    | Room 2.8.4 (KSP)                                  |
| Images         | Coil 3                                            |
| Fonts          | Compose Google Fonts (Exo)                        |
| State (UI)     | androidx.lifecycle 2.10.0 — `viewmodel-compose` + `runtime-compose` |
| i18n           | AppCompat 1.7.x (`AppCompatDelegate.setApplicationLocales`) |

**Not in stack:** Hilt, Material Extended Icons, third-party design systems, DataStore.
Do not introduce them without an explicit decision recorded in this file.

SDKs: `minSdk = 29`, `targetSdk = 36`, `compileSdk = 36.1`. AGP 9.2.1, Gradle 9.4.1.

---

## Build Commands

```bash
./gradlew assembleDebug          # Compile + package the debug APK
./gradlew installDebug           # Install on the connected device/emulator
./gradlew build                  # Full build (assemble + lint + tests)
./gradlew lint                   # Static analysis only
./gradlew test                   # JVM unit tests
./gradlew connectedAndroidTest   # Instrumented tests (requires device)
./gradlew clean                  # Wipe build outputs
```

On Windows (PowerShell): swap `./gradlew` for `.\gradlew.bat`.

---

## Architecture

**Package-by-feature, DDD-style bounded contexts** under `com.elysium.softwork`:

| Package      | Responsibility                                       |
|--------------|------------------------------------------------------|
| `shared`     | Cross-cutting code (theme, components, i18n, network/local infra, core utilities) |
| `iam`        | Identity & Access — Employee login, session, profile |
| `forum`      | Workers Forum — posts, comments, reactions           |
| `feedback`   | Check-ins & Reports — periodic feedback to HR        |
| `payments`   | Payment processing — transactions, invoices          |

**Each context owns four layers:**

```
<context>/
├── domain/         # Models, interfaces, business rules. No Android imports.
├── data/store/     # FooStore implementations (Retrofit + Room)
├── application/    # Use cases / orchestration (kept thin)
└── presentation/   # Composables, ViewModels, navigation
```

### Store Pattern

The data layer uses **Stores**, not Repositories — a frontend-friendly term that avoids the
`*RepositoryImpl` suffix.

```kotlin
// domain/PostStore.kt
interface PostStore {
    suspend fun list(): List<Post>
    suspend fun publish(content: String): Post
}

// data/store/PostStoreImpl.kt
class PostStoreImpl(
    private val api: ForumApi,
    private val dao: PostDao,
) : PostStore { /* ... */ }
```

### Bean / Pragmatic Shortcut (Phase 2 onwards)

The IAM context introduced a deliberate shortcut: **a single Kotlin `data class` annotated
with `@SerializedName` flows through the Retrofit WebService for both request bodies and
response payloads** — no DTOs, no assemblers, no mappers. Different endpoints fill different
subsets of the model (login fills `email`/`password`, the response fills `id`/`token`/…), so
all fields are nullable. Trade-off accepted: simpler code, faster iteration; cost: a single
class describes multiple wire shapes.

```kotlin
// iam/domain/model/User.kt
data class User(
    @SerializedName("id") val id: String? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("password") val password: String? = null,
    @SerializedName("token") val token: String? = null,
    /* ... */
)

// iam/data/network/AuthWebService.kt
interface AuthWebService {
    @POST("auth/login") suspend fun login(@Body credentials: User): Response<User>
}
```

Adopt this pattern in new contexts only when the data shape is genuinely simple and the
team accepts the round-trip risk. Re-introduce DTOs the moment the wire contract diverges
meaningfully from the domain model.

### Dependency Wiring (no Hilt)

A manual [`ServiceLocator`](app/src/main/java/com/elysium/softwork/shared/core/ServiceLocator.kt)
owned by `SoftWorkApplication` exposes process-wide singletons (SharedPreferences, Retrofit,
each context's WebService, each context's Store). ViewModels receive their `Store` via a
`ViewModelProvider.Factory` exposed on the ViewModel companion (see `AuthViewModel.Factory`).
Composables resolve the ViewModel with `viewModel(factory = AuthViewModel.Factory)`.

---

## UI Conventions

- **Always wrap UI in `SoftWorkTheme`.** Defined at `shared/presentation/theme/Theme.kt`.
- **Use the brand components first** — `SoftWorkButton`, `SoftWorkTextField`, `SoftWorkCard` in
  `shared/presentation/components/`. Reach for raw Material 3 widgets only when the brand
  component genuinely cannot express the requirement; document the reason in a code comment.
- **Buttons declare a variant** — `ButtonVariant.EMPLOYEE` (gradient, default) or
  `ButtonVariant.HR` (solid navy). Never hard-code a colored `Button`.
- **No raw colors in feature code.** Pull from `MaterialTheme.colorScheme` or
  `SoftWorkTheme.colors` — never `Color(0xFF…)` outside `shared/presentation/theme/Color.kt`.
- **Icons:** `painterResource(R.drawable.ic_*)` only. New icons land as XML vector drawables
  under `app/src/main/res/drawable/`. Do **not** add `material-icons-extended`.

---

## i18n Workflow

User-facing text lives **only** in Android string resources:

- `app/src/main/res/values/strings.xml` — English (default).
- `app/src/main/res/values-es/strings.xml` — Spanish.

**Every new key must land in both files in the same change.** Never hard-code a literal in a
Composable; always `stringResource(R.string.key)`.

**Switching language at runtime** uses the modern AppCompat API:

```kotlin
LocaleHelper.apply(AppLocale.ES)
```

AppCompat persists the choice (via the `AppLocalesMetadataHolderService` declared in the
manifest with `autoStoreLocales=true`) and re-applies the configuration automatically. **Do
not** call `Activity.recreate()`, override `attachBaseContext`, or wrap `Configuration` —
those approaches are obsolete and incompatible with App Bundles + per-app language settings.

---

## KDoc Standards

- **English only** — across code, comments, log messages, and identifiers.
- **KDoc required** on every public class, interface, function, and enum. Document the
  *why*, not just the signature. Mention defaults, units, and side effects.
- Document parameters with `@param`, return values with `@return`, and example usage in a
  fenced ` ``` ` block when the API has more than one obvious shape.

---

## Current Progress

### ✅ Phase 1 — Scaffolding & Design System (complete)

- DDD package skeleton for `shared`, `iam`, `forum`, `feedback` (each with
  `domain/`, `data/store/`, `application/`, `presentation/`).
- Compose design system: `Color.kt`, `Type.kt` (Exo via Google Fonts), `Shape.kt`, `Theme.kt`.
- Brand components: `SoftWorkButton` (Employee + HR variants), `SoftWorkTextField`,
  `SoftWorkCard`.
- Native i18n: `AppLocale`, `LocaleHelper` (AppCompat back-port), English + Spanish strings.
- `SoftWorkApplication` wired in the manifest. Manifest theme renamed to `Theme.SoftWork`.

### ✅ Phase 2 — IAM bounded context (complete)

- **Domain**: `User` and `AuthToken` data classes annotated with `@SerializedName`. Shared
  between request/response per the bean/pragmatic-shortcut pattern.
- **Data**: `AuthWebService` (Retrofit), `AuthStore` (interface) + `AuthStoreImpl` orchestrating
  the WebService and persisting the JWT in `SharedPrefsManager`. Process-wide Retrofit
  instance lives in `shared/data/network/ApiClient.kt`.
- **Application**: `AuthViewModel` exposing `state: StateFlow<AuthState>` and
  `form: StateFlow<FormState>`. Validation in `AuthValidation` (pure, JVM-testable).
- **Presentation**: `LoginScreen`, `RegisterScreen`, `RegisterGoogleScreen`,
  `AuthSuccessScreen` plus shared IAM components (`PasswordVisibilityToggle`,
  `RoleSelectorCard`, `GoogleOutlineButton`, `BackTopBar`, `VerifiedDomainChip`).
- **Navigation**: `AuthNavHost` in `iam/presentation/navigation/AuthNavigation.kt`. Routes:
  `auth/login`, `auth/register`, `auth/register-google`, `auth/success/{kind}`.
- **Wiring**: `SoftWorkApplication.serviceLocator` exposes `authStore`. `AuthViewModel.Factory`
  pulls it from the application via `CreationExtras`. `MainActivity` mounts `AuthNavHost`.
- **Icons added** as XML vector drawables: `ic_visibility`, `ic_visibility_off`, `ic_check`,
  `ic_check_circle`, `ic_arrow_back`, `ic_user`, `ic_logo`, `ic_google`.

#### New dependencies (with rationale)

- `androidx.appcompat:appcompat:1.7.x` — required for `AppCompatDelegate.setApplicationLocales`
  (Phase 1).
- `androidx.lifecycle:lifecycle-viewmodel-compose:2.10.0` — Compose-aware `viewModel(...)`
  resolver, needed to instantiate `AuthViewModel` with a `CreationExtras`-aware factory.
- `androidx.lifecycle:lifecycle-runtime-compose:2.10.0` — `collectAsStateWithLifecycle`, the
  lifecycle-safe replacement for `collectAsState` on Compose surfaces.

### 🔜 Next — Phase 3

- Root nav graph that hands off from `AuthNavHost` to the main app shell on
  `onAuthComplete`. Forum (`forum/`) entry point.
- Auth header interceptor on `ApiClient` once the backend session contract is finalized.
- Forgot-password flow.

# Final Consideration
- When implementing important changes or adding new dependencies, please update this document with the rationale and the impact on the architecture. This will help maintain a clear understanding of the design decisions and ensure consistency across the codebase.