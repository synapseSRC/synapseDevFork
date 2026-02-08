import sys

file_path = 'app/src/main/java/com/synapse/social/studioasinc/ui/settings/SettingsComponents.kt'

lines_to_remove = [
    '    // Debug logging',
    '    android.util.Log.d("ProfileHeaderCard", "Rendering profile card - avatarUrl: $avatarUrl, displayName: $displayName")',
    '                    android.util.Log.d("ProfileHeaderCard", "Loading image from URL: $avatarUrl")'
]

with open(file_path, 'r') as f:
    lines = f.readlines()

new_lines = []
skip_next_empty = False

for i, line in enumerate(lines):
    content = line.rstrip('\n')

    should_remove = False
    if content in lines_to_remove:
        should_remove = True
        # If this is the specific log line, trigger skip for next empty line to avoid double blank lines
        if 'Rendering profile card' in content:
            skip_next_empty = True

    if should_remove:
        continue

    if skip_next_empty:
        if line.strip() == '':
            skip_next_empty = False # Skipped one empty line, done.
            continue
        else:
            skip_next_empty = False # Next line wasn't empty, don't skip it, and reset flag.

    new_lines.append(line)

with open(file_path, 'w') as f:
    f.writelines(new_lines)

print(f"Processed {file_path}")
