lines = open('app/src/main/java/com/synapse/social/studioasinc/feature/shared/navigation/AppNavigation.kt').readlines()

new_lines = []
skip = False
for line in lines:
    if "composable(AppDestination.Inbox.route)" in line:
        new_lines.append(line)
        new_lines.append("            InboxScreen(\n")
        new_lines.append("                onNavigateToProfile = { userId ->\n")
        new_lines.append("                    navController.navigate(AppDestination.Profile.createRoute(userId))\n")
        new_lines.append("                }\n")
        new_lines.append("            )\n")
        new_lines.append("        }\n")
        skip = True
    elif skip:
        if "composable(AppDestination.Search.route)" in line:
            new_lines.append("\n") # Add spacing
            new_lines.append(line)
            skip = False
        else:
            continue
    else:
        new_lines.append(line)

open('app/src/main/java/com/synapse/social/studioasinc/feature/shared/navigation/AppNavigation.kt', 'w').writelines(new_lines)
