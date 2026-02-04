# 🧪 Tester's Journal - Testing Insights

## 📊 Testing Baseline - 2025-05-24

### Current State of Test Coverage
- **Total Unit Tests:** 2 (AuthViewModelTest, FollowListViewModelTest)
- **Coverage Percentage:**
    - **Presentation Layer (ViewModels):** Very low (< 5%)
    - **Domain Layer (UseCases):** 0% (Most logic is in shared module or ViewModels)
    - **Data Layer (Repositories):** 0%
- **Testing Frameworks:**
    - **JUnit 4**
    - **Mockito-Kotlin**
    - **kotlinx-coroutines-test**
    - **Robolectric**

### Test Organization
- Tests are located in `app/src/test/java` and mirror the production package structure.
- Naming convention: `ClassNameTest`.

### Identified Gaps
- **NotificationsViewModel:** Zero test coverage despite handling critical user data and optimistic updates.
- **Shared Module:** No tests found for repositories or business logic in `commonMain`.
- **Error Handling:** Existing tests mostly cover happy paths; error state transitions are under-tested.

### Initial Observations
- The project has a standard Android testing stack but it's drastically underutilized.
- ViewModel tests use `Robolectric` and `StandardTestDispatcher` with `advanceUntilIdle()`.
- No standardized `MainCoroutineRule` is being used in existing tests, leading to manual setup/teardown in each test class.

## 💡 Testing Insights

### NotificationsViewModel Testing (2025-05-24)
- **Coverage Gap:** `NotificationsViewModel` was completely untested despite containing mapping logic from `NotificationDto` (shared) to `UiNotification` (app) and implementing optimistic UI updates.
- **Approach:** Implemented `NotificationsViewModelTest` using `Robolectric` and a standardized `MainCoroutineRule`. Used `Mockito` to stub repository responses.
- **Insights:**
    - The ViewModel correctly handles unauthenticated states by exiting early when `authRepository.getCurrentUserId()` is null.
    - Optimistic updates for `markAsRead` were verified: the UI state updates immediately before the network call completes, providing a snappy user experience.
    - Revert logic was verified: on failure, the ViewModel re-fetches notifications from the repository to ensure consistency.
    - **Edge Case Identified:** The mapping logic `dto.body["en"]?.jsonPrimitive?.contentOrNull` assumes the presence of an "en" key. If missing, it fallbacks to "New notification". This pattern should be centralized or handled more robustly across the app.
- **Impact:** Increased confidence in notification flow and established a standardized pattern for coroutine testing with `MainCoroutineRule`.

### 🐞 Discovered Bug: UI Inconsistency on Double-Failure (2025-05-24)
- **Gap identified in Review:** A scenario where both the repository call (`markAsRead`) and the subsequent state recovery call (`loadNotifications`) fail.
- **Investigation:** In the original implementation, the ViewModel relied solely on `loadNotifications()` to revert the optimistic update. If `loadNotifications()` failed, the UI would remain in the optimistically updated state (marked as read), leading to inconsistency between the UI and the server state.
- **Fix implemented:** Added manual local rollback in the `catch` block of `markAsRead` before attempting the sync with `loadNotifications()`.
- **Verification:** Added `markAsRead failure followed by reload failure should handle gracefully` to `NotificationsViewModelTest` which asserts that the UI state is reverted even if the reload attempt fails.
