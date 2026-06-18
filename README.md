# TechCare Services Mobile App

Native Android application for **TechCare Services**, a company that repairs and services electronic devices and home appliances including smartphones, laptops, televisions, air conditioners, refrigerators, and washing machines.

## Technologies Used

- Java 17
- XML layouts
- Android SDK (minSdk 22, targetSdk 34)
- Room Database (SQLite)
- MVVM architecture with ViewModel and LiveData
- Repository pattern
- RecyclerView and Material Components / CardView
- SharedPreferences for login session
- PBKDF2 password hashing

## Main Features

- User registration and login
- Browse repair services by device type
- Save and manage customer devices
- Submit repair requests (pickup or service center drop-off)
- View and track repair bookings
- Repair status timeline with demo status updates
- Technician assignment and estimated completion
- FAQs and maintenance tips
- Contact support form

## How to Run

1. Open the project in Android Studio (Giraffe or newer recommended).
2. Let Gradle sync complete.
3. Connect an Android device or start an emulator (API 22+).
4. Run the `app` configuration.
5. The app launches at **SplashActivity**, then routes to login or dashboard based on session.

**Note:** Use JDK 17 for Gradle builds. In Android Studio, set **Gradle JDK** to Java 17 (File → Settings → Build → Gradle).

## Project Structure

- `app/src/main/java/com/example/ecostay/data/` — Room entities, DAOs, repositories, AppDatabase
- `app/src/main/java/com/example/ecostay/ui/` — Activities and adapters
- `app/src/main/java/com/example/ecostay/ui/viewmodel/` — ViewModels
- `app/src/main/java/com/example/ecostay/util/` — Validation, date/time, technician helpers
- `app/src/main/res/` — Layouts, strings, themes

## Documentation

- [DATABASE_SCHEMA.md](DATABASE_SCHEMA.md)
- [TECHNICAL_DOCUMENTATION.md](TECHNICAL_DOCUMENTATION.md)
- [USER_DOCUMENTATION.md](USER_DOCUMENTATION.md)
- [TEST_PLAN.md](TEST_PLAN.md)
