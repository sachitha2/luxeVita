# Technical Documentation (EcoStay)

## Overview
EcoStay is a native Android (Java) application for the LuxeVista Resort scenario. It stores all data locally using **Room** and uses **WorkManager** for local notifications.

Cross-platform frameworks (Flutter/React Native/Ionic) are not used, matching the assessment requirement.

## Architecture
UI is implemented with Activities and XML layouts.
Database access uses:
- `EcoStayDatabase` (RoomDatabase singleton)
- DAO interfaces for each entity type

Long-running work is executed on background threads using `ExecutorService`.

## Data Model
The schema is defined in `app/src/main/java/com/example/ecostay/data/*` and documented in `docs/db_schema.md`.

### Dates
`LocalDate` values are stored as **epoch day** (`LocalDate.toEpochDay()`) in the database. This avoids the need for custom `TypeConverters`.

### Room Availability / Conflict Detection
Rooms are modeled with `room_types.totalRooms` inventory and user confirmations stored in `room_bookings`.

Two room bookings overlap using ranges treated as **[start, end)** (end is checkout day):
`startA < endB AND startB < endA`.

Available inventory for a date range is computed as:
`available = totalRooms - overlappingConfirmedBookings`.

### Service Availability / Conflict Detection
For the local demo, services are modeled as **one confirmed reservation per service per date**. Conflicts are detected using:
`COUNT(confirmedBookingsForServiceAndDate) > 0`.

## Validation
Reusable validation helpers:
- `DateValidationUtils` for epoch-day range validity and overlap rules
- `BookingValidationUtils` for availability checks

These are applied in:
- Room booking creation (`RoomBookingActivity`)
- Service reservation UI (`ServiceBookingActivity`)
- Profile travel date validation (`ProfileActivity`)

## Notifications (WorkManager)
Notifications are handled with:
- `BookingReminderWorker` (one-time reminders for room/service bookings)
- `PromotionWorker` (periodic reminders)

`NotificationScheduler` sets up:
- `enqueueUniqueWork` for booking reminders (unique by booking id)
- `enqueueUniquePeriodicWork` for promotion notifications

Cancellation:
- When a user cancels their service reservation, the associated reminder work is cancelled using the same unique work name.

## Tests
Core logic is tested using:
- `PasswordUtilsTest`, `DateValidationUtilsTest`, `BookingValidationUtilsTest` (local JVM unit tests)
- `DatabaseConflictInstrumentedTest` (in-memory Room database)

