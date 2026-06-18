# TechCare Services — Database Schema

SQLite database managed by Room (`techcare.db`).

## Tables

### users

| Field | Type | Constraints |
|-------|------|-------------|
| userId | INTEGER | PRIMARY KEY, AUTOINCREMENT |
| fullName | TEXT | NOT NULL |
| email | TEXT | NOT NULL, UNIQUE |
| phone | TEXT | NOT NULL, UNIQUE |
| password | TEXT | NOT NULL (PBKDF2 hash) |
| password_salt | TEXT | NOT NULL |
| address | TEXT | NOT NULL |

### devices

| Field | Type | Constraints |
|-------|------|-------------|
| deviceId | INTEGER | PRIMARY KEY, AUTOINCREMENT |
| userId | INTEGER | NOT NULL, FK → users(userId) ON DELETE CASCADE |
| deviceType | TEXT | NOT NULL |
| brand | TEXT | NOT NULL |
| model | TEXT | NOT NULL |

### services

| Field | Type | Constraints |
|-------|------|-------------|
| serviceId | INTEGER | PRIMARY KEY, AUTOINCREMENT |
| deviceType | TEXT | NOT NULL |
| serviceName | TEXT | NOT NULL |
| description | TEXT | NOT NULL |
| estimatedPrice | REAL | NOT NULL |

### bookings

| Field | Type | Constraints |
|-------|------|-------------|
| bookingId | INTEGER | PRIMARY KEY, AUTOINCREMENT |
| userId | INTEGER | NOT NULL, FK → users(userId) |
| deviceId | INTEGER | NOT NULL, FK → devices(deviceId) |
| serviceId | INTEGER | NOT NULL, FK → services(serviceId) |
| issueDescription | TEXT | NOT NULL |
| serviceMethod | TEXT | NOT NULL |
| preferredDate | TEXT | NOT NULL |
| preferredTime | TEXT | NOT NULL |
| status | TEXT | NOT NULL |
| technicianName | TEXT | NOT NULL |
| estimatedCompletion | TEXT | NOT NULL |
| createdAt | TEXT | NOT NULL |

### repair_status

| Field | Type | Constraints |
|-------|------|-------------|
| statusId | INTEGER | PRIMARY KEY, AUTOINCREMENT |
| bookingId | INTEGER | NOT NULL, FK → bookings(bookingId) ON DELETE CASCADE |
| status | TEXT | NOT NULL |
| remarks | TEXT | NOT NULL |
| updatedAt | TEXT | NOT NULL |

### faqs

| Field | Type | Constraints |
|-------|------|-------------|
| faqId | INTEGER | PRIMARY KEY, AUTOINCREMENT |
| question | TEXT | NOT NULL |
| answer | TEXT | NOT NULL |

### maintenance_tips

| Field | Type | Constraints |
|-------|------|-------------|
| tipId | INTEGER | PRIMARY KEY, AUTOINCREMENT |
| deviceType | TEXT | NOT NULL |
| title | TEXT | NOT NULL |
| description | TEXT | NOT NULL |

## Relationships

- **users** 1 → N **devices**
- **users** 1 → N **bookings**
- **devices** 1 → N **bookings**
- **services** 1 → N **bookings**
- **bookings** 1 → N **repair_status**

## Normalization (3NF)

1. **First Normal Form (1NF):** All attributes are atomic; no repeating groups in any table.
2. **Second Normal Form (2NF):** All non-key attributes depend on the full primary key. Each table has a single surrogate primary key.
3. **Third Normal Form (3NF):** No transitive dependencies. Device details live in `devices`, service catalog in `services`, and booking-specific data in `bookings`. Status history is separated into `repair_status` to avoid update anomalies on booking records.

## Seed Data

On first database creation, the app seeds 15 repair services, FAQs, and maintenance tips via `AppDatabase` callback.
