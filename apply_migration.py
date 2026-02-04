import os
import re

# Load symbol renames
symbol_renames = []
with open('symbol_renames.txt', 'r') as f:
    for line in f:
        line = line.strip()
        if '|' in line:
            symbol_renames.append(line.split('|'))

# Load package renames (derived from package_mismatches)
package_renames = []
with open('package_mismatches.txt', 'r') as f:
    for line in f:
        line = line.strip()
        if '|' in line:
            parts = line.split('|')
            package_renames.append((parts[1], parts[2]))
package_renames = sorted(list(set(package_renames)), key=lambda x: len(x[0]), reverse=True)

# 1. Update package declarations in mismatched files
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

# 2. Update references in all project files
def migrate_file(filepath):
    with open(filepath, 'r') as f:
        content = f.read()

    original_content = content

    # Replace explicit symbol imports and FQNs
    for old_full, new_full in symbol_renames:
        content = content.replace(f'import {old_full}', f'import {new_full}')
        pattern = re.compile(r'(?<![a-zA-Z0-9_.])' + re.escape(old_full) + r'(?![a-zA-Z0-9_])')
        content = pattern.sub(new_full, content)

    # Replace star imports and residual package references
    for old_pkg, new_pkg in package_renames:
        content = content.replace(f'import {old_pkg}.*', f'import {new_pkg}.*')
        pattern = re.compile(r'(?<![a-zA-Z0-9_.])' + re.escape(old_pkg) + r'(?![a-zA-Z0-9_])')
        content = pattern.sub(new_pkg, content)

    if content != original_content:
        with open(filepath, 'w') as f:
            f.write(content)

for root, dirs, files in os.walk('app/src/main/java'):
    for file in files:
        if file.endswith('.kt'):
            migrate_file(os.path.join(root, file))

if os.path.exists('app/src/main/AndroidManifest.xml'):
    migrate_file('app/src/main/AndroidManifest.xml')
