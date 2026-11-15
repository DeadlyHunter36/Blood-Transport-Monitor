package com.bloodtransport.iot.data.config

/**
 * Supabase configuration constants for the Blood Transport Temperature Monitoring System.
 * 
 * Instructions:
 * 1. Log in to your Supabase project dashboard (https://supabase.com/dashboard).
 * 2. Navigate to Project Settings -> API.
 * 3. Copy the "Project URL" and paste it into SUPABASE_URL below.
 * 4. Copy the "anon" "public" API Key and paste it into SUPABASE_ANON_KEY below.
 * 5. Ensure the "telemetry_logs" table is enabled in Realtime publication:
 *    ALTER PUBLICATION supabase_realtime ADD TABLE telemetry_logs;
 */
object SupabaseConfig {

    // =========================================================================
    // INSERT YOUR SUPABASE CREDENTIALS HERE:
    // =========================================================================
    const val SUPABASE_URL: String = "https://YOUR_PROJECT_REF.supabase.co"
    const val SUPABASE_ANON_KEY: String = "YOUR_SUPABASE_ANON_KEY_HERE"
    // =========================================================================

    // PostgreSQL Realtime configuration
    const val SCHEMA_PUBLIC: String = "public"
    const val TABLE_TELEMETRY_LOGS: String = "telemetry_logs"
    const val REALTIME_CHANNEL_NAME: String = "telemetry_realtime_channel"

    /**
     * Determines whether the developer has replaced the default placeholders
     * with valid Supabase project credentials.
     */
    val isConfigured: Boolean
        get() = SUPABASE_URL.isNotBlank() &&
                !SUPABASE_URL.contains("YOUR_PROJECT_REF") &&
                SUPABASE_ANON_KEY.isNotBlank() &&
                !SUPABASE_ANON_KEY.contains("YOUR_SUPABASE_ANON_KEY")
}
