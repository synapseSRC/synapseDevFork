package com.synapse.social.studioasinc.shared.di

import com.synapse.social.studioasinc.shared.data.FileUploader
import com.synapse.social.studioasinc.shared.data.database.StorageDatabase
import com.synapse.social.studioasinc.shared.data.database.Post
import com.synapse.social.studioasinc.shared.data.database.Comment
import com.synapse.social.studioasinc.shared.data.database.User
import com.synapse.social.studioasinc.shared.data.database.mediaItemListAdapter
import com.synapse.social.studioasinc.shared.data.database.pollOptionListAdapter
import com.synapse.social.studioasinc.shared.data.database.reactionMapAdapter
import com.synapse.social.studioasinc.shared.data.database.reactionTypeAdapter
import com.synapse.social.studioasinc.shared.data.database.postMetadataAdapter
import com.synapse.social.studioasinc.shared.data.database.intAdapter
import com.synapse.social.studioasinc.shared.data.database.booleanAdapter
import com.synapse.social.studioasinc.shared.data.repository.StorageRepositoryImpl



import com.synapse.social.studioasinc.shared.data.source.remote.CloudinaryUploadService
import com.synapse.social.studioasinc.shared.data.source.remote.ImgBBUploadService
import com.synapse.social.studioasinc.shared.data.source.remote.R2UploadService
import com.synapse.social.studioasinc.shared.data.source.remote.SupabaseUploadService
import com.synapse.social.studioasinc.shared.domain.repository.StorageRepository
import com.synapse.social.studioasinc.shared.domain.usecase.GetStorageConfigUseCase
import com.synapse.social.studioasinc.shared.domain.usecase.UpdateStorageProviderUseCase
import com.synapse.social.studioasinc.shared.domain.usecase.UploadMediaUseCase
import com.synapse.social.studioasinc.shared.domain.usecase.ValidateProviderConfigUseCase


import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.module.Module
import org.koin.dsl.module
import app.cash.sqldelight.db.SqlDriver

expect val storageDriverModule: Module
expect val fileUploaderModule: Module

val storageModule = module {
    includes(storageDriverModule)
    includes(fileUploaderModule)
    includes(secureStorageModule)

    single {
        StorageDatabase(
            driver = get(),
            PostAdapter = Post.Adapter(
                mediaItemsAdapter = mediaItemListAdapter,
                pollOptionsAdapter = pollOptionListAdapter,
                reactionsAdapter = reactionMapAdapter,
                userReactionAdapter = reactionTypeAdapter,
                metadataAdapter = postMetadataAdapter,
                likesCountAdapter = intAdapter,
                commentsCountAdapter = intAdapter,
                viewsCountAdapter = intAdapter,
                resharesCountAdapter = intAdapter,
                userPollVoteAdapter = intAdapter
            ),
            CommentAdapter = Comment.Adapter(
                likesCountAdapter = intAdapter,
                repliesCountAdapter = intAdapter
            ),
            UserAdapter = User.Adapter(
                followersCountAdapter = intAdapter,
                followingCountAdapter = intAdapter,
                postsCountAdapter = intAdapter
            )
        )
    }

    single<StorageRepository> { StorageRepositoryImpl(get(), get()) }




    single {
        HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    prettyPrint = true
                    isLenient = true
                })
            }
        }
    }

    single { CloudinaryUploadService(get()) }
    single { ImgBBUploadService(get()) }
    single { SupabaseUploadService(get()) }
    single { R2UploadService(get()) }

    single { GetStorageConfigUseCase(get()) }
    single { UpdateStorageProviderUseCase(get()) }
    single { ValidateProviderConfigUseCase() }
    single { UploadMediaUseCase(get(), get(), get(), get(), get(), get()) }


}
