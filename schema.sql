-- =============================================================================
-- Blood Transport Temperature Monitoring System
-- Supabase PostgreSQL Schema & Realtime Setup
-- =============================================================================

-- 1. Create telemetry_logs table
CREATE TABLE IF NOT EXISTS public.telemetry_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    trip_id TEXT NOT NULL,
    temperature DOUBLE PRECISION NOT NULL,
    battery_percentage INT NOT NULL CHECK (battery_percentage >= 0 AND battery_percentage <= 100),
    signal_strength INT NOT NULL,
    timestamp TEXT NOT NULL DEFAULT to_char(NOW(), 'YYYY-MM-DD"T"HH24:MI:SS"Z"'),
    is_tampered BOOLEAN NOT NULL DEFAULT FALSE
);

-- 2. Enable Row Level Security (RLS)
ALTER TABLE public.telemetry_logs ENABLE ROW LEVEL SECURITY;

-- 3. Create Public Read Policy for anon API keys
CREATE POLICY "Allow public read access to telemetry_logs"
    ON public.telemetry_logs
    FOR SELECT
    TO anon, authenticated
    USING (true);

-- 4. Create Public Insert Policy for IoT devices/anon keys
CREATE POLICY "Allow public insert access to telemetry_logs"
    ON public.telemetry_logs
    FOR INSERT
    TO anon, authenticated
    WITH CHECK (true);

-- 5. CRUCIAL: Enable Realtime WebSockets on telemetry_logs table
ALTER PUBLICATION supabase_realtime ADD TABLE public.telemetry_logs;

-- =============================================================================
-- SAMPLE TEST DATA INSERTIONS (Run these in SQL Editor to test live WebSocket updates)
-- =============================================================================

-- Test A: Normal reading for Whole Blood (4.1°C, intact)
INSERT INTO public.telemetry_logs (trip_id, temperature, battery_percentage, signal_strength, is_tampered)
VALUES ('TRIP-8842-US', 4.1, 95, 92, false);

-- Test B: Temperature Excursion Alert (8.7°C, exceeds safe threshold 6.0°C)
-- Should turn background solid RED and trigger heavy haptic vibration!
INSERT INTO public.telemetry_logs (trip_id, temperature, battery_percentage, signal_strength, is_tampered)
VALUES ('TRIP-8842-US', 8.7, 94, 88, false);

-- Test C: Security Breach / Tamper Alert (is_tampered = true)
-- Should turn background solid YELLOW, display "DATA INTEGRITY COMPROMISED", and trigger heavy vibration!
INSERT INTO public.telemetry_logs (trip_id, temperature, battery_percentage, signal_strength, is_tampered)
VALUES ('TRIP-8842-US', 4.3, 93, 85, true);

-- Test D: Return to Normal (3.8°C, intact)
-- Returns background to high-contrast solid BLACK with white typography.
INSERT INTO public.telemetry_logs (trip_id, temperature, battery_percentage, signal_strength, is_tampered)
VALUES ('TRIP-8842-US', 3.8, 92, 90, false);
