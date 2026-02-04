import os
import re

# Load symbol renames
renames = []
with open('symbol_renames.txt', 'r') as f:
    for line in f:
        line = line.strip()
        if '|' in line:
            renames.append(line.split('|'))

# Sort renames by length of old name descending
renames.sort(key=lambda x: len(x[0]), reverse=True)

# 1. Update package declarations
with open('package_mismatches.txt', 'r') as f:
    for line in f:
        line = line.strip()
        if not line: continue
        filepath, old_pkg, new_pkg = line.split('|')
        with open(filepath, 'r') as kf:
            content = kf.read()
        content = re.sub(r'^package ' + re.escape(old_pkg) + r'\b', f'package {new_pkg}', content, flags=re.MULTILINE)
        with open(filepath, 'w') as kf:
            kf.write(content)

# 2. Update references in all files
def migrate_file(filepath):
    with open(filepath, 'r') as f:
        content = f.read()

    original_content = content
    for old_full, new_full in renames:
        # Replace imports
        content = content.replace(f'import {old_full}', f'import {new_full}')
        # Replace fully qualified names
        pattern = re.compile(r'(?<![a-zA-Z0-9_.])' + re.escape(old_full) + r'(?![a-zA-Z0-9_.])')
        content = pattern.sub(new_full, content)

    if content != original_content:
        with open(filepath, 'w') as f:
            f.write(content)

for root, dirs, files in os.walk('app/src/main/java'):
    for file in files:
        if file.endswith('.kt'):
            migrate_file(os.path.join(root, file))

if os.path.exists('app/src/main/AndroidManifest.xml'):
    migrate_file('app/src/main/AndroidManifest.xml')

# 3. Manual fixes
# Fix Box import in NotificationsScreen.kt
notif_screen = 'app/src/main/java/com/synapse/social/studioasinc/ui/notifications/NotificationsScreen.kt'
if os.path.exists(notif_screen):
    with open(notif_screen, 'r') as f:
        content = f.read()
    content = content.replace('import androidx.compose.runtime.Box', 'import androidx.compose.foundation.layout.Box')
    with open(notif_screen, 'w') as f:
        f.write(content)


# 4. Replace star imports and general package references
package_renames = []
with open('package_mismatches.txt', 'r') as f:
    for line in f:
        line = line.strip()
        if not line: continue
        filepath, old_pkg, new_pkg = line.split('|')
        package_renames.append((old_pkg, new_pkg))

# Sort by length descending
package_renames = sorted(list(set(package_renames)), key=lambda x: len(x[0]), reverse=True)

def migrate_packages(filepath):
    with open(filepath, 'r') as f:
        content = f.read()

    original_content = content
    for old_pkg, new_pkg in package_renames:
        # Replace star imports
        content = content.replace(f'import {old_pkg}.*', f'import {new_pkg}.*')
        # Replace any other occurrences of the package name (e.g. in fully qualified names not caught before)
        # But be careful not to replace parts of other packages.
        # Use regex to match the package as a word boundary
        pattern = re.compile(r'(?<![a-zA-Z0-9_.])' + re.escape(old_pkg) + r'(?![a-zA-Z0-9_])')
        content = pattern.sub(new_pkg, content)

    if content != original_content:
        with open(filepath, 'w') as f:
            f.write(content)

for root, dirs, files in os.walk('app/src/main/java'):
    for file in files:
        if file.endswith('.kt'):
            migrate_packages(os.path.join(root, file))

if os.path.exists('app/src/main/AndroidManifest.xml'):
    migrate_packages('app/src/main/AndroidManifest.xml')
