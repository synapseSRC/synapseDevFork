import re

file_path = 'app/src/main/java/com/synapse/social/studioasinc/ui/settings/BusinessPlatformScreen.kt'

with open(file_path, 'r') as f:
    content = f.read()

# Remove verticalArrangement = Arrangement.spacedBy(8.dp)
content = content.replace(
    '            Column(\n                verticalArrangement = Arrangement.spacedBy(8.dp)\n            ) {',
    '            Column {'
)

# Restore padding to itemVerticalPadding for list items, since we removed parent spacing
content = content.replace(
    'modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),',
    'modifier = Modifier.fillMaxWidth().padding(vertical = SettingsSpacing.itemVerticalPadding),'
)

with open(file_path, 'w') as f:
    f.write(content)

print("Analytics spacing fixed.")
