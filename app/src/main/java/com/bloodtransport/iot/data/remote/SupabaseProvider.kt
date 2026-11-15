package com.bloodtransport.iot.data.remote

import com.bloodtransport.iot.data.config.SupabaseConfig
import io.github.jan.tennert.supabase.SupabaseClient
import io.github.jan.tennert.supabase.createSupabaseClient
import io.github.jan.tennert.supabase.postgrest.Postgrest
import io.github.jan.tennert.supabase.realtime.Realtime
import kotlin.time.Duration.Companion.seconds

/**
 * Singleton provider for the Supabase Client.
 * Configured with Postgrest for REST querying and Realtime for WebSocket change streams.
 */
object SupabaseProvider {

    @Volatile
    private var instance: SupabaseClient? = null

    /**
     * Retrieves or lazily creates the SupabaseClient singleton instance.
     */
    val client: SupabaseClient
        get() = instance ?: synchronized(this) {
            instance ?: createClient().also { instance = it }
        }

    private fun createClient(): SupabaseClient {
        return createSupabaseClient(
            supabaseUrl = SupabaseConfig.SUPABASE_URL,
            supabaseKey = SupabaseConfig.SUPABASE_ANON_KEY
        ) {
            install(Postgrest)
            install(Realtime) {
                // Heartbeat & reconnect configuration
                reconnectDelay = 5.seconds
            }
        }
    }

    /**
     * Resets instance if developer updates config at runtime.
     */
    fun reinitialize() {
        synchronized(this) {
            instance = null
        }
    }
}
