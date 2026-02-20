import re

file_path = 'app/src/main/java/com/synapse/social/studioasinc/ui/settings/BusinessPlatformScreen.kt'

with open(file_path, 'r') as f:
    content = f.read()

# Restore dividers in ProfessionalToolsSection
# Find the pattern of three SettingsNavigationItem calls
# We want to insert SettingsDivider() between them.

# Pattern 1: Between Top and Middle
content = content.replace(
    'position = SettingsItemPosition.Top\n            )\n            SettingsNavigationItem',
    'position = SettingsItemPosition.Top\n            )\n            SettingsDivider()\n            SettingsNavigationItem'
)

# Pattern 2: Between Middle and Bottom
content = content.replace(
    'position = SettingsItemPosition.Middle\n            )\n            SettingsNavigationItem',
    'position = SettingsItemPosition.Middle\n            )\n            SettingsDivider()\n            SettingsNavigationItem'
)

with open(file_path, 'w') as f:
    f.write(content)

print("Dividers restored successfully.")
