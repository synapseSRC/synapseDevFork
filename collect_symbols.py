import os
import re

symbols = [] # (old_full_name, new_full_name)

with open('package_mismatches.txt', 'r') as f:
    for line in f:
        line = line.strip()
        if not line: continue
        filepath, old_pkg, new_pkg = line.split('|')

        with open(filepath, 'r') as kf:
            content = kf.read()

            # Match top-level declarations
            # Regex for class, object, interface, fun, val, const val
            regex = r'^(?:(?:private|internal|public|protected|open|sealed|data|enum|abstract)\s+)*(?:class|object|interface|fun|val|const val|typealias)\s+([a-zA-Z0-9_]+)'
            matches = re.findall(regex, content, re.MULTILINE)

            for name in set(matches):
                if name in ['Companion', 'args', 'it', 'state']: continue
                symbols.append((f"{old_pkg}.{name}", f"{new_pkg}.{name}"))

with open('symbol_renames.txt', 'w') as sf:
    for old, new in sorted(list(set(symbols)), key=lambda x: len(x[0]), reverse=True):
        sf.write(f"{old}|{new}\n")
