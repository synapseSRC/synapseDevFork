package com.synapse.social.studioasinc.shared.core.config

import com.synapse.social.studioasinc.shared.BuildConfig

actual object SynapseConfig {
    actual val SUPABASE_URL: String = BuildConfig.SUPABASE_URL
    actual val SUPABASE_ANON_KEY: String = BuildConfig.SUPABASE_ANON_KEY
    actual val SUPABASE_SYNAPSE_S3_ENDPOINT_URL: String = BuildConfig.SUPABASE_SYNAPSE_S3_ENDPOINT_URL
    actual val SUPABASE_SYNAPSE_S3_ENDPOINT_REGION: String = BuildConfig.SUPABASE_SYNAPSE_S3_ENDPOINT_REGION
    actual val SUPABASE_SYNAPSE_S3_ACCESS_KEY_ID: String = BuildConfig.SUPABASE_SYNAPSE_S3_ACCESS_KEY_ID
    actual val SUPABASE_SYNAPSE_S3_ACCESS_KEY: String = BuildConfig.SUPABASE_SYNAPSE_S3_ACCESS_KEY
}
