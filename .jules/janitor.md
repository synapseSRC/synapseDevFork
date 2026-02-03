# Janitor's Journal - Cleanup Insights

## Cleanup Baseline - 2025-05-24

Established the initial cleanup baseline for the Synapse Android codebase.

### Unused Resource Statistics
- **Drawables:** 1 identified (`button_follow_background.xml`)
- **Layouts:** 1 identified (`component_follow_button.xml`)
- **Strings:** 3 identified (`poll_votes_count`, `poll_vote_submitted`, `poll_vote_failed`)
- **Other Resources:** 0

### Dead Code Identified
- **Legacy Components:**
    - `com.synapse.social.studioasinc.feature.shared.components.legacy.FollowButton`: View-based follow button, replaced by Compose versions.
    - `com.synapse.social.studioasinc.feature.shared.components.legacy.PollDisplay`: Unused Composable.
- **Debris Files:**
    - `app/src/main/java/com/synapse/social/studioasinc/feature/shared/navigation/DeepLinkHandler.kt.bak`
    - `app/src/main/java/com/synapse/social/studioasinc/feature/shared/navigation/AppNavGraph.kt.bak`
    - `app/src/main/java/com/synapse/social/studioasinc/core/util/NavigationMigration.kt.bak`

### Deprecated API Usage
- `InboxActivity` and `ProfileActivity` are marked as `@deprecated` and recommended to be replaced by `InboxScreen` and `ProfileScreen` within the `MainActivity` navigation graph. (Cleanup deferred to ensure safety).

### Application Size Baseline
- Baseline establishment failed due to build timeout. Impact will be measured by file count and code line reduction.

---

## 2025-05-24: Legacy Component Cleanup

### Pattern of Accumulation
Legacy View-based components were left in the codebase during the migration to Jetpack Compose. Specifically, a `FollowButton` custom view and an unused `PollDisplay` Composable were found in a `legacy` package.

### Discovery Method
Manual inspection of the `legacy` package combined with `grep` to verify zero usages of these classes and their associated resources (`component_follow_button.xml`, `button_follow_background.xml`, and poll-related strings).

### Impact
- Removal of 2 dead Kotlin classes.
- Removal of 1 unused layout.
- Removal of 1 unused drawable.
- Removal of 3 unused string resources.
- Removal of 3 `.bak` debris files.
- Improved code navigability by eliminating the misleading `legacy` components.

### Risks & Complications
Risk: Resource lookup via reflection could cause runtime crashes if strings are removed.
Mitigation: Verified no dynamic lookup of `poll_` strings exists in the codebase.

### Prevention Strategy
Enforce removal of legacy components as soon as their Compose equivalents are fully integrated and verified. Periodic scans for `legacy` packages should be performed.
