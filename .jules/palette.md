## 2025-02-04 - Standardizing Social Action UX and Accessibility

**Learning:** Micro-UX enhancements like haptic feedback provide critical tactile confirmation for common social actions (Like, Send, Follow), making the interface feel more responsive and high-quality. Additionally, aligning package declarations with the physical directory structure is vital for large KMP-migrated projects to maintain symbol resolution reliability.

**Action:** Always include `LocalHapticFeedback` for primary action buttons and ensure `contentDescription` for icons utilizes dynamic string resources (e.g., including counts) to provide a rich experience for screen reader users. Strictly verify package-to-folder alignment when modifying legacy components.
