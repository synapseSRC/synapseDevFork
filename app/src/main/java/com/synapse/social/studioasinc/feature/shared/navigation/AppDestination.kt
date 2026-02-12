package com.synapse.social.studioasinc.feature.shared.navigation

sealed class AppDestination(val route: String) {
    object Auth : AppDestination("auth")
    object Home : AppDestination("home")
    object Profile : AppDestination("profile/{userId}") {
        fun createRoute(userId: String) = "profile/$userId"
    }
    object Inbox : AppDestination("inbox")
    object Search : AppDestination("search")
    object PostDetail : AppDestination("post_detail/{postId}") {
        fun createRoute(postId: String) = "post_detail/$postId"
    }
    object CreatePost : AppDestination("create_post?postId={postId}&type={type}") {
        fun createRoute(postId: String? = null, type: String = "post") =
            "create_post?type=$type" + (if (postId != null) "&postId=$postId" else "")
    }
    object Settings : AppDestination("settings")
    object EditProfile : AppDestination("edit_profile")
    object RegionSelection : AppDestination("region_selection")
    object PhotoHistory : AppDestination("photo_history/{type}") {
        fun createRoute(type: String) = "photo_history/$type"
    }
    object FollowList : AppDestination("follow_list/{userId}/{type}") {
        fun createRoute(userId: String, type: String) = "follow_list/$userId/$type"
    }
    object Chat : AppDestination("chat/{chatId}?userId={userId}") {
        fun createRoute(chatId: String, userId: String? = null) =
            if (userId != null) "chat/$chatId?userId=$userId" else "chat/$chatId"
    }
    object StoryViewer : AppDestination("story_viewer/{userId}") {
        fun createRoute(userId: String) = "story_viewer/$userId"
    }
    object StoryCreator : AppDestination("story_creator")
}
