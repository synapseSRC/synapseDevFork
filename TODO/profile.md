# 👤 Profile - TODO List

**Total TODOs:** 2

---

## 📋 TODO #1: Implement share via message functionality

**🤖 AI Agent Prompt:**

You are tasked with implementing the following feature in the Synapse social media app.

**Context:** This TODO is located in `PostFeed.kt` which is part of the profile module.

**Task:** Implement share via message functionality

**Implementation Guidelines:**
- Review the surrounding code context below
- Follow Kotlin/Android best practices
- Maintain consistency with existing code patterns
- Ensure proper error handling
- Add appropriate comments

**Acceptance Criteria:**
- [ ] Feature is fully implemented and functional
- [ ] Code follows project conventions
- [ ] No compilation errors
- [ ] Tested manually (if UI component)

**Code Context:**
```kotlin
                    putExtra(EXTRA_SHARED_POST_ID, selectedPostId)
                }
                context.startActivity(intent)
                showShareSheet = false
            },
            onShareViaMessage = { /* TODO: Implement */ },
            onShareExternal = {
                val post = posts.find { it.id == selectedPostId }
                post?.let {
                    val shareText = buildString {
                        if (!it.postText.isNullOrBlank()) {
```

**Location:** `app/src/main/java/com/synapse/social/studioasinc/feature/profile/profile/components/PostFeed.kt:112`


---

## 📋 TODO #2: Implement open linked account URL

**🤖 AI Agent Prompt:**

You are tasked with implementing the following feature in the Synapse social media app.

**Context:** This TODO is located in `UserDetailsSection.kt` which is part of the profile module.

**Task:** Implement open linked account URL

**Implementation Guidelines:**
- Review the surrounding code context below
- Follow Kotlin/Android best practices
- Maintain consistency with existing code patterns
- Ensure proper error handling
- Add appropriate comments

**Acceptance Criteria:**
- [ ] Feature is fully implemented and functional
- [ ] Code follows project conventions
- [ ] No compilation errors
- [ ] Tested manually (if UI component)

**Code Context:**
```kotlin
}

@Composable
private fun LinkedAccountChip(account: LinkedAccount) {
    AssistChip(
        onClick = { /* TODO: Open link */ },
        label = { Text(account.platform) },
        leadingIcon = {
            Icon(
                imageVector = getPlatformIcon(account.platform),
                contentDescription = null,
```

**Location:** `app/src/main/java/com/synapse/social/studioasinc/feature/profile/profile/components/UserDetailsSection.kt:344`


---

