-- 1. Available rooms "right now" in a given building (KST)
-- Usage (Supabase RPC):
--   supabase.rpc('available_rooms_now', { p_building_code: '310' })
CREATE OR REPLACE FUNCTION public.available_rooms_now(p_building_code text)
RETURNS TABLE (
  room_id       BIGINT,
  building_code TEXT,
  room_number   TEXT
) AS $$
BEGIN
  RETURN QUERY
  WITH now_korea AS (
    SELECT
      now() AT TIME ZONE 'Asia/Seoul' AS now_ts,
      (now() AT TIME ZONE 'Asia/Seoul')::time AS t,
      CASE
        WHEN extract(isodow FROM now() AT TIME ZONE 'Asia/Seoul') = 1 THEN '월'::public.day_of_week
        WHEN extract(isodow FROM now() AT TIME ZONE 'Asia/Seoul') = 2 THEN '화'::public.day_of_week
        WHEN extract(isodow FROM now() AT TIME ZONE 'Asia/Seoul') = 3 THEN '수'::public.day_of_week
        WHEN extract(isodow FROM now() AT TIME ZONE 'Asia/Seoul') = 4 THEN '목'::public.day_of_week
        WHEN extract(isodow FROM now() AT TIME ZONE 'Asia/Seoul') = 5 THEN '금'::public.day_of_week
        WHEN extract(isodow FROM now() AT TIME ZONE 'Asia/Seoul') = 6 THEN '토'::public.day_of_week
        ELSE '일'::public.day_of_week
      END AS dow
  )
  SELECT
    r.id,
    b.code,
    r.room_number
  FROM public.rooms r
  JOIN public.buildings b ON r.building_id = b.id
  CROSS JOIN now_korea n
  WHERE b.code = p_building_code
    -- ❶ Room is not currently in official timetable
    AND NOT EXISTS (
          SELECT 1
          FROM public.timetable_entries te
          WHERE te.room_id = r.id
            AND te.day = n.dow
            AND n.t BETWEEN te.start_time AND te.end_time
        )
    -- ❷ Room is not currently reserved
    AND NOT EXISTS (
          SELECT 1
          FROM public.reservations rs
          WHERE rs.room_id = r.id
            AND rs.status = 'confirmed'
            AND n.now_ts BETWEEN rs.start_at AND rs.end_at
        )
  ORDER BY r.room_number;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Allow frontend to call this function
GRANT EXECUTE ON FUNCTION public.available_rooms_now(text)
TO anon, authenticated;
