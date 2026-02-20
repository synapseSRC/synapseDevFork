import re

file_path = 'app/src/main/java/com/synapse/social/studioasinc/ui/settings/BusinessPlatformScreen.kt'

with open(file_path, 'r') as f:
    content = f.read()

# 1. Fix duplicate modifier in AnalyticsDashboardSection
# Pattern matches the duplicate lines
content = re.sub(
    r'modifier = Modifier\.padding\(vertical = SettingsSpacing\.itemVerticalPadding\),\s+modifier = Modifier\.fillMaxWidth\(\),',
    'modifier = Modifier\n                        .fillMaxWidth()\n                        .padding(vertical = SettingsSpacing.itemVerticalPadding),',
    content
)

# 2. Add Spacer after SettingsDivider in MonetizationSection
content = content.replace(
    'SettingsDivider()\n\n                Row(',
    'SettingsDivider()\n                Spacer(modifier = Modifier.height(16.dp))\n                Row('
)

# 3. Clean up AccountTypeSection redundant modifier
content = content.replace(
    'Column(\n            modifier = Modifier\n        )',
    'Column(\n            modifier = Modifier.fillMaxWidth()\n        )'
)

with open(file_path, 'w') as f:
    f.write(content)

print("File updated successfully.")
