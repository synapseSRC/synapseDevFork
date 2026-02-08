package com.synapse.social.studioasinc.shared.di

import com.synapse.social.studioasinc.shared.data.local.IosSecureStorage
import com.synapse.social.studioasinc.shared.data.local.SecureStorage
import org.koin.dsl.module
import org.koin.core.module.Module

actual val secureStorageModule = module {
    single<SecureStorage> { IosSecureStorage() }
}
