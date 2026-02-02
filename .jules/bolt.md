# Bolt's Journal - Synapse Social

## 2025-05-14 - Initial Exploration
**Learning:** The project is a KMP hybrid with Native Android UI (Compose) and a shared logic module. Supabase is the backend.
**Action:** Focus on Android-specific performance optimizations in the `app` module, specifically targeting Compose UI or memory efficiency.

## 2025-05-15 - Optimizing Post List and Date Parsing
**Learning:** Even if a Composable's child is @Stable, the child will recompose if the lambdas passed to it are not remembered in the (potentially unstable) parent. Caching lambdas in `SharedPostItem` is critical for `PostCard` skippability. Additionally, `java.time` (API 26+) is significantly faster than `SimpleDateFormat` for list-heavy operations.
**Action:** Always memoize lambdas in list item wrappers and prefer `java.time` for performance-critical date formatting.
