package com.synapse.social.studioasinc.shared.di

import org.koin.dsl.module
import com.synapse.social.studioasinc.shared.data.FileUploader
import org.koin.core.module.Module

actual val fileUploaderModule: Module = module {
    single { FileUploader() }
}
