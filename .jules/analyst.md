# Analyst's Journal - Quality Insights

## 2025-05-15 - Quality Baseline Established
**Static Analysis:** Android Lint is configured but not yet run to completion.
**Quality Metrics:** 32 instances of '!!', 20 '@Suppress', 26 'TODO's in 'app' module.
**Common Issues:** ViewModels and Repositories frequently use manual instantiation and static singleton access (SupabaseClient) instead of Hilt Dependency Injection.
**Standards:** Hilt for DI, KMP for shared logic, Compose for UI.
**Technical Debt:** Legacy repositories and ViewModels violating DI principles, hindering testability and maintainability.

## 2025-05-15 - Inconsistent DI Implementation
**Issue Pattern:** ViewModels (e.g., PostDetailViewModel) and Repositories (e.g., CommentRepository) manually instantiate dependencies and access static singletons.
**Root Cause:** Incomplete migration to Hilt or legacy code patterns being followed in new features.
**Impact:** Poor testability (hard to mock dependencies), violation of Clean Architecture, and inconsistency with other Hilt-enabled parts of the app.
**Resolution:** Refactor Post Detail feature to fully use Hilt for all dependencies.
**Prevention:** Enforce Hilt usage in code reviews; provide templates for Hilt-enabled ViewModels and Repositories.
