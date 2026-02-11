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
**Status:** ❌ **DID NOT WORK**

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

### Result
- ❌ **User reported: Still doesn't fix the issue**
- ❌ Screen still shifts upward when keyboard opens
- ❌ Top bar still goes off-screen

### Why It Didn't Work
`adjustNothing` prevents window adjustment entirely, which may cause keyboard to cover content. The disconnect between log data (showing correct behavior) and visual observation (showing incorrect behavior) suggests the issue may be at a different layer or the Activity window is being manipulated by something else.

---

## Attempt #5 - Edge-to-Edge Inset Handling (Commit 97a508b)
**Date:** 2026-02-11 05:19:05
**Status:** ❌ **DID NOT WORK**

### Discovery
Found that `PostDetailActivity` uses `enableEdgeToEdge()` which requires different inset handling approach.

### What We Tried
**AndroidManifest.xml:**
```xml
<activity
    android:name=".feature.post.PostDetailActivity"
    android:windowSoftInputMode="adjustNothing"  <!-- Kept from previous attempt -->
    ...
/>
```

**PostDetailScreen.kt:**
```kotlin
Scaffold(
    contentWindowInsets = WindowInsets(0, 0, 0, 0),
    topBar = {
        TopAppBar(
            modifier = Modifier
                .statusBarsPadding()  // NEW: Prevent overlap with status bar
                .onGloballyPositioned { ... }
        )
    },
    bottomBar = {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()  // NEW: Handle navigation bar
                .imePadding()
        ) { ... }
    }
)
```

### Key Changes
1. ✅ **Added `.statusBarsPadding()` to TopAppBar** - Prevents overlap with status bar in edge-to-edge mode
2. ✅ **Added `.navigationBarsPadding()` to bottom bar** - Handles navigation bar insets
3. ✅ **Kept `.imePadding()` on bottom bar** - Handles keyboard insets
4. ✅ **Kept `contentWindowInsets = WindowInsets(0,0,0,0)`** - Manual inset control
5. ✅ **Kept `windowSoftInputMode="adjustNothing"`** - Full manual control

### Result
- ❌ **User reported: Still doesn't fix the issue**
- ❌ Screen still shifts upward when keyboard opens
- ❌ Top bar still goes off-screen

### Why It Didn't Work
Even with proper edge-to-edge inset handling, the issue persists. The combination of `enableEdgeToEdge()` + `adjustNothing` + manual inset padding should work in theory, but something is still causing the entire screen to shift.

---

## Current Status

**Attempt #7 in progress** - Testing double padding fix.

**Latest changes:**
- **Commit 8d22ada:** Fixed Activity lifecycle order
- **Commit 53721d0:** Changed back to `adjustResize` 
- **Commit 8846338:** Removed `.imePadding()` to fix double padding issue

**Discovery:** Bottom bar was appearing way too high (near status bar) because of double padding:
- `adjustResize` shrinks window by keyboard height
- `.imePadding()` adds ANOTHER keyboard height offset
- Result: 2× keyboard height = bottom bar near top

**Awaiting user testing to confirm if this fixes the issue.**

---

## Attempt #7 - Fix Double Padding (Commits 8d22ada, 53721d0, 8846338)
**Date:** 2026-02-11 06:06:xx
**Status:** ⏳ **TESTING**

### Discovery
User reported: "bottom bar appears too much higher instead of over keyboard. The bottom bar is appearing approximately at the top (near status bar)"

This revealed **double padding**:
1. `adjustResize` shrinks the Activity window by keyboard height
2. `.imePadding()` adds padding equal to keyboard height
3. **Result:** Bottom bar pushed up by 2× keyboard height → appears near status bar!

### What We Tried
**PostDetailActivity.kt (Commit 8d22ada):**
```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)  // ✅ FIRST
    WindowCompat.setDecorFitsSystemWindows(window, false)  // ✅ AFTER super
    ...
}
```

