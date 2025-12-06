# 📘 Classroom Informer – Database Documentation

This folder contains all SQL files and instructions for setting up and managing the **Supabase/PostgreSQL database** used in the Classroom Informer application.

This README is written for **Team 7 members (Frontend/Backend/Android)** so everyone can understand how the backend data layer works.

---

## 📁 Folder Structure

```
backend/database/
│
├── schema.sql
├── import_timetable.sql
├── functions.sql
├── policies_and_triggers.sql
└── README.md   ← (this file)
```

---

## ✔ schema.sql

Defines all core tables, enums, indexes, and relationships:

- buildings  
- rooms  
- raw_timetable_csv  
- timetable_entries  
- profiles  
- favorites  
- reservations  
- day_of_week ENUM  
- indexes for performance  

---

## ✔ import_timetable.sql

Used for importing the official CAU timetable CSV into normalized tables:

- Inserts buildings  
- Inserts rooms  
- Inserts timetable entries  
- Converts Korean weekday text → ENUM  

Used **once per semester**, or whenever timetable updates happen.

---

## ✔ functions.sql

Contains application business logic functions.

### `available_rooms_now(p_building_code text)`

Returns all rooms *currently empty* by checking:

- Timetable (no ongoing lecture)  
- Reservations (no active confirmed reservation)  
- Korea time zone (Asia/Seoul)

Frontend calls it via:

```kotlin
supabase.rpc("available_rooms_now", mapOf("p_building_code" to "310"))
```

---

## ✔ policies_and_triggers.sql

Contains:

### 🔒 Row-Level Security (RLS) Policies

| Table | Read | Write |
|-------|------|--------|
| buildings | Public | Backend only |
| rooms | Public | Backend only |
| timetable_entries | Public | Backend only |
| profiles | User sees only own row | User updates only own row |
| favorites | User sees only own | User modifies own |
| reservations | User sees own | Only professors/admin |

### 👤 Auth → Profile Trigger

Automatically creates a row in `public.profiles` when a new Supabase Auth user signs up.

---

## 👤 Test Users for Development

Useful for UI + backend preview.

### 👨‍🏫 Professors (can create reservations)

| Email | Password | Role |
|-------|----------|------|
| prof1@classroom.test | test1234 | professor |
| prof2@classroom.test | test1234 | professor |

### 🧑‍🎓 Students (normal users)

| Email | Password | Role |
|-------|----------|------|
| student1@classroom.test | test1234 | student |
| yamin310702@gmail.com | test1234 | student |

Students **cannot** create reservations but can search rooms + save favorites.

---

## 🗃 How to Load Timetable Data (Backend Only)

### 1. Upload the CSV into Supabase

Supabase → Table Editor → `raw_timetable_csv` → **Upload CSV**.

### 2. Run Import Script

In SQL Editor:

```sql
\i backend/database/import_timetable.sql
```

This will:

- Insert buildings  
- Insert rooms  
- Insert class schedules  
- Convert data into the normalized `timetable_entries` table  

---

## 🏗 Frontend Usage Guide (Android/Kotlin)

### 1. Get real-time available rooms

```kotlin
val result = supabase.rpc(
    "available_rooms_now",
    mapOf("p_building_code" to "310")
)
```

Returns:

```json
[
  { "room_id": 123, "building_code": "310", "room_number": "603" }
]
```

### 2. Fetch user favorites

Authenticated users automatically have:

- a `profiles` row  
- access only to their own favorite rooms  

Favorites format example:

```json
{
  "user_id": "uuid",
  "room_id": 123
}
```

---

## 📦 Deployment Notes

These SQL files:

- do **not** run automatically  
- document the backend schema  
- help reproduce the database if needed  
- help onboard new developers  

The live Supabase already contains the actual tables.

---

## 👍 Maintainer Notes

**Backend Developer:** *Yamin Myat Aung*

Responsibilities completed:

- Designed and set up database schema  
- Implemented ETL import for timetable CSV  
- Built logic functions (available rooms, etc.)  
- Implemented RLS and security model  
- Added test users  
- Created Supabase auth → profiles trigger  


