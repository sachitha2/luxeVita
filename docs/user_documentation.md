# User Documentation (EcoStay)

## Login / Sign Up
1. Open the app.
2. Choose **Login** or **Sign Up**.
3. Enter your email and password.
4. On success, you are taken to the Home menu.

## Home Menu
From the Home screen you can access:
- Browse & Book Rooms
- Reserve Services
- Offers & Attractions
- Profile
- Manage Bookings (placeholder in this implementation)

## Rooms: Browse & Book
1. Open **Browse & Book Rooms**.
2. (Optional) Filter by room type and price range.
3. (Optional) Pick start/end dates to check availability.
4. Select a room card to open the booking screen.
5. Choose dates and confirm. If the room type has no availability, the app prevents the booking.

## Services: Reserve a Date
1. Open **Reserve Services**.
2. Choose a category (SPA, DINING, CABANAS, TOURS).
3. Select a service to open its reservation screen.
4. Use the built-in calendar list (next 14 days) to pick a date.
5. Tap **Reserve** to create a reservation, or **Cancel reservation** if you already booked that date.

## Offers & Attractions
1. Open **Offers & Attractions**.
2. View active offers (filtered by current date).
3. View nearby attractions.

## Profile
1. Open **Profile**.
2. Set your preferred room type and travel date range.
3. Optionally set a max budget.
4. Booking history is displayed below using your locally stored bookings.

## Notifications
When you create a booking/reservation, the app schedules a local reminder notification using WorkManager.
Promotions are shown via periodic background notifications.

