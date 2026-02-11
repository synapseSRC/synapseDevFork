# 🐛 Keyboard UI Bug Fix - PostDetailScreen

## Problem
When clicking the comment input field, the keyboard would push the **entire screen** (including the TopAppBar) upwards and off-screen, making the top bar invisible.

## Root Cause
The issue was caused by improper window inset handling in the edge-to-edge layout:
- `contentWindowInsets = WindowInsets.safeDrawing` was forcing the Scaffold to treat all content as part of the resizable area
- The content area lacked a `weight` modifier, preventing proper height recalculation when the keyboard appeared
- The bottom bar (CommentInput) wasn't using IME padding to position itself above the keyboard

## Solution Applied

### Changes Made to `PostDetailScreen.kt`:

1. **Removed `contentWindowInsets` from Scaffold**
   - Allows Scaffold to handle insets automatically with edge-to-edge behavior
   
2. **Added `imePadding()` to bottom bar Column**
   - Positions the CommentInput directly above the keyboard
   - Ensures proper spacing when IME (keyboard) is visible

3. **Wrapped content in Column with `weight(1f)`**
   - The main content Box now has `Modifier.weight(1f)` 
   - Allows the content area to resize dynamically when keyboard appears
   - Prevents the TopAppBar from being pushed off-screen

## Result
✅ **TopAppBar remains fixed** at the top when keyboard opens  
✅ **Content area (CommentsList) resizes** to fit remaining space  
✅ **CommentInput sits directly above** the keyboard  
✅ **Edge-to-edge design maintained** with proper inset handling  

## Technical Details
- **Manifest setting**: `android:windowSoftInputMode="adjustResize"` (already configured)
- **Activity**: `enableEdgeToEdge()` (maintained)
- **Layout approach**: Column with weighted content + IME padding on bottom bar

## Reference
Based on solution from: https://stackoverflow.com/questions/74742358/stop-the-keyboard-pushing-the-top-app-bar-off-the-screen-in-compose
