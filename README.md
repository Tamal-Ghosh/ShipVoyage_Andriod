# ShipVoyage (Android)

ShipVoyage is a dual-role cruise booking Android app (Admin & Passenger) built with Java, Firebase, and Material Design 3. It supports tour discovery, room selection with color-coded availability, secure payments (Visa/bKash), and real-time admin management of tours, ships, rooms, schedules, bookings, customers, and featured photos.

## Features
- Dual roles: Admin dashboard and Passenger booking flow
- Tour search and upcoming trips (future-dated instances only)
- Room selection with legend (Single/Double/Selected/Booked) and capacity checks
- Payments: Visa and bKash forms with validation; bookings lock after payment
- Booking lifecycle: PENDING vs CONFIRMED; history and status tracking
- Admin management: tours, ships, rooms, tour instances, bookings, customers, featured photos
- Real-time sync via Firebase Realtime Database; email/password authentication

## Architecture
- Single-Activity containers: `AdminMainActivity`, `PassengerMainActivity`; Navigation Component for fragment flows
- DAO layer for Firebase CRUD: User, Tour, Ship, Room, TourInstance, Booking, FeaturedPhoto
- Threading: shared ThreadPool for background Firebase calls; UI updates on main thread
- RecyclerView + DiffUtil: efficient lists for tours/ships/rooms/bookings/etc.
- Material Design 3 UI: unified card styles, TextInputLayout, MaterialButton

## Tech Stack
- Language: Java 11
- Android: minSdk 24, targetSdk 36
- Firebase: Authentication, Realtime Database
- UI: AndroidX, Material Design 3, ConstraintLayout, Navigation Component, RecyclerView

## Project Structure (high level)
```
app/src/main/
	# ShipVoyage (Android)

	ShipVoyage is a dual-role cruise booking app (Admin & Passenger) built with Java, Firebase, and Material Design 3. It delivers tour discovery, color-coded room selection, secure payments (Visa/bKash), and real-time admin control over tours, ships, rooms, schedules, bookings, customers, and featured photos.

	## Highlights
	- Dual portals: Admin dashboard + Passenger booking experience
	- Realtime: Firebase Auth + Realtime Database for live data and sessions
	- Booking integrity: PENDING vs CONFIRMED; rooms lock only after payment
	- Room UX: Color legend (Single/Double/Selected/Booked) with capacity checks
	- Payments: Visa and bKash forms with validation before confirmation
	- Admin ops: Manage tours, ships, rooms, tour instances, bookings, customers, featured photos

	## Architecture
	- Containers: `AdminMainActivity`, `PassengerMainActivity` with Navigation Component (fragment flows, shared back stack)
	- Data layer: DAO pattern for Firebase CRUD (User, Tour, Ship, Room, TourInstance, Booking, FeaturedPhoto)
	- Threading: Shared ThreadPool for background Firebase calls; UI updates on main thread
	- UI lists: RecyclerView + DiffUtil for efficient updates across 10 adapters
	- UI system: Material Design 3 with unified card styling, TextInputLayout, MaterialButton

	## Tech Stack
	- Language: Java 11
	- Android: minSdk 24, targetSdk 36
	- Firebase: Authentication, Realtime Database
	- UI: AndroidX, Material Design 3, ConstraintLayout, Navigation Component, RecyclerView

	## Project Structure (high level)
	```
	app/src/main/
	  java/com/example/shipvoyage/
	    ui/auth/                # Login, Signup, Role select
	    ui/admin/               # Admin dashboard & management fragments
	    ui/passenger/           # Passenger home, bookings, profile, support
	    adapter/                # RecyclerView adapters (DiffUtil)
	    dao/                    # Firebase data access objects
	    model/                  # User, Tour, Ship, Room, TourInstance, Booking, FeaturedPhoto
	    util/                   # ThreadPool
	  res/
	    layout/                 # Activities, fragments, item cards
	    navigation/             # Admin & passenger nav graphs
	    values/                 # Colors, strings, styles, dimens
	```

	## Prerequisites
	- Android Studio Flamingo/Koala or newer
	- JDK 11
	- Android SDK 36 installed
	- Firebase project with Realtime Database & Auth enabled
	- `google-services.json` placed in `app/`

	## Setup
	1) Clone and open in Android Studio.
	2) Add your `app/google-services.json`.
	3) Configure Firebase rules/keys as needed.
	4) Sync Gradle.

	## Run
	Use Android Studio Run on a device/emulator (API 24+).

	## Build (APK)
	```powershell
	./gradlew.bat assembleDebug
	```
	Result: `app/build/outputs/apk/debug/app-debug.apk`

	## Core Flows
	- Passenger: Login → Home search → View results → Room selection → Payment (Visa/bKash) → Confirmation → My Bookings
	- Admin: Login → Dashboard → Manage (Tours/Ships/Rooms/Instances/Photos) → View Bookings → Customers → Profile

	## Implementation Notes
	- Availability: Rooms are blocked only after CONFIRMED status (post-payment)
	- Scheduling: Upcoming tours filter out past dates
	- Legend: Green Single, Blue Double, Dark Blue Selected, Orange Booked

	## License
	MIT (see `LICENSE`).