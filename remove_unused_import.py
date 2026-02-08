import os

file_path = 'app/src/main/java/com/synapse/social/studioasinc/ui/settings/SettingsComponents.kt'
import_line = 'import androidx.compose.ui.text.font.FontWeight'

with open(file_path, 'r') as f:
    lines = f.readlines()

new_lines = []
found = False

for line in lines:
    if line.strip() == import_line:
        found = True
        continue
    new_lines.append(line)

if found:
    with open(file_path, 'w') as f:
        f.writelines(new_lines)
    print(f"Removed {import_line} from {file_path}")
else:
    print(f"{import_line} not found in {file_path}")
