# Database (Normalized Relational Schema)

The application stores all data locally using **Room** (SQLite). Dates are stored as **epoch day** (`LocalDate.toEpochDay()`), and room/service conflicts are resolved using overlap rules.

## Tables

### `users`
Primary key:
- `id` (INTEGER, auto-generated)

Unique fields:
- `email` (TEXT, UNIQUE)

Columns:
- `email` (TEXT, NOT NULL)
- `password_hash` (TEXT, NOT NULL) - PBKDF2 derived hash (Base64)
- `password_salt` (TEXT, NOT NULL) - per-user random salt (Base64)
- `preferredRoomTypeId` (INTEGER, NULL) - personalization
- `maxBudget` (REAL, NULL)
- `travelStartDateEpochDay` (INTEGER, NULL)
- `travelEndDateEpochDay` (INTEGER, NULL)

## `room_types`
Primary key:
- `id`

Columns:
- `name` (TEXT, NOT NULL)
- `description` (TEXT, NOT NULL)
- `price_per_night` (REAL, NOT NULL)
- `totalRooms` (INTEGER, NOT NULL) - inventory per room type
- `imageRef` (TEXT, NULL) - optional reference for UI

## `room_bookings`
Primary key:
- `id`

Columns:
- `user_id` (INTEGER, NOT NULL) - references `users(id)`
- `room_type_id` (INTEGER, NOT NULL) - references `room_types(id)`
- `start_date_epoch_day` (INTEGER, NOT NULL)
- `end_date_epoch_day` (INTEGER, NOT NULL)
- `status` (TEXT, NOT NULL) - `CONFIRMED` / `CANCELLED`
- `created_at_epoch_millis` (INTEGER, NOT NULL)

Conflict rule (rooms):
- Booked ranges are treated as **[start, end)** (end is checkout date).
- Two bookings overlap iff:
  `startA < endB AND startB < endA`

Availability (per room type):
- `available = totalRooms - COUNT(overlappingConfirmedBookings)`

## `services`
Primary key:
- `id`

Columns:
- `name` (TEXT, NOT NULL)
- `category` (TEXT, NOT NULL) - SPA / DINING / CABANAS / TOURS
- `description` (TEXT, NOT NULL)
- `price` (REAL, NOT NULL)

## `service_bookings`
Primary key:
- `id`

Columns:
- `user_id` (INTEGER, NOT NULL) - references `users(id)`
- `service_id` (INTEGER, NOT NULL) - references `services(id)`
- `booking_date_epoch_day` (INTEGER, NOT NULL)
- `status` (TEXT, NOT NULL) - `CONFIRMED` / `CANCELLED`
- `created_at_epoch_millis` (INTEGER, NOT NULL)

Conflict rule (services):
- In this local demo model: **1 confirmed reservation per service per date**.
- Conflict if `COUNT(confirmedBookingsForServiceAndDate) > 0`.

## `offers`
Primary key:
- `id`

Columns:
- `title` (TEXT, NOT NULL)
- `description` (TEXT, NOT NULL)
- `valid_from_epoch_day` (INTEGER, NOT NULL)
- `valid_to_epoch_day` (INTEGER, NOT NULL)

Active offers query uses:
- `valid_from_epoch_day <= todayEpochDay <= valid_to_epoch_day`

## `attractions`
Primary key:
- `id`

Columns:
- `title` (TEXT, NOT NULL)
- `description` (TEXT, NOT NULL)
- `location` (TEXT, NOT NULL)

## Notes on Normalization

All entities that vary independently (rooms, services, offers, attractions) are stored in their own tables.
User-specific state is stored in `room_bookings` and `service_bookings`.

