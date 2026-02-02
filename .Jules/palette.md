## 2025-05-14 - Improving Profile Screen Tactile Feedback and Accessibility

**Learning:** In Jetpack Compose, custom wrappers for icon buttons (like `AnimatedIconButton` in this app) can easily lose accessibility properties if not explicitly handled. Even if they accept a `contentDescription` parameter, it must be manually applied to the underlying `IconButton` or its content. Additionally, adding subtle haptic feedback using `HapticFeedbackType.TextHandleMove` provides a "flagship" feel to simple interactions without being intrusive.

**Action:** Always verify that `contentDescription` parameters are actually used in the semantics of interactive components. Standardize on `TextHandleMove` for light interactions and `LongPress` for high-impact actions in this design system. When ensuring minimum touch targets in Material 3, use `Modifier.minimumInteractiveComponentSize()` and ensure it's properly imported from `androidx.compose.material3`.