**AndroidManifest.xml (Commit 53721d0):**
```xml
<activity
    android:name=".feature.post.PostDetailActivity"
    android:windowSoftInputMode="adjustResize"  <!-- Changed back from adjustNothing -->
    ...
/>
```

**PostDetailScreen.kt (Commit 8846338):**
```kotlin
Scaffold(
    contentWindowInsets = WindowInsets(0, 0, 0, 0),
    topBar = {
        TopAppBar(
            modifier = Modifier.statusBarsPadding()
        )
    },
    bottomBar = {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                // REMOVED: .imePadding()  ← This was causing double padding!
        ) { ... }
    }
)
```

### Key Changes
1. ✅ **Fixed Activity lifecycle** - `super.onCreate()` first, then `WindowCompat`
2. ✅ **Used `adjustResize`** - Let window resize naturally for keyboard
3. ✅ **Removed `.imePadding()`** - Prevent double padding since `adjustResize` already handles it
4. ✅ **Kept `contentWindowInsets = WindowInsets(0,0,0,0)`** - Manual control over insets
5. ✅ **Kept `.statusBarsPadding()` and `.navigationBarsPadding()`** - Handle system bars only

### Why This Should Work
- `adjustResize` shrinks the window when keyboard opens
- Window shrinks from bottom, so TopAppBar stays at top
- Scaffold content area automatically fits in smaller window
- Bottom bar sits at bottom of resized window (above keyboard)
- No extra padding needed since window already resized

### Result
⏳ **Awaiting user testing**

---

## Attempt #6 - Fix Activity Lifecycle Order (Commit 8d22ada)
**Date:** 2026-02-11 05:44:xx
**Status:** ❌ **FAILED - Led to Attempt #7**

### Discovery
Found that `enableEdgeToEdge()` was being called BEFORE `super.onCreate()` in PostDetailActivity:
```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    enableEdgeToEdge()  // ❌ WRONG ORDER
    super.onCreate(savedInstanceState)
    ...
}
```

This is incorrect and breaks window inset handling. The Activity window setup happens in `super.onCreate()`, so calling `enableEdgeToEdge()` before it causes the window to be configured incorrectly.

### What We Tried
**PostDetailActivity.kt:**
```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)  // ✅ FIRST
    WindowCompat.setDecorFitsSystemWindows(window, false)  // ✅ AFTER super
    ...
}
```

### Key Changes
1. ✅ **Moved `super.onCreate()` to be FIRST line** - Proper Activity initialization order
2. ✅ **Replaced `enableEdgeToEdge()` with `WindowCompat.setDecorFitsSystemWindows(window, false)`** - More explicit control
3. ✅ **Called WindowCompat AFTER `super.onCreate()`** - Window is properly initialized before configuration

### Why This Should Work
- `super.onCreate()` initializes the Activity window
- `WindowCompat.setDecorFitsSystemWindows(window, false)` tells the window to NOT automatically fit system windows (status bar, nav bar, keyboard)
- This gives Compose full control over insets via the existing `.statusBarsPadding()`, `.navigationBarsPadding()`, and `.imePadding()` modifiers
- Combined with `windowSoftInputMode="adjustNothing"` and `contentWindowInsets = WindowInsets(0,0,0,0)`, we have complete manual control

### Result
⏳ **Awaiting user testing**

**Note:** This attempt combined with `adjustNothing` failed. The lifecycle fix was correct but incomplete - it revealed the double padding issue that led to Attempt #7.

---

## All Attempts Have Failed (Previous Status)

**All previous attempts failed.** The issue persisted despite:
- Configuring Scaffold to ignore insets
- Adding manual inset padding to top and bottom bars
- Trying different `windowSoftInputMode` values (adjustResize, adjustNothing, adjustPan)
- Accounting for edge-to-edge mode with proper status/navigation bar padding

**Possible remaining causes:**
1. Theme-level `windowSoftInputMode` override
2. Parent activity or navigation component interfering
3. System UI controller or window insets controller configuration
4. Edge-to-edge implementation conflicting with keyboard handling
5. Something in the Activity lifecycle manipulating window insets

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
