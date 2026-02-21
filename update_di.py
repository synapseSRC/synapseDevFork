import re

file_path = "app/src/main/java/com/synapse/social/studioasinc/core/di/RepositoryModule.kt"

with open(file_path, "r") as f:
    content = f.read()

# Pattern matching the providePostRepository function
pattern = r"fun providePostRepository\(\s*postDao: PostDao,\s*client: SupabaseClientType\s*\): PostRepository \{\s*return PostRepository\(postDao, client\)\s*\}"

replacement = """fun providePostRepository(
        postDao: PostDao,
        client: SupabaseClientType,
        prefs: SharedPreferences
    ): PostRepository {
        return PostRepository(postDao, client, prefs)
    }"""

# Use regex to replace
new_content, count = re.subn(pattern, replacement, content)

if count == 0:
    print("Failed to replace providePostRepository. Pattern not found.")
    # Debug: Print a snippet where it should be
    snippet = content.find("providePostRepository")
    if snippet != -1:
        print("Found nearby content:\n" + content[snippet:snippet+200])
    exit(1)

with open(file_path, "w") as f:
    f.write(new_content)

print("Successfully updated RepositoryModule.kt")
