# 🐛 fix: Correct profile avatar positioning in edit profile screen

## 💡 What
Fixed the profile avatar appearing nested inside the cover photo in the Edit Profile screen. The avatar now properly overlaps the cover photo with correct positioning.

## 🎯 Why
The avatar was positioned using an offset of 152dp from the top of a dynamically-sized container, causing it to appear embedded within the cover photo (200dp height) rather than overlapping it properly. This created a poor visual hierarchy and didn't match the expected design pattern.

## 🔧 How
- Set the outer container to a fixed height of 248dp (200dp cover + 48dp avatar overlap)
- Changed avatar positioning from `offset(y = 152.dp)` to `align(Alignment.BottomCenter)`
- This creates a proper 50% overlap where half the avatar (48dp) sits on the cover and half extends below it

## 🧪 Tests
N/A - UI layout fix verified through build

## ♿ Accessibility
N/A - No accessibility impact

## 📁 Files Changed
- `app/src/main/java/com/synapse/social/studioasinc/feature/profile/editprofile/components/ProfileImageSection.kt`

## References
N/A
