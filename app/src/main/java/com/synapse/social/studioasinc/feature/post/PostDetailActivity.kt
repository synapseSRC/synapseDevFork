package com.synapse.social.studioasinc.feature.post

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.synapse.social.studioasinc.CreatePostActivity
import com.synapse.social.studioasinc.ProfileActivity
import com.synapse.social.studioasinc.feature.post.postdetail.PostDetailScreen
import com.synapse.social.studioasinc.feature.shared.theme.SynapseTheme

class PostDetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_POST_ID = "post_id"
        const val EXTRA_AUTHOR_UID = "author_uid"

        fun start(context: Context, postId: String, authorUid: String? = null) {
            context.startActivity(Intent(context, PostDetailActivity::class.java).apply {
                putExtra(EXTRA_POST_ID, postId)
                putExtra(EXTRA_AUTHOR_UID, authorUid)
            })
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Removed WindowCompat.setDecorFitsSystemWindows - using default window behavior

        val postId = intent.getStringExtra(EXTRA_POST_ID)

        if (postId == null) {
            finish()
            return
        }

        setContent {
            SynapseTheme {
                PostDetailScreen(
                    postId = postId,
                    onNavigateBack = { finish() },
                    onNavigateToProfile = { userId -> navigateToProfile(userId) },
                    onNavigateToEditPost = { editPostId -> navigateToEditPost(editPostId) }
                )
            }
        }
    }

    private fun navigateToProfile(userId: String) {
        startActivity(Intent(this, ProfileActivity::class.java).apply {
            putExtra("user_id", userId)
        })
    }

    private fun navigateToEditPost(postId: String) {
        startActivity(Intent(this, CreatePostActivity::class.java).apply {
            putExtra("edit_post_id", postId)
        })
    }
}
