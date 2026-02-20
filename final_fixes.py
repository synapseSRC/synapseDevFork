import re

file_path = 'app/src/main/java/com/synapse/social/studioasinc/ui/settings/BusinessPlatformScreen.kt'

with open(file_path, 'r') as f:
    content = f.read()

# 1. Remove redundant empty modifier in AccountTypeSection
content = content.replace(
    'Column(\n            modifier = Modifier.fillMaxWidth()\n        )',
    'Column'
)

# 2. Fix indentation in AccountTypeSection Button
content = content.replace(
    '                Button(\n                    onClick',
    '            Button(\n                onClick'
)
content = content.replace(
    '                    modifier = Modifier.fillMaxWidth().padding(SettingsSpacing.itemPadding)\n                ) {',
    '                modifier = Modifier.fillMaxWidth().padding(SettingsSpacing.itemPadding)\n            ) {'
)
content = content.replace(
    '                    Text("Switch to Business Account")\n                }',
    '                Text("Switch to Business Account")\n            }'
)

# 3. Restore spacedBy(8.dp) in AnalyticsDashboardSection
content = content.replace(
    'verticalArrangement = Arrangement.Top',
    'verticalArrangement = Arrangement.spacedBy(8.dp)'
)

# 4. Fix indentation in MonetizationSection Row
content = content.replace(
    '                Row(\n                    modifier = Modifier.fillMaxWidth().padding(horizontal = SettingsSpacing.itemHorizontalPadding),\n                    horizontalArrangement = Arrangement.SpaceBetween,\n                    verticalAlignment = Alignment.CenterVertically\n                ) {',
    '            Row(\n                modifier = Modifier.fillMaxWidth().padding(horizontal = SettingsSpacing.itemHorizontalPadding),\n                horizontalArrangement = Arrangement.SpaceBetween,\n                verticalAlignment = Alignment.CenterVertically\n            ) {'
)
content = content.replace(
    '                    Column {\n                        Text(\n                            text = "Total Earnings",\n                            style = MaterialTheme.typography.bodyMedium\n                        )\n                        Text(\n                            text = "4024{state.revenue.totalEarnings}",\n                            style = MaterialTheme.typography.headlineMedium,\n                            color = MaterialTheme.colorScheme.primary\n                        )\n                    }\n                    Icon(\n                        imageVector = Icons.Default.AttachMoney,\n                        contentDescription = null,\n                        modifier = Modifier.size(48.dp),\n                        tint = MaterialTheme.colorScheme.tertiary\n                    )\n                }',
    '                Column {\n                    Text(\n                        text = "Total Earnings",\n                        style = MaterialTheme.typography.bodyMedium\n                    )\n                    Text(\n                        text = "4024{state.revenue.totalEarnings}",\n                        style = MaterialTheme.typography.headlineMedium,\n                        color = MaterialTheme.colorScheme.primary\n                    )\n                }\n                Icon(\n                    imageVector = Icons.Default.AttachMoney,\n                    contentDescription = null,\n                    modifier = Modifier.size(48.dp),\n                    tint = MaterialTheme.colorScheme.tertiary\n                )'
)

# 5. Remove redundant SettingsDivider in ProfessionalToolsSection
content = content.replace(
    'SettingsDivider()\n            SettingsNavigationItem',
    'SettingsNavigationItem'
)

with open(file_path, 'w') as f:
    f.write(content)

print("Final fixes applied successfully.")
