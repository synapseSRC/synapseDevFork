# Keyboard Fix Attempts - PostDetailScreen

## Problem Statement
When the keyboard opens in PostDetailScreen, the entire screen (including TopAppBar) shifts upward, pushing the top bar off-screen. The goal is to keep the background fixed, top app bar pinned, and only move the bottom input bar above the keyboard.

---

## Attempt #1 - Initial Fix (Commit 95a2950)
**Date:** 2026-02-11 01:43:22

### What We Tried
```kotlin
Scaffold(
    // Removed: contentWindowInsets = WindowInsets.safeDrawing
    bottomBar = {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()  // Added this
        ) { ... }
    }
) { paddingValues ->
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
    ) {
        Box(modifier = Modifier.weight(1f)) { // Added weight
            // Content
        }
    }
}
```

### Changes Made
- ❌ Removed `contentWindowInsets = WindowInsets.safeDrawing` from Scaffold
- ✅ Added `.imePadding()` to bottom bar Column
- ✅ Wrapped content in Column with `weight(1f)` for dynamic resizing

### Result
- ✅ TopAppBar stayed fixed at top
- ✅ Content area resized correctly
- ✅ Bottom bar positioned above keyboard
- ❌ **BUT:** User reported "comment input floating above keyboard" issue

### Why It Didn't Work
The fix was actually **correct**, but was reverted due to a perceived "floating" issue that wasn't properly diagnosed.

---

## Attempt #2 - Revert (Commit 683227f)
**Date:** 2026-02-11 02:21:51

### What We Tried
```kotlin
Scaffold(
    bottomBar = {
        Column(
            modifier = Modifier.fillMaxWidth()
            // Removed: .imePadding()
        ) { ... }
    }
)
```

### Changes Made
- ❌ **Removed `.imePadding()` from bottom bar**

### Result
- ❌ TopAppBar gets pushed off-screen again
- ❌ Entire screen shifts upward with keyboard
- ❌ Back to the original problem

### Why It Didn't Work
Removing `.imePadding()` broke the fix. Without it, the Scaffold uses default behavior which shifts the entire layout upward to accommodate the keyboard.

---

## Attempt #3 - Debug Logging (Commits 3ef6b28, 430399e)
**Date:** 2026-02-11 02:xx:xx - 03:xx:xx

### What We Tried
Added comprehensive logging to understand the behavior:
- Keyboard state changes (IME visible, IME bottom insets)
- Scaffold padding values
- Top app bar position and height
- Bottom bar position and height
- Content column position and height

