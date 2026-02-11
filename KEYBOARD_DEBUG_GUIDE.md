# Keyboard Bug Debug Guide

## How to Collect Logs

1. **Enable Logcat filtering**:
   ```bash
   adb logcat -s PostDetailScreen:D
   ```

2. **Test scenario**:
   - Open PostDetailScreen
   - Tap on comment input to open keyboard
   - Observe the behavior
   - Close keyboard
   - Repeat 2-3 times

## What the Logs Show

### 1. KEYBOARD STATE CHANGED
```
IME Visible: true/false          # Is keyboard visible?
IME Bottom: <pixels>             # Keyboard height in pixels
SafeDrawing Bottom: <pixels>     # System safe area bottom inset
```

**What to look for**:
- When keyboard opens, `IME Visible` should be `true`
- `IME Bottom` should show keyboard height (typically 800-1200px)
- If `IME Bottom` is 0 when keyboard is visible, the system isn't detecting the keyboard

### 2. TOP APP BAR
```
Y Position: <pixels>             # Distance from screen top
Height: <pixels>                 # AppBar height
```

**What to look for**:
- Y Position should stay at 0 or small positive value (status bar height)
- If Y Position goes negative when keyboard opens, the AppBar is being pushed off-screen
- Height should remain constant (~56-64dp = 168-192px)

### 3. BOTTOM BAR POSITION
```
Y Position: <pixels>             # Distance from screen top
Height: <pixels>                 # Bottom bar height
Screen Height: <pixels>          # Total screen height
```

**What to look for**:
- When keyboard is closed: Y Position should be near screen bottom
- When keyboard opens: Y Position should decrease by keyboard height
- If Y Position stays the same when keyboard opens, the bottom bar isn't moving up

### 4. SCAFFOLD PADDING
```
Top: <dp>                        # Padding from top (for TopAppBar)
Bottom: <dp>                     # Padding from bottom (for bottomBar)
```

**What to look for**:
- Top padding should match TopAppBar height
- Bottom padding should match bottom bar height
- These values tell us how much space Scaffold is reserving

### 5. CONTENT COLUMN
```
Y Position: <pixels>             # Where content starts
Height: <pixels>                 # Available content height
```

**What to look for**:
- Y Position should be below TopAppBar
- Height should shrink when keyboard opens (to fit between AppBar and bottom bar)
- If Height doesn't change when keyboard opens, content isn't resizing

## Expected Behavior

### Keyboard Closed
```
TOP APP BAR: Y=0, Height=168
CONTENT COLUMN: Y=168, Height=1800 (example)
BOTTOM BAR: Y=1968, Height=150
```

### Keyboard Open (800px keyboard)
```
TOP APP BAR: Y=0, Height=168          # STAYS AT TOP
CONTENT COLUMN: Y=168, Height=1000    # SHRINKS
BOTTOM BAR: Y=1168, Height=150        # MOVES UP by 800px
```

### Bug Symptoms

**TopAppBar pushed off-screen**:
```
TOP APP BAR: Y=-168 or negative       # ❌ BAD
```

**Bottom bar floating above keyboard**:
```
BOTTOM BAR: Y=500                     # ❌ Too high
IME Bottom: 800                       # Keyboard at 800px from bottom
```

**Content not resizing**:
```
CONTENT COLUMN: Height=1800           # ❌ Same height with keyboard open
```

## Common Issues & Solutions

| Symptom | Likely Cause | Solution |
|---------|--------------|----------|
| TopAppBar Y goes negative | Entire screen being pushed up | Remove `contentWindowInsets` from Scaffold |
| Bottom bar too high | Extra padding being added | Remove `imePadding()` modifier |
| Content not scrollable | Missing `weight(1f)` | Add weight modifier to content Box |
| Keyboard overlaps input | `adjustPan` instead of `adjustResize` | Change windowSoftInputMode in manifest |

## Share These Logs

When reporting the issue, please share:
1. Full logcat output from opening/closing keyboard
2. Device model and Android version
3. Screen resolution
4. Whether the issue happens on all devices or specific ones

Copy logs with:
```bash
adb logcat -s PostDetailScreen:D > keyboard_logs.txt
```
