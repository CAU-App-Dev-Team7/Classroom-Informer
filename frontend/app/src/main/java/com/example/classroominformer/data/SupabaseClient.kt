package com.example.classroominformer.data

import com.example.classroominformer.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.FlowType
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime

object SupaBaseClient {

    val client: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_KEY
        ) {
            // DB access
            install(Postgrest)

            // ✅ Auth plugin (replaces old GoTrue)
            install(Auth) {
                // you can tweak this later if needed
                flowType = FlowType.PKCE
            }

            // Realtime (optional but nice to have)
            install(Realtime)
        }
    }
}
