# TechCare Services — Test Plan

## Test Environment

- Android emulator or physical device (API 22+)
- Fresh install or cleared app data for registration tests

---

### TC-01: Register valid user

| Step | Action | Expected Result |
|------|--------|-----------------|
| 1 | Open app → Register | Register screen shown |
| 2 | Enter valid name, email, 10-digit phone, password (8+ chars), address | Fields accepted |
| 3 | Tap Create account | Success; returns to login |

---

### TC-02: Register invalid email

| Step | Action | Expected Result |
|------|--------|-----------------|
| 1 | Open Register | Register screen shown |
| 2 | Enter invalid email (e.g. `bad-email`) and other valid fields | — |
| 3 | Tap Create account | Error: valid email required |

---

### TC-03: Register empty fields

| Step | Action | Expected Result |
|------|--------|-----------------|
| 1 | Open Register | Register screen shown |
| 2 | Leave fields empty, tap Create account | Error: fill required fields |

---

### TC-04: Login valid user

| Step | Action | Expected Result |
|------|--------|-----------------|
| 1 | Register a user (TC-01) | Account created |
| 2 | Login with email/phone and password | Dashboard opens with greeting |

---

### TC-05: Login invalid password

| Step | Action | Expected Result |
|------|--------|-----------------|
| 1 | Login with valid email/phone, wrong password | Error: incorrect password |

---

### TC-06: Add device

| Step | Action | Expected Result |
|------|--------|-----------------|
| 1 | Login → Saved Devices → Add Device | Add device screen shown |
| 2 | Select type, enter brand and model, Save | Device appears in list |

---

### TC-07: Submit repair request

| Step | Action | Expected Result |
|------|--------|-----------------|
| 1 | Ensure at least one saved device | Device available |
| 2 | Browse Services → Request Service | Submit form opens |
| 3 | Fill issue, method, date, time; Submit | Success toast; booking created |

---

### TC-08: Submit request with missing fields

| Step | Action | Expected Result |
|------|--------|-----------------|
| 1 | Open submit repair form | Form shown |
| 2 | Leave issue/date/time/method empty; Submit | Validation errors shown |

---

### TC-09: View booking

| Step | Action | Expected Result |
|------|--------|-----------------|
| 1 | My Bookings | List shows booking cards |
| 2 | Tap a booking | Details with timeline and history |

---

### TC-10: Update booking status

| Step | Action | Expected Result |
|------|--------|-----------------|
| 1 | Open booking details | Timeline shows current status |
| 2 | Tap Update Status repeatedly | Status advances through flow until Completed |

---

### TC-11: View FAQs

| Step | Action | Expected Result |
|------|--------|-----------------|
| 1 | Dashboard → FAQs & Tips | FAQ list and maintenance tips displayed |

---

### TC-12: Submit support form

| Step | Action | Expected Result |
|------|--------|-----------------|
| 1 | Dashboard → Support | Contact info and form shown |
| 2 | Enter name and message (10+ chars), Send | Success toast shown |
| 3 | Submit with empty/short fields | Validation error shown |
