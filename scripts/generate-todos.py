#!/usr/bin/env python3
import re
import os
import json
from pathlib import Path
from collections import defaultdict

PROJECT_ROOT = Path(__file__).parent.parent
APP_PATH = PROJECT_ROOT / "app"
SHARED_PATH = PROJECT_ROOT / "shared"
TODO_DIR = PROJECT_ROOT / "TODO"

FEATURE_MAP = {
    "feature/stories": ("stories", "🎬"),
    "feature/search": ("search", "🔍"),
    "feature/post": ("posts", "📝"),
    "feature/profile": ("profile", "👤"),
    "feature/createpost": ("create-post", "✍️"),
    "ui/settings": ("settings", "⚙️"),
    "core/di": ("core", "🔧"),
    "shared/data/auth": ("authentication", "🔐"),
    "data/repository": ("repositories", "💾"),
}

def get_feature_category(file_path):
    for pattern, (category, emoji) in FEATURE_MAP.items():
        if pattern in file_path:
            return category, emoji
    return "misc", "📌"

def extract_context(file_path, line_num, context_lines=5):
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            lines = f.readlines()
            start = max(0, line_num - context_lines - 1)
            end = min(len(lines), line_num + context_lines)
            return ''.join(lines[start:end]), lines[line_num - 1] if line_num <= len(lines) else ""
    except:
        return "", ""

def scan_todos():
    todos = []
    patterns = ["TODO", "FIXME", "XXX"]
    
    for base_path in [APP_PATH, SHARED_PATH]:
        if not base_path.exists():
            continue
            
        for root, dirs, files in os.walk(base_path):
            dirs[:] = [d for d in dirs if d not in ['.git', 'build', '.gradle']]
            
            for file in files:
                if not file.endswith(('.kt', '.java', '.py')):
                    continue
                    
                file_path = Path(root) / file
                try:
                    with open(file_path, 'r', encoding='utf-8') as f:
                        for line_num, line in enumerate(f, 1):
                            for pattern in patterns:
                                if pattern in line and ('//' in line or '/*' in line):
                                    rel_path = str(file_path.relative_to(PROJECT_ROOT))
                                    context, todo_line = extract_context(file_path, line_num)
                                    
                                    # Match both // TODO and /* TODO */ patterns
                                    comment_match = re.search(r'(?://|/\*)\s*(TODO|FIXME|XXX):?\s*(.+?)(?:\s*\*/)?', line)
                                    if comment_match and comment_match.group(2):
                                        todo_text = comment_match.group(2).strip()
                                        # Clean up trailing */ or }) {
                                        todo_text = re.sub(r'\s*\*/.*$', '', todo_text)
                                        todo_text = re.sub(r'\s*\}\).*$', '', todo_text)
                                    else:
                                        # Extract from comment in the line
                                        if '//' in line:
                                            parts = line.split('//', 1)
                                            if len(parts) > 1:
                                                todo_text = parts[1].replace('TODO', '').replace('FIXME', '').replace('XXX', '').replace(':', '').strip()
                                        else:
                                            todo_text = ""
                                    
                                    # Skip if it's just a placeholder with no description
                                    if not todo_text or len(todo_text) < 3 or todo_text in ['', '*/', '*', 'TODO', 'FIXME', 'XXX', '}) {']:
                                        # Try to infer from context - look at comments above
                                        context_lower = context.lower()
                                        if 'drawing tool' in context_lower:
                                            todo_text = "Implement drawing tool for story creation"
                                        elif 'sticker' in context_lower:
                                            todo_text = "Implement stickers picker for stories"
                                        elif 'qr' in context_lower and 'scan' in context_lower:
                                            todo_text = "Implement QR code scanner"
                                        elif 'edit post' in context_lower:
                                            todo_text = "Implement edit post navigation"
                                        elif 'share' in context_lower and 'message' in context_lower:
                                            todo_text = "Implement share via message functionality"
                                        elif 'open link' in context_lower or ('link' in context_lower and 'account' in context_lower):
                                            todo_text = "Implement open linked account URL"
                                        elif 'upgrade' in context_lower and 'flow' in context_lower:
                                            todo_text = "Implement Synapse Plus upgrade flow"
                                        elif 'crop' in context_lower:
                                            todo_text = "Implement explicit Save/Discard/Cancel controls for crop screen"
                                        else:
                                            todo_text = "Implement functionality"
                                    
                                    category, emoji = get_feature_category(rel_path)
                                    
                                    todos.append({
                                        'file': rel_path,
                                        'line': line_num,
                                        'text': todo_text,
                                        'context': context,
                                        'category': category,
                                        'emoji': emoji,
                                        'type': pattern
                                    })
                                    break
                except:
                    continue
    
    return todos

