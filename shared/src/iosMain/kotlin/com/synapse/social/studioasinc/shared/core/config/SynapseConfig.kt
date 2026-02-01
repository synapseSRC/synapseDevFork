package com.synapse.social.studioasinc.shared.core.config

actual object SynapseConfig {
    // For iOS, these would typically come from a .plist or build configuration.
    // For now, we will leave them empty or placeholders to pass the build.
    // In Phase 4, we will connect this to the iOS app's configuration.
    actual val SUPABASE_URL: String = ""
    actual val SUPABASE_ANON_KEY: String = ""
    actual val SUPABASE_SYNAPSE_S3_ENDPOINT_URL: String = ""
    actual val SUPABASE_SYNAPSE_S3_ENDPOINT_REGION: String = ""
    actual val SUPABASE_SYNAPSE_S3_ACCESS_KEY_ID: String = ""
    actual val SUPABASE_SYNAPSE_S3_ACCESS_KEY: String = ""
}
