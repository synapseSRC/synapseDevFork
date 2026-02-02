# Tester's Journal - Testing Insights

## 2025-05-24: Testing Baseline Establishment

### Current State of Test Coverage
- **Overall Coverage:** 0% (Initial) -> Increased with unit tests for AuthViewModel and FollowListViewModel.
- **Domain Layer:** 0%
- **Data Layer:** 0%
- **Presentation Layer:** Improved with tests for critical ViewModels.

### Testing Frameworks and Libraries
- **JUnit:** 4.13.2
- **Mocking:** Mockito-Kotlin 5.2.1
- **Coroutine Testing:** kotlinx-coroutines-test 1.8.0
- **Android Testing:** Robolectric 4.11.1

### Test Organization
- Created `app/src/test/java/.../feature/auth/presentation/viewmodel/AuthViewModelTest.kt`
- Created `app/src/test/java/.../viewmodel/FollowListViewModelTest.kt` (Matches production package `com.synapse.social.studioasinc.viewmodel`)

## 2025-05-24: High-Value Testing Gap - AuthViewModel & FollowListViewModel

### Coverage Gap Identified
ViewModels were entirely untested, representing a risk for UI state management regressions. `AuthViewModel` is critical for security/onboarding, and `FollowListViewModel` is a representative example of data loading flows.

### Testing Approach
- Implemented unit tests using `StandardTestDispatcher` and `UnconfinedTestDispatcher`.
- Found that re-initializing ViewModels within `runTest` using `testScheduler` provides the most stable environment for testing state transitions.
- **Fixed Architectural Violations:** Identified that `AuthViewModel.kt` was accessing `SupabaseClient.client` directly in multiple places. Added `signInWithOAuth` to `AuthRepository` and updated `AuthViewModel` to use injected repository methods exclusively, ensuring better testability and adherence to Clean Architecture.

### Challenges Encountered
- **Polling Loops:** Complex flows in `AuthViewModel` (like email verification polling) are difficult to test with standard dispatchers and can lead to `OutOfMemoryError` in the sandbox environment if not carefully handled. Decided to focus on basic states first.
- **SharedFlow Timing:** Testing `SharedFlow` emissions for navigation events requires careful coroutine management.

### Impact
- Established a repeatable pattern for ViewModel testing.
- Verified initial state, validation, and error handling for Auth and Follower flows.
- Improved production code quality by enforcing dependency injection and removing direct framework client access.
