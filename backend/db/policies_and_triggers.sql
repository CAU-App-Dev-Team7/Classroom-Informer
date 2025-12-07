------------------------------------------------------------
-- 1. Enable Row Level Security
------------------------------------------------------------

ALTER TABLE public.buildings          ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.rooms              ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.timetable_entries  ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.profiles           ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.favorites          ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.reservations       ENABLE ROW LEVEL SECURITY;

------------------------------------------------------------
-- 2. Drop existing policies (if any)
------------------------------------------------------------

-- buildings / rooms / timetable
DROP POLICY IF EXISTS public_read_buildings   ON public.buildings;
DROP POLICY IF EXISTS public_read_rooms       ON public.rooms;
DROP POLICY IF EXISTS public_read_timetable   ON public.timetable_entries;

-- favorites
DROP POLICY IF EXISTS favorites_select        ON public.favorites;
DROP POLICY IF EXISTS favorites_write         ON public.favorites;

-- profiles
DROP POLICY IF EXISTS allow_initial_profile_insert ON public.profiles;
DROP POLICY IF EXISTS insert_profile_unrestricted  ON public.profiles;
DROP POLICY IF EXISTS view_own_profile             ON public.profiles;
DROP POLICY IF EXISTS update_own_profile           ON public.profiles;

-- reservations
DROP POLICY IF EXISTS reservations_select          ON public.reservations;
DROP POLICY IF EXISTS reservations_professor_write ON public.reservations;

------------------------------------------------------------
-- 3. Public read access for static data
------------------------------------------------------------

CREATE POLICY public_read_buildings
ON public.buildings
FOR SELECT
USING (true);

CREATE POLICY public_read_rooms
ON public.rooms
FOR SELECT
USING (true);

CREATE POLICY public_read_timetable
ON public.timetable_entries
FOR SELECT
USING (true);

------------------------------------------------------------
-- 4. Profiles (one row per user)
------------------------------------------------------------

-- Supabase default-style policy (matches your export)
CREATE POLICY allow_initial_profile_insert
ON public.profiles
FOR INSERT
WITH CHECK (true);

-- Your extra insert policy (also in export)
CREATE POLICY insert_profile_unrestricted
ON public.profiles
FOR INSERT
WITH CHECK (true);

-- Each user can see only their own profile
CREATE POLICY view_own_profile
ON public.profiles
FOR SELECT
USING (id = auth.uid());

-- Each user can update only their own profile
CREATE POLICY update_own_profile
ON public.profiles
FOR UPDATE
USING (id = auth.uid())
WITH CHECK (id = auth.uid());

------------------------------------------------------------
-- 5. Favorites (user → rooms)
------------------------------------------------------------

CREATE POLICY favorites_select
ON public.favorites
FOR SELECT
USING (user_id = auth.uid());

CREATE POLICY favorites_write
ON public.favorites
FOR ALL
USING (user_id = auth.uid())
WITH CHECK (user_id = auth.uid());

------------------------------------------------------------
-- 6. Reservations (professors/admin only)
------------------------------------------------------------

-- Users can read only their own reservations
CREATE POLICY reservations_select
ON public.reservations
FOR SELECT
USING (user_id = auth.uid());

-- Only professors/admin can INSERT/UPDATE/DELETE their reservations
CREATE POLICY reservations_professor_write
ON public.reservations
FOR ALL
USING (
  (user_id = auth.uid())
  AND EXISTS (
    SELECT 1
    FROM public.profiles p
    WHERE p.id = auth.uid()
      AND p.role IN ('professor', 'admin')
  )
)
WITH CHECK (
  (user_id = auth.uid())
  AND EXISTS (
    SELECT 1
    FROM public.profiles p
    WHERE p.id = auth.uid()
      AND p.role IN ('professor', 'admin')
  )
);

------------------------------------------------------------
-- 7. Trigger: auto-create profile on new auth user
------------------------------------------------------------

-- Function body is defined in functions.sql as public.handle_new_user()
-- Here we only (re)attach the trigger.

DROP TRIGGER IF EXISTS on_auth_user_created ON auth.users;

CREATE TRIGGER on_auth_user_created
AFTER INSERT ON auth.users
FOR EACH ROW
EXECUTE FUNCTION public.handle_new_user();
