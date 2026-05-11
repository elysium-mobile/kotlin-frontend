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

| Package              | Responsibility                                                                    |
|----------------------|-----------------------------------------------------------------------------------|
| `shared`             | Cross-cutting code (theme, components, i18n, network/local infra, core utilities) |
| `iam`                | Identity & Access — Employee login, session, profile                              |
| `worker.forum`       | Workers Forum — posts, comments, reactions                                        |
| `feedback`           | Check-ins & Reports — periodic feedback to HR                                     |
| `payment.membership` | Payment processing — transactions, invoices                                       |

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

### ✅ Phase 3 — Authenticated shell (complete)

- **Bottom navigation**: `MainNavHost` (Scaffold + Material 3 `NavigationBar`, 72.dp height)
  in `shared/presentation/navigation/MainNavigation.kt`. Tabs: Menú (Home), Perfil
  (Profile), Foro (placeholder), Notificaciones (placeholder). Active color `PrimarySky`,
  inactive `AccentMint`, container white, no indicator pill.
- **HomeScreen** (`shared/presentation/home/`) — greeting + initials avatar, 5-emoji mood
  check-in card with anonymous badge, three action cards (Report incident, Internal forums,
  AI Assistant) using `SoftWorkCard` + `painterResource` icons.
- **ProfileScreen** (`shared/presentation/profile/`) — header with edit affordance, hero
  with `InitialsAvatar`, four section cards (Información laboral / Privacidad / Idioma /
  Pago), outline `Editar perfil` button, `Cerrar sesión` text button (Danger color) wired
  to a logout callback.
- **Live language toggle** in the Idioma card calls
  `LocaleHelper.apply(AppLocale.ES | EN)` → `AppCompatDelegate.setApplicationLocales(...)`.
  AppCompat persists via the manifest service and recreates the activity transparently.
- **Auth ↔ Main routing**: `MainActivity` now extends `AppCompatActivity` (so AppCompat's
  locale back-port can recreate the Activity on API 29-32) and swaps between `AuthNavHost`
  and `MainNavHost` based on `rememberSaveable<Boolean>` flipped by `onAuthComplete` /
  `onLogout`. Logout calls `AuthStore.clearSession()` and pops the user back to the IAM graph.
- **Shared component**: `InitialsAvatar` in `shared/presentation/components/` derives initials
  from a name and renders a circular brand-colored avatar.
- **Icons added**: `ic_home`, `ic_person`, `ic_forum`, `ic_notifications`, `ic_shield`,
  `ic_people`, `ic_sparkle`, `ic_chevron_right`, `ic_edit`.

#### Phase 3 extension — Identity Protection flow

- **Persistence**: `SharedPrefsManager` gained `getBoolean` / `putBoolean` plus four new
  keys: `KEY_GLOBAL_ANONYMITY`, `KEY_FORUM_ANONYMITY`, `KEY_SURVEYS_ANONYMITY`,
  `KEY_REPORTS_ANONYMITY`.
- **Domain**: `AnonymityPreferences` data class in `shared/domain/identity/` bundles the
  four flags as a single immutable snapshot.
- **Application**: `AnonymityViewModel` in `shared/application/` loads from prefs on
  construction, exposes a `StateFlow<AnonymityPreferences>` for the in-memory edit buffer,
  and flushes to prefs only on explicit `save()`. Wired through a `Factory` reading
  `SharedPrefsManager` from the service locator (same pattern as `AuthViewModel`).
- **Presentation**: `ProtectedIdentityScreen` in `shared/presentation/identity/` —
  back-arrow header, anonymous-user hero, global-toggle card, three-row granular toggle
  card with 0.5.dp dividers, AccentMint-tinted HR info banner with `ic_lock`, and a
  primary `Guardar preferencias` button.
- **Navigation**: new `MainRoutes.PROTECTED_IDENTITY` route. The Profile "Modo anónimo en
  foro" row now navigates to it; tapping Save pops back to Profile.
- **Icons added**: `ic_lock`.

**UX note** — toggles edit an in-memory buffer; persistence is explicit on Save. Backing
out of the screen discards unsaved changes. To switch to write-through behavior, route the
toggle handlers to call `viewModel.save()` after each `setX(...)`.

#### Architectural note — `AppCompatActivity` for locale back-port

