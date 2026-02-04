# Janitor's Journal - Cleanup Insights

## [2025-05-22] - Cleanup Baseline Establishment

### Unused Resource Statistics
- **Drawables**: `drawable/button_follow_background.xml` (referenced only by legacy component).
- **Layouts**: `layout/component_follow_button.xml` (referenced only by legacy component).
- **Strings**: Several strings in `strings.xml` appear to have no references in code, likely from removed features or half-implemented ones.

### Deprecated API Usage
- `InboxActivity` is marked as `@Deprecated` in favor of `InboxScreen` in `MainActivity`.
- `ProfileActivity` is marked as `@Deprecated` in favor of `ProfileScreen` in `MainActivity`.
- `NotificationHelper` contains several deprecated methods shifted to Edge Functions.

### Dead Code Identification
- `app/src/main/java/com/synapse/social/studioasinc/feature/shared/components/legacy/FollowButton.kt`: View-based component, no usages in modern Compose UI.
- `app/src/main/java/com/synapse/social/studioasinc/feature/shared/components/legacy/PollDisplay.kt`: Compose component, superseded by `PollContent.kt` in `.../post/` package. No usages found.
- `app/src/main/java/com/synapse/social/studioasinc/feature/shared/components/UserActivity.kt`: No usages found in the codebase.
- `app/src/main/java/com/synapse/social/studioasinc/feature/shared/components/PostOptionsMenu.kt`: No usages found; `PostMenuBottomSheet` or `PostOptionsBottomSheet` are used instead.

### Commented-out Code Blocks
- `HomeActivity.kt`: Presence management TODO/commented logic.
- `InboxActivity.kt`: Chat navigation TODOs.
- `ProfileActivity.kt`: Chat functionality commented out.
- `AppNavigation.kt`: Several chat-related navigation routes are commented out.

### Application Size Metrics
- Baseline established before removal of legacy components and resources.
- Estimated impact: Minor reduction in DEX size and resource overhead.

---

## [2025-05-22] - Removal of Legacy View Components

### Pattern of Accumulation
Legacy View-based components (`FollowButton`) and early Compose prototypes (`PollDisplay`) were left in a `legacy` package after being replaced by more optimized or modern versions in the `feature/shared/components/post` package. These components often kept their associated XML layouts and drawables, which contributed to resource clutter.

- **Mismatched Package Declarations**: The removed files `FollowButton.kt` and `PollDisplay.kt` had a package declaration of `com.synapse.social.studioasinc.components`, which didn't match their file system location in the `.../feature/shared/components/legacy/` directory. This indicates a pattern where legacy code often diverges from established project structure.

### Discovery Method
- Manual scan of `legacy` packages.
- Grep-based usage analysis to confirm zero references in the current production UI.
- Lint (partial) confirming unused resources.

### Impact of Cleanup
- Removed 2 Kotlin files.
- Removed 1 XML layout file.
- Removed 1 XML drawable file.
- Reduced code navigability friction by removing "false lead" components that developers might accidentally use instead of the modern ones.

### Prevention Strategy
- Implement a "Migration Complete" checklist that requires deleting the legacy implementation once a feature has been fully moved to Compose or a newer architecture.
- Periodic cleanup of the `legacy` packages should be scheduled after major feature refactors.
- **Automated Checks**: Integrate static analysis tools into the CI pipeline to proactively detect unused code.
    - Configure stricter **Android Lint** rules (e.g., `UnusedResources`, `UnusedIds`) to fail the build or issue high-priority warnings.
    - Consider using dedicated tools like **ucd** (Unused Code Detector) to identify public components and functions with no active call sites.
    - Monitor deprecation warnings in CI to ensure older patterns are phased out rather than lingering indefinitely.
