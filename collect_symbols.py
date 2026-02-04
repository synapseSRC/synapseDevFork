import os
import re

symbols = []

with open('package_mismatches.txt', 'r') as f:
    for line in f:
        line = line.strip()
        if not line: continue
        filepath, old_pkg, new_pkg = line.split('|')

        with open(filepath, 'r') as kf:
            content = kf.read()
            regex = r'^(?:(?:private|internal|public|protected|open|sealed|data|enum|abstract)\s+)*(?:class|object|interface|fun|val|const val|typealias)\s+([a-zA-Z0-9_]+)'
            matches = re.findall(regex, content, re.MULTILINE)

            for name in set(matches):
                if name in ['Companion', 'it', 'args', 'state']: continue
                symbols.append((f"{old_pkg}.{name}", f"{new_pkg}.{name}"))

# Also handle top-level functions and properties
with open('symbol_renames.txt', 'w') as sf:
    for old, new in sorted(list(set(symbols)), key=lambda x: len(x[0]), reverse=True):
        sf.write(f"{old}|{new}\n")