`MainActivity` was migrated from `ComponentActivity` to `AppCompatActivity` so that
`AppCompatDelegate.setApplicationLocales` triggers automatic activity recreation on API
29-32. This is the official path documented at
[developer.android.com/guide/topics/resources/app-languages](https://developer.android.com/guide/topics/resources/app-languages).
On API 33+ the platform `LocaleManager` handles recreation regardless of the activity base
class. AppCompatActivity remains fully Compose-compatible — `enableEdgeToEdge()` and
`setContent` work as before.

### ✅ Phase 4 — Forum bounded context (complete, offline-first)

- **Domain**: [`Post`](app/src/main/java/com/elysium/softwork/forum/domain/model/Post.kt) data
  class doubles as a Room `@Entity(tableName = "posts")` and a Gson `@SerializedName` bean —
  the same instance flows through `PostWebService` requests/responses and into the local
  cache. All non-key fields default so partially populated drafts can be passed end-to-end.
- **Data — local**: `PostDao` exposes `getAllPosts(): Flow<List<Post>>`, a `count()` for the
  seed branch, and `upsertAll` / `upsert` (REPLACE on conflict). `ForumDatabase` (Room
  v1, `forum.db`) is built via a `companion object create(context)`.
- **Data — network**: `PostWebService` declares `@GET("posts")` and `@POST("posts")`. Both
  endpoints carry `Post` directly per the bean shortcut.
- **Data — store**: `PostStore` interface + `PostStoreImpl` implementing the
  **offline-first** contract:
  - `observe()` returns the Room `Flow` directly — UI always renders cached state.
  - `refresh()` tries the network and upserts on success; **fallback**: when the cache is
    empty after a failed refresh it seeds three bundled sample posts so the demo is alive
    without a backend. Remove the seed when the API is live.
  - `publish(...)` always inserts locally (using a UUID when the server is unreachable) so
    the user sees their post immediately; the next refresh reconciles.
- **Application**: `ForumCategory` enum (`SUGGESTIONS`, `QUESTIONS`, `EVENTS`, `CONFLICTS`)
  carries the wire `key` + the localized `labelRes`. ViewModels in
  `forum/application/viewmodel/`:
  - `ForumViewModel` — combines the cached posts flow with an in-memory category filter,
    triggers `store.refresh()` on init, exposes `posts: StateFlow<List<Post>>`.
  - `NewPostViewModel` — reads `forum_anonymity` from `SharedPrefsManager` on construction
    and surfaces it via `isAnonymous`. The user does **not** toggle anonymity here; it must
    be set on the protected-identity screen. Form has a 500-char limit, exposes
    `PublishState` (Idle / Publishing / Published / Error).
  - `ThreadViewModel` — loads a single post by id, surfaces `isAnonymous` for the sticky
    input. Comments are static samples in Phase 4 (Comment store lands in Phase 5).
- **Presentation**: screens under `forum/presentation/views/{feed,newpost,thread}/`,
  components (`AnonymousBadge`, `CategoryChips`, `Chip`) under
  `forum/presentation/components/`. Navigation in `forum/presentation/navigation/`:
  `ForumRoutes` (`forum/feed`, `forum/new-post`, `forum/thread/{postId}`) +
  `forumGraph(navController, userName)` extension that `MainNavHost` invokes.
- **MainNavigation update**: the Foro bottom tab now routes to `ForumRoutes.FEED`. The
  in-progress placeholder screen for the Foro tab was removed; the home action card
  ("Internal forums") also navigates to `ForumRoutes.FEED`.
- **Wiring**: `ServiceLocator` now owns a process-wide `ForumDatabase` and exposes
  `postStore: PostStore`. ViewModel factories pull `postStore` (and `sharedPrefsManager`)
  from the locator just like `AuthViewModel` and `AnonymityViewModel`.
- **Icons added**: `ic_close`, `ic_send`, `ic_paperclip`, `ic_image`, `ic_add` (FAB glyph).

#### Phase 4 caveats

- The 3 seeded posts are hard-coded in `PostStoreImpl.SeedPosts` — **delete that companion
  block** when the live `/posts` endpoint is ready.
- Comments are stubbed in `ThreadScreen.SAMPLE_COMMENTS`. A real Comment domain + store +
  WebService land in Phase 5.
- `forum_anonymity` is read **once** on ViewModel construction. Re-enter the new-post or
  thread screen to pick up a privacy preference change. Switch to a `Flow` if real-time
  updates are needed.
- Image / attachment toolbar buttons are no-ops (Phase 5 will wire the picker).

### 🔜 Next — Phase 5

- Comment domain + store + WebService backing `ThreadScreen`.
- Image / attachment picker for the new-post composer.
- Real user/profile data sourced from a `ProfileStore` (replace placeholder strings).
- Forgot-password flow.
- Auth header interceptor on `ApiClient` once the backend session contract is finalized.

---

## Security & Environment Variables

The app is built with **zero secrets in source**. Backend URLs and third-party API keys are
injected at compile time via the [Secrets Gradle Plugin for Android](https://developers.google.com/maps/documentation/android-sdk/secrets-gradle-plugin)
and exposed to Kotlin through `BuildConfig` fields.

### Local setup (new developer checklist)

1. Copy the committed template to a personal, gitignored file:
   ```bash
   cp secrets.defaults.properties secrets.properties
   ```
2. Fill in real values in `secrets.properties` (see the keys below). **Never commit this
   file** — it is listed in `.gitignore`.
3. Build normally: `./gradlew assembleDebug` (or `.\gradlew.bat assembleDebug` on Windows).
   The plugin reads `secrets.properties` first, then falls back to
   `secrets.defaults.properties` for any missing key, so an unconfigured environment still
   builds and boots.

### Available keys

| Properties key | Generated constant | Consumer |
|---|---|---|
| `BACKEND_BASE_URL` | `BuildConfig.BACKEND_BASE_URL` | `ApiClient.retrofit` base URL — fail-fast if blank |
| `API_KEY_GEMINI` | `BuildConfig.API_KEY_GEMINI` | `ApiKeyInterceptor` → `x-goog-api-key` header on `generativelanguage.googleapis.com` |
| `API_KEY_GMAIL` | `BuildConfig.API_KEY_GMAIL` | Reserved for future Gmail integration |
| `API_KEY_EXTERNAL_SERVICE` | `BuildConfig.API_KEY_EXTERNAL_SERVICE` | `ApiKeyInterceptor` → `X-Api-Key` header on the configured external host |

### CI / pipelines

CI must materialize `secrets.properties` from the team secret manager **before** invoking
`./gradlew assembleRelease`. The committed defaults file alone produces a build that boots
but with no functional third-party integrations — adequate for unit tests and static
analysis only.

### Network layer rule

The base URL has exactly **one** source of truth: `BuildConfig.BACKEND_BASE_URL`. Retrofit
`WebService` interfaces (`AuthWebService`, `PostWebService`, …) must only declare
**relative paths** in their `@GET`/`@POST` annotations. Reintroducing a full URL — or any
new `"https://..."` literal anywhere under `app/src/main/` — is a regression and must be
caught in review.

Third-party API keys must never appear in `WebService` annotations either. Route them
through `ApiKeyInterceptor` on a per-host basis (see `ApiKeyInterceptor.hostKeyMap`).

### The `unset` sentinel

The Secrets Gradle Plugin refuses to emit empty-string values as `BuildConfig` fields
(generates illegal Java). The defaults file therefore uses the literal string `unset` as
the placeholder for unconfigured API keys. `ApiKeyInterceptor` recognizes this sentinel
and **skips header injection** for it, so a default build sends no third-party credentials
rather than leaking the string `"unset"` over the wire. Replace `unset` with a real key in
`secrets.properties` to enable the corresponding integration.

### Adding a new key

1. Append the new key to `secrets.defaults.properties` with a non-blank placeholder (use
   `unset` — see above).
2. Declare the matching `buildConfigField("String", "MY_NEW_KEY", "\"\"")` inside
   `android { defaultConfig { ... } }` in `app/build.gradle.kts`. This is the fallback the
   secrets plugin overwrites when the same name is present in `secrets.properties`.
3. Consume it from Kotlin via `BuildConfig.MY_NEW_KEY`. For host-scoped third-party keys,
   add an entry to `ApiKeyInterceptor.hostKeyMap` rather than handling it in a store.

### New dependencies (with rationale)

- `com.google.android.libraries.mapsplatform.secrets-gradle-plugin:2.0.1` — generates
  `BuildConfig` fields from `secrets.properties` with a committed defaults fallback.
- `com.squareup.okhttp3:okhttp:4.12.0` — pinned explicitly (Retrofit 3 brings it
  transitively) so the version is locked and an `OkHttpClient.Builder` can register the
  interceptor chain.
- `com.squareup.okhttp3:logging-interceptor:4.12.0` (debug only) — `BODY`-level request
  logging in Logcat. Never reaches release builds.

### AGP 9 compatibility note

The Secrets Gradle Plugin (2.0.1, Apr 2024) predates AGP 9. If a future AGP bump breaks it,
the plugin can be replaced by ~15 lines of inline `Properties().load(...)` +
`buildConfigField(...)` in `app/build.gradle.kts`; the rest of the stack (`BuildConfig`
consumers, interceptor, defaults file) is unaffected by that swap.

---

# Final Consideration
- When implementing important changes or adding new dependencies, please update this document with the rationale and the impact on the architecture. This will help maintain a clear understanding of the design decisions and ensure consistency across the codebase.