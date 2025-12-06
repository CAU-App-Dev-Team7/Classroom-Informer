backend/database/
│
├── schema.sql
├── import_timetable.sql
├── functions.sql
├── policies_and_triggers.sql
└── README.md ← (this file)
Here’s what each file does:
✔ schema.sql
Defines all tables, enums, indexes, and relationships:
buildings
rooms
raw_timetable_csv
timetable_entries
profiles
favorites
reservations
day_of_week ENUM
Useful indexes to speed up queries
✔ import_timetable.sql
Scripts for importing the official CAU CSV timetable into normalized tables:
Insert buildings
Insert rooms
Insert all timetable entries
Convert Korean day text → ENUM automatically
This is used one time per semester, or whenever we import updated timetables.
✔ functions.sql
Contains all business logic stored procedures, especially:
available_rooms_now(p_building_code text)
Returns all rooms currently empty in a selected building by checking:
Official timetable (no ongoing lecture)
Reservations (no confirmed reservation)
Current Korea time (Asia/Seoul)
Frontend (Android Kotlin) calls it via:
supabase.rpc("available_rooms_now", mapOf("p_building_code" to "310"))
✔ policies_and_triggers.sql
Contains:
🔒 Row-Level Security (RLS) Policies
Public can read buildings, rooms, timetable
Users can read/update only their own profile
Users can read/write only their own favorites
Only professors can create/update/delete reservations
Students cannot create reservations
👤 Auth → Profile Trigger
Automatically creates a row in public.profiles when a new Supabase Auth user signs up.
👤 Test Users for Development
For UI/feature testing, 5 fake accounts are already created in Supabase:
👨‍🏫 Professors (can create reservations)
Email Password Role
prof1@classroom.test Test1234! professor
prof2@classroom.test Test1234! professor
→ These accounts can reserve rooms in the app.
🧑‍🎓 Students (normal users)
Email Password Role
student1@classroom.test Test1234! student
student2@classroom.test Test1234! student
student3@classroom.test Test1234! student
→ These accounts cannot make reservations, but can search rooms + save favorites.
🗃 How to Load Timetable Data (for backend only)

1. Upload your CSV file in Supabase
   Use Table Editor → raw_timetable_csv → Upload CSV.
2. Run import script
   In SQL Editor, run:
   -- Populate buildings, rooms, timetable entries
   \i backend/database/import_timetable.sql
   This will automatically:
   Insert building codes
   Insert rooms
   Insert course schedules
   Normalize data into timetable_entries
   🏗 How Android Frontend Should Use the Database
3. Get available rooms in a building now
   val result = supabase.rpc(
   "available_rooms_now",
   mapOf("p_building_code" to "310")
   )
   Returns:
   [
   { "room_id": 123, "building_code": "310", "room_number": "603" },
   ...
   ]
4. Get user favorites
   Authenticated user automatically has:
   row in profiles
   RLS allows only their favorites
   Favorites table format:
   {
   "user_id": "...",
   "room_id": 123
   }
   🔐 RLS Overview for Teammates
   Table Read Write
   buildings Everyone Backend only
   rooms Everyone Backend only
   timetable_entries Everyone Backend only
   profiles User sees only own row User updates only own row
   favorites User sees own User modifies own
   reservations User sees own Only professors/admin
   📦 Deployment Notes
   These SQL files are not auto-running.
   Supabase schema already exists
