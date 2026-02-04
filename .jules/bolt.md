# ⚡ Bolt's Performance Journal

## 2025-05-24 - [Notifications Performance Audit]
**Learning:** Even simple lists like Notifications can suffer from O(N) operations and unnecessary recompositions. Surgical updates of StateFlow lists using `indexOfFirst` and `toMutableList` combined with surgical counter updates (like `unreadCount`) avoid redundant work. Memoizing lambdas passed to list items is crucial for skippability.
**Action:** Always check ViewModels for `map` updates in large lists and ensure lambdas in `LazyColumn` items are referentially stable.
