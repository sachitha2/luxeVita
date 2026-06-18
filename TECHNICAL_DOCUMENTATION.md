# TechCare Services — Technical Documentation

## Architecture

The app follows **MVVM (Model-View-ViewModel)** with the **Repository pattern**:

```
Activities (View) → ViewModels → Repositories → Room DAOs → SQLite
```

- **View:** Java Activities + XML layouts + RecyclerView adapters
- **ViewModel:** `AuthViewModel`, `DeviceViewModel`, `ServiceViewModel`, `BookingViewModel`, `FaqTipsViewModel`
- **Model:** Room entities and `AppDatabase`
- **Repository:** Data access abstraction with background `ExecutorService`

Session state uses `SessionManager` (SharedPreferences), separate from Room.

## MVVM Pattern

Activities observe `LiveData` from ViewModels. ViewModels delegate to repositories and do not hold Activity references. Example flow:

1. `LoginActivity` calls `authViewModel.login(email, password)`
2. `UserRepository` validates and queries Room on background thread
3. `loginResult` LiveData posts success/failure
4. Activity observes and navigates or shows error

## Room Database Usage

- **Class:** `AppDatabase` (singleton)
- **File:** `techcare.db`
- **Migration:** `fallbackToDestructiveMigration()` for coursework development
- **Writes:** `AppDatabase.getWriteExecutor()` — never on main thread
- **Seed:** `onCreate` callback inserts services, FAQs, and tips

## Validation Logic

`ValidationUtils` provides:

- `isEmpty()` — required fields
- `isValidEmail()` — regex email check
- `isValidPhone()` — 10-digit phone
- `isValidPassword()` — minimum 8 characters
- `isValidIssueDescription()` — minimum 10 characters

Repositories and Activities use these before persisting data.

## Password Security

`PasswordUtils` hashes passwords with PBKDF2-HMAC-SHA256. Hash and salt stored in `users.password` and `users.password_salt`.

## Main Java Classes

| Class | Role |
|-------|------|
| `AppDatabase` | Room database singleton and seed data |
| `UserRepository` | Register, login, duplicate checks |
| `BookingRepository` | Create bookings, status updates, summaries |
| `TechnicianUtils` | Device type → technician mapping, status flow |
| `DateTimeUtils` | ISO timestamps, date validation |
| `SessionManager` | Login session in SharedPreferences |

## Main Activities

| Activity | Purpose |
|----------|---------|
| `SplashActivity` | Branding, session routing |
| `LoginActivity` / `RegisterActivity` | Authentication |
| `DashboardActivity` | Main navigation hub |
| `BrowseServicesActivity` | Service catalog by device type |
| `DeviceListActivity` / `AddDeviceActivity` | Device CRUD |
| `SubmitRepairRequestActivity` | Create repair booking |
| `MyBookingsActivity` / `BookingDetailsActivity` | Booking list and tracking |
| `FaqTipsActivity` | FAQs and maintenance tips |
| `SupportActivity` | Contact form |

## Data Flow — Repair Booking

1. User selects service in `BrowseServicesActivity`
2. `SubmitRepairRequestActivity` loads devices and services via ViewModels
3. On submit, `BookingViewModel` → `BookingRepository.submitBooking()`
4. Repository sets status `Received`, assigns technician, saves `BookingEntity` and initial `RepairStatusEntity`
5. `MyBookingsActivity` loads summaries via `BookingRepository.loadBookingsForUser()`
6. `BookingDetailsActivity` shows timeline; Update Status calls `advanceStatus()` through status flow

## Status Flow

`Received` → `Technician Assigned` → `Under Repair` → `Ready for Pickup` → `Completed`

Each advance inserts a new `RepairStatusEntity` record.
