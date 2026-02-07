import re

file_path = 'app/src/main/java/com/synapse/social/studioasinc/core/di/RepositoryModule.kt'

with open(file_path, 'r') as f:
    content = f.read()

# Collect imports
new_imports = [
    'com.synapse.social.studioasinc.shared.domain.usecase.GetStorageConfigUseCase',
    'com.synapse.social.studioasinc.shared.domain.usecase.UpdateStorageProviderUseCase',
    'com.synapse.social.studioasinc.shared.domain.usecase.UploadMediaUseCase',
    'com.synapse.social.studioasinc.shared.data.database.StorageDatabase',
    'com.synapse.social.studioasinc.shared.data.repository.StorageRepositoryImpl'
]

# Insert new imports if not present
for imp in new_imports:
    if f'import {imp}' not in content:
        last_import_idx = content.rfind('import ')
        line_end_idx = content.find('\n', last_import_idx)
        content = content[:line_end_idx+1] + f'import {imp}\n' + content[line_end_idx+1:]

# Replace fully qualified names with simple names
replacements = {
    'com.synapse.social.studioasinc.shared.domain.usecase.GetStorageConfigUseCase': 'GetStorageConfigUseCase',
    'com.synapse.social.studioasinc.shared.domain.usecase.UpdateStorageProviderUseCase': 'UpdateStorageProviderUseCase',
    'com.synapse.social.studioasinc.shared.domain.usecase.UploadMediaUseCase': 'UploadMediaUseCase',
    'com.synapse.social.studioasinc.shared.data.database.StorageDatabase': 'StorageDatabase',
    'com.synapse.social.studioasinc.shared.data.repository.StorageRepositoryImpl': 'StorageRepositoryImpl',
    'com.synapse.social.studioasinc.shared.domain.repository.StorageRepository': 'StorageRepository'
}

for full, simple in replacements.items():
    content = content.replace(full, simple)

with open(file_path, 'w') as f:
    f.write(content)

print("Successfully cleaned up RepositoryModule.kt")
