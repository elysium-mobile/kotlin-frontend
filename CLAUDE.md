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
| i18n           | AppCompat 1.7.0 (`AppCompatDelegate.setApplicationLocales`) |

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

### 🔜 Next — Phase 2 (`iam`)

- Login screen wired to a `SessionStore` (Retrofit + Room session cache).
- Auth navigation graph (`navigation-compose`) and a session-gated entry point.
- Forgot-password and registration entry points (UI shells; backend wiring later).