### Result
- ✅ Identified that scaffold padding never changes when keyboard opens
- ✅ Confirmed bottom bar Y position stays constant (doesn't move up)
- ✅ Confirmed top app bar is being pushed off-screen
- ✅ Logs revealed the entire screen is shifting upward

### Why It Didn't Work
This wasn't a fix attempt - just diagnostic work. But it confirmed the root cause: **Scaffold is consuming IME insets by default and shifting the entire layout**.

---

## Root Cause Analysis

### The Problem
When no `contentWindowInsets` is specified, Scaffold has **default inset handling** that:
1. Consumes all window insets (including IME/keyboard)
2. Shifts the entire Scaffold container upward to make room for keyboard
3. Pushes everything (including TopAppBar) off the top of the screen

### Why Previous Attempts Failed
- **Attempt #1:** Was actually correct, but reverted prematurely
- **Attempt #2:** Removed the critical `.imePadding()` modifier, breaking the fix
- **Missing piece:** Never explicitly set `contentWindowInsets = WindowInsets(0, 0, 0, 0)` to tell Scaffold to ignore ALL insets

---

## Attempt #4 - Scaffold Configuration (Commit 4b4a526)
**Date:** 2026-02-11 04:xx:xx

### What We Tried
```kotlin
Scaffold(
    contentWindowInsets = WindowInsets(0, 0, 0, 0),  // NEW: Explicitly ignore all insets
    modifier = Modifier.onGloballyPositioned { ... }, // NEW: Log root position
    bottomBar = {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()  // RESTORED: Move bottom bar above keyboard
        ) { ... }
    }
)
```

### Key Changes
1. ✅ **Added `contentWindowInsets = WindowInsets(0, 0, 0, 0)`** - Tells Scaffold to ignore all insets
2. ✅ **Re-added `.imePadding()` to bottom bar** - Makes only bottom bar respond to keyboard
3. ✅ **Added root Scaffold position logging** - Verifies screen stays fixed

### Result
- ✅ Logs showed Root Y position staying constant at 78.0
- ✅ Logs showed TopAppBar Y position staying constant at 78.0
- ✅ Logs showed Bottom bar moving up correctly (2196 → 1206)
- ❌ **BUT:** Visually, entire screen still shifted up and top bar went off-screen!

### Why It Didn't Work
The Scaffold configuration was correct, but the **Activity window itself** was still resizing. The `AndroidManifest.xml` had `android:windowSoftInputMode="adjustResize"` which told Android to resize the entire Activity window when keyboard appears, overriding our Compose-level fixes.

---

## Final Solution (Commit f788337)
**Date:** 2026-02-11 04:58:xx

### What We Did
**AndroidManifest.xml:**
```xml
<activity
    android:name=".feature.post.PostDetailActivity"
    android:windowSoftInputMode="adjustNothing"  <!-- Changed from adjustResize -->
    ...
/>
```

**PostDetailScreen.kt (from Attempt #4):**
```kotlin
Scaffold(
    contentWindowInsets = WindowInsets(0, 0, 0, 0),
    bottomBar = {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
        ) { ... }
    }
)
```

### Key Changes
1. ✅ **Changed `windowSoftInputMode` from `adjustResize` to `adjustNothing`** - Prevents Activity window from resizing
2. ✅ **Kept `contentWindowInsets = WindowInsets(0, 0, 0, 0)`** - Scaffold ignores insets
3. ✅ **Kept `.imePadding()` on bottom bar** - Only bottom bar responds to keyboard

### Why This Works
**Two-layer fix:**
- **Activity level:** `adjustNothing` prevents window from resizing/shifting
- **Compose level:** `contentWindowInsets(0,0,0,0)` + `.imePadding()` gives precise control

Result:
- Activity window stays fixed (no resize)
- Scaffold container stays fixed (ignores insets)
- TopAppBar stays pinned at top (no movement)
- Only bottom bar reacts to keyboard via `.imePadding()`
- Content area resizes naturally with `weight(1f)`

---

## Lessons Learned

1. **Don't revert without understanding the root cause**
   - Attempt #1 was correct but got reverted due to unclear issue

2. **Scaffold has default inset behavior**
   - Must explicitly set `contentWindowInsets = WindowInsets(0, 0, 0, 0)` to prevent automatic shifting

3. **Activity-level configuration matters**
   - `windowSoftInputMode` in AndroidManifest.xml controls window-level behavior
   - `adjustResize` causes Activity window to resize/shift (bad for our use case)
   - `adjustNothing` keeps Activity window fixed (required for Compose-level control)

4. **Two-layer keyboard handling**
   - **Activity layer:** Controls whether window resizes (`adjustNothing` = no resize)
   - **Compose layer:** Controls which composables react (`.imePadding()` on specific elements)

5. **Selective inset handling is key**
   - Activity: don't adjust (`adjustNothing`)
   - Scaffold: ignore all insets (`contentWindowInsets = WindowInsets(0,0,0,0)`)
   - Bottom bar: apply `.imePadding()` (moves above keyboard)
   - Content: use `weight(1f)` (resizes dynamically)

6. **Logging can be misleading**
   - Logs showed correct behavior at Compose level
   - Visual issue was at Activity level (higher in the stack)
   - Always test visually, not just logs

---

## Testing Checklist

When keyboard opens, verify:
- [ ] Root Scaffold Y position stays constant (check logs)
- [ ] TopAppBar Y position stays constant (check logs)
- [ ] Bottom bar Y position decreases (moves up, check logs)
- [ ] Content column height decreases (shrinks to fit)
- [ ] TopAppBar remains visible on screen
- [ ] Bottom input bar sits directly above keyboard
- [ ] Reply/edit indicators move with bottom bar
- [ ] Content is scrollable when compressed