def generate_ai_prompt(todo):
    file_name = Path(todo['file']).name
    return f"""**🤖 AI Agent Prompt:**

You are tasked with implementing the following feature in the Synapse social media app.

**Context:** This TODO is located in `{file_name}` which is part of the {todo['category']} module.

**Task:** {todo['text']}

**Implementation Guidelines:**
- Review the surrounding code context below
- Follow Kotlin/Android best practices
- Maintain consistency with existing code patterns
- Ensure proper error handling
- Add appropriate comments

**Acceptance Criteria:**
- [ ] Feature is fully implemented and functional
- [ ] Code follows project conventions
- [ ] No compilation errors
- [ ] Tested manually (if UI component)

**Code Context:**
```kotlin
{todo['context'].rstrip()}
```

**Location:** `{todo['file']}:{todo['line']}`
"""

def generate_markdown(category, todos, emoji):
    content = f"# {emoji} {category.title()} - TODO List\n\n"
    content += f"**Total TODOs:** {len(todos)}\n\n"
    content += "---\n\n"
    
    for idx, todo in enumerate(todos, 1):
        type_emoji = "🐛" if todo['type'] == "FIXME" else "⚠️" if todo['type'] == "XXX" else "📋"
        content += f"## {type_emoji} TODO #{idx}: {todo['text'][:80]}\n\n"
        content += generate_ai_prompt(todo)
        content += "\n\n---\n\n"
    
    return content

def generate_index(categories):
    content = "# 📚 TODO Index\n\n"
    content += "This directory contains organized TODO items from the Synapse codebase.\n\n"
    content += "## 📊 Summary\n\n"
    
    total = sum(count for _, count in categories.items())
    content += f"**Total TODOs:** {total}\n\n"
    content += "## 📁 Categories\n\n"
    
    for category, count in sorted(categories.items()):
        emoji = next((e for p, (c, e) in FEATURE_MAP.items() if c == category), "📌")
        content += f"- [{emoji} {category.title()}](./{category}.md) - {count} items\n"
    
    content += "\n## 🚀 Usage\n\n"
    content += "Each TODO file contains:\n"
    content += "- Clear task descriptions\n"
    content += "- Code context and location\n"
    content += "- AI agent prompts for implementation\n"
    content += "- Acceptance criteria\n\n"
    content += "To regenerate these files, run:\n"
    content += "```bash\n./scripts/generate-todos.sh\n```\n"
    
    return content

def main():
    print("🔍 Scanning for TODOs...")
    todos = scan_todos()
    print(f"✅ Found {len(todos)} TODOs")
    
    categorized = defaultdict(list)
    for todo in todos:
        categorized[todo['category']].append(todo)
    
    TODO_DIR.mkdir(exist_ok=True)
    
    print("\n📝 Generating markdown files...")
    for category, items in categorized.items():
        emoji = items[0]['emoji']
        content = generate_markdown(category, items, emoji)
        output_file = TODO_DIR / f"{category}.md"
        output_file.write_text(content, encoding='utf-8')
        print(f"  ✓ {category}.md ({len(items)} items)")
    
    index_content = generate_index({cat: len(items) for cat, items in categorized.items()})
    (TODO_DIR / "README.md").write_text(index_content, encoding='utf-8')
    print(f"  ✓ README.md (index)")
    
    print(f"\n✨ Done! Generated {len(categorized)} TODO files in TODO/")

if __name__ == "__main__":
    main()
