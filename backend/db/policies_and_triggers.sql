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
-- 2. Public read access for static data
------------------------------------------------------------

-- Buildings
CREATE POLICY public_read_buildings
ON public.buildings
FOR SELECT
USING (true);

-- Rooms
CREATE POLICY public_read_rooms
ON public.rooms
FOR SELECT
USING (true);

-- Timetable entries
CREATE POLICY public_read_timetable
ON public.timetable_entries
FOR SELECT
USING (true);

------------------------------------------------------------
-- 3. Profiles (one row per user)
------------------------------------------------------------

-- Allow inserting profile rows (used by trigger / admin scripts)
DROP POLICY IF EXISTS insert_own_profile ON public.profiles;
DROP POLICY IF EXISTS insert_profile_unrestricted ON public.profiles;

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
-- 4. Favorites (user → rooms)
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
-- 5. Reservations (professors/admin only)
------------------------------------------------------------

-- Users can read only their own reservations
CREATE POLICY reservations_select
ON public.reservations
FOR SELECT
USING (user_id = auth.uid());

-- Only professors/admin can INSERT/UPDATE/DELETE their reservations
CREATE POLICY reservations_professor_write
ON public.reservations
FOR ALL   -- INSERT, UPDATE, DELETE
USING (
  user_id = auth.uid()
  AND EXISTS (
    SELECT 1
    FROM public.profiles p
    WHERE p.id = auth.uid()
      AND p.role IN ('professor', 'admin')
  )
)
WITH CHECK (
  user_id = auth.uid()
  AND EXISTS (
    SELECT 1
    FROM public.profiles p
    WHERE p.id = auth.uid()
      AND p.role IN ('professor', 'admin')
  )
);

------------------------------------------------------------
-- 6. Trigger: auto-create profile on new auth user
------------------------------------------------------------

-- Clean up any old versions
DROP FUNCTION IF EXISTS public.handle_new_auth_user();
DROP FUNCTION IF EXISTS public.handle_new_user();
DROP TRIGGER IF EXISTS on_auth_user_created ON auth.users;

-- Trigger function: insert into public.profiles when a new auth.users row is created
CREATE FUNCTION public.handle_new_user()
RETURNS trigger AS $$
BEGIN
  INSERT INTO public.profiles (id)
  VALUES (NEW.id)
  ON CONFLICT (id) DO NOTHING;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Attach trigger to auth.users
CREATE TRIGGER on_auth_user_created
AFTER INSERT ON auth.users
FOR EACH ROW
EXECUTE PROCEDURE public.handle_new_user();
