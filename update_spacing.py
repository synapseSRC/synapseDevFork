import re

file_path = 'app/src/main/java/com/synapse/social/studioasinc/ui/settings/BusinessPlatformScreen.kt'

with open(file_path, 'r') as f:
    content = f.read()

# 1. Update LazyColumn padding
content = content.replace(
    '.padding(horizontal = 16.dp)',
    '.padding(SettingsSpacing.screenPadding)'
)

# 2. Update AccountTypeSection padding
# The original code:
# modifier = Modifier.fillMaxWidth().padding(horizontal = SettingsSpacing.itemHorizontalPadding)
# The parent Column has padding(vertical = 8.dp) which we should probably keep or adjust if itemPadding handles it.
# The reviewer said: "consider using the unified SettingsSpacing.itemPadding token... to ensure this manual Row matches..."
# SettingsToggleItem typically uses itemPadding.
# I will replace padding(horizontal = ...) with padding(SettingsSpacing.itemPadding)
# And I should probably remove the parent column padding if I do that, or check if it doubles up.
# Parent column: modifier = Modifier.padding(vertical = 8.dp)
# If I use itemPadding (16h, 12v), then 8v + 12v = 20v which is too much.
# So I should remove padding(vertical = 8.dp) from the parent Column if I add itemPadding to children.
# However, the Button also needs padding.
# Let's apply itemPadding to the Row and the Button, and remove padding from the Column.

content = content.replace(
    'modifier = Modifier.padding(vertical = 8.dp)',
    'modifier = Modifier'
)

content = content.replace(
    'modifier = Modifier.fillMaxWidth().padding(horizontal = SettingsSpacing.itemHorizontalPadding)',
    'modifier = Modifier.fillMaxWidth().padding(SettingsSpacing.itemPadding)'
)

# 3. AnalyticsDashboardSection
# Remove spacedBy(8.dp)
content = content.replace(
    'verticalArrangement = Arrangement.spacedBy(8.dp)',
    'verticalArrangement = Arrangement.Top'
)
# Add padding(vertical = 8.dp) to the Row inside the loop
content = re.sub(
    r'(analytics\.topPosts\.forEachIndexed \{ index, post ->\s+Row\()',
    r'\1\n                        modifier = Modifier.padding(vertical = 8.dp),',
    content
)
# Wait, the Row already has modifier = Modifier.fillMaxWidth()...
# I need to be careful with regex replacement.
# The existing Row modifier is:
# modifier = Modifier.fillMaxWidth(),
# I should change it to:
# modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),

content = re.sub(
    r'(analytics\.topPosts\.forEachIndexed \{ index, post ->\s+Row\(\s+modifier = Modifier\.fillMaxWidth\(\)),',
    r'\1.padding(vertical = 8.dp),',
    content
)


# 4. MonetizationSection
# Remove spacedBy(16.dp)
content = content.replace(
    'Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {',
    'Column {'
)
# Add Spacer(Modifier.height(16.dp)) after SettingsToggleItem
content = content.replace(
    'onCheckedChange = onToggleMonetization\n            )\n\n            if',
    'onCheckedChange = onToggleMonetization\n            )\n            Spacer(modifier = Modifier.height(16.dp))\n\n            if'
)
# Add Spacer(Modifier.height(16.dp)) after SettingsDivider
# Actually, the divider is followed by a Row. The Row has padding.
# Reviewer said: "This creates a large 16dp gap around the divider".
# So if I remove spacedBy, I just have the Divider.
# I should probably add a small spacer if needed, but SettingsDivider usually has some space?
# SettingsDivider is just HorizontalDivider usually.
# Let's add Spacer(16.dp) between items, but maybe let the Divider be?
# If I have: Toggle -> Spacer(16) -> Divider -> Row -> NavigationItem
# Wait, the Divider was: if (...) { Divider(); Row... }
# If I remove spacedBy, the Divider sits right under the Spacer(16) I added?
# No, Toggle -> Spacer(16) -> if block -> Divider.
# So effectively Toggle -> 16dp -> Divider.
# Then Divider -> Row. The Row has padding? No, I used itemHorizontalPadding.
# I should add vertical padding to the Row? Or a spacer.
# Let's add Spacer(16.dp) after the Row.
content = content.replace(
    'tint = MaterialTheme.colorScheme.tertiary\n                    )\n                }',
    'tint = MaterialTheme.colorScheme.tertiary\n                    )\n                }\n                Spacer(modifier = Modifier.height(16.dp))'
)


# 5. ProfessionalToolsSection
# Update SettingsNavigationItem positions
# First item
content = content.replace(
    'imageVector = Icons.Default.Schedule,\n                onClick = { }\n            )',
    'imageVector = Icons.Default.Schedule,\n                onClick = { },\n                position = SettingsItemPosition.Top\n            )'
)
# Middle item
content = content.replace(
    'imageVector = Icons.Default.CalendarToday,\n                onClick = { }\n            )',
    'imageVector = Icons.Default.CalendarToday,\n                onClick = { },\n                position = SettingsItemPosition.Middle\n            )'
)
# Last item
content = content.replace(
    'imageVector = Icons.Default.Work,\n                onClick = { }\n            )',
    'imageVector = Icons.Default.Work,\n                onClick = { },\n                position = SettingsItemPosition.Bottom\n            )'
)
# Remove SettingsDivider from ProfessionalToolsSection as they are now joined cards?
# The review said: "Using the position tokens will ensure the items join together...".
# If they join, we usually don't want a full width divider between them, but maybe a hairline?
# SettingsNavigationItem might handle its own divider logic if grouped, or we need to keep them but remove spacing?
# Standard approach for joined items often implies no external divider composable is needed between them if the item itself draws a border, OR a divider is needed but without padding.
# Since I can't check SettingsNavigationItem source easily, I will trust that if I use positions, they look like a group.
# I will comment out or remove SettingsDivider calls in ProfessionalToolsSection for now, assuming joined items shouldn't have large dividers between them.
content = content.replace(
    'SettingsDivider()\n            SettingsNavigationItem',
    'SettingsNavigationItem'
)


# 6. VerificationSection
# Update vertical padding
content = content.replace(
    'vertical = 8.dp',
    'vertical = SettingsSpacing.itemVerticalPadding'
)

with open(file_path, 'w') as f:
    f.write(content)

print("File updated successfully.")
