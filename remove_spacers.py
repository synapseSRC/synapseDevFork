import re

file_path = 'app/src/main/java/com/synapse/social/studioasinc/ui/settings/BusinessPlatformScreen.kt'

with open(file_path, 'r') as f:
    content = f.read()

# Remove Spacer(modifier = Modifier.height(16.dp)) in MonetizationSection
content = content.replace(
    'Spacer(modifier = Modifier.height(16.dp))\n',
    ''
)

with open(file_path, 'w') as f:
    f.write(content)

print("Spacers removed successfully.")
