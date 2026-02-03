# Media Upload System Migration Guide

## Quick Reference

| Old API | New API | Notes |
|---------|---------|-------|
| `ImageUploader.uploadImage()` | `MediaFacade.uploadImage()` | Use facade for cleaner API |
| `MediaUploadManager.uploadMultipleMedia()` | `MediaUploadCoordinator.uploadMultipleMedia()` | Coordinator uses Facade internally |
| `MediaStorageService` (direct) | `MediaFacade` | Facade handles processing + storage |

## Step-by-Step Migration

### Example 1: Simple Image Upload
```kotlin
// ❌ OLD
ImageUploader.uploadImage(context, filePath, object : ImageUploader.UploadCallback {
    override fun onUploadComplete(imageUrl: String) { ... }
    override fun onUploadError(errorMessage: String) { ... }
})

// ✅ NEW (using Hilt injection)
@Inject lateinit var mediaFacade: MediaFacade

// Coroutine context
val result = mediaFacade.uploadImage(
    uri = Uri.fromFile(File(filePath)),
    type = MediaUploadType.POST
)
result.onSuccess { uploadResult ->
    val url = uploadResult.url
}.onFailure { error ->
    // Handle error
}
```

### Example 2: Multiple Media Upload
```kotlin
// ❌ OLD
MediaUploadManager.uploadMultipleMedia(context, mediaItems, onProgress, onComplete, onError)

// ✅ NEW
@Inject lateinit var coordinator: MediaUploadCoordinator
coordinator.uploadMultipleMedia(mediaItems, onProgress, onComplete, onError)
```
