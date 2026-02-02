# 🎥 Reels & Oppose Mechanic

Reels are short-form video content in Synapse Social, featuring a unique "Oppose" interaction that encourages healthy debate and contrarian perspectives.

## 🏗️ Architecture

The Reels feature is implemented across the `shared` and `app` modules:

- **Logic (`:shared`)**:
    - `ReelRepository`: Handles fetching reels, uploading videos to Supabase Storage, and managing interactions.
    - `Reel`, `ReelInteraction`, `ReelComment`: Domain models.
- **UI (`:app`)**:
    - `ReelsScreen`: The main vertical pager for viewing reels.
    - `ReelItem`: Individual video player component with interaction buttons.
    - `ReelsViewModel`: Orchestrates state for the reels feed.
    - `VideoPlayerViewModel`: Manages Media3 player instances for each reel.

## 🔄 The "Oppose" Mechanic

Unlike traditional "dislike" buttons, the **Oppose** mechanic in Synapse is designed to signal a counter-argument or a different perspective.

### How it works:
1.  **Interaction**: Users can tap the "Oppose" (thumb down) icon.
2.  **Anonymity**: Opposition can be recorded anonymously, allowing users to express contrarian views without fear of social pressure.
3.  **Data Flow**:
    - Tapping "Oppose" sends an interaction type of `oppose` to the `reel_interactions` table in Supabase.
    - The `oppose_count` on the reel is incremented.
    - If `isAnonymous` is true, the user ID is still recorded in the interaction table for RLS and uniqueness, but may be hidden in public-facing "Opposer Logs".

## 📤 Uploading Reels

Reels are uploaded as follows:
1.  The video file is streamed via `ByteReadChannel`.
2.  `ReelRepository.uploadReel` uploads the file to the `reels` bucket in Supabase Storage.
3.  A public URL is generated.
4.  A record is inserted into the `reels` database table with metadata (caption, music, location).

## 🛠️ Key Components

### `ReelRepository`
The central hub for Reels data.
- `getReels(page, pageSize)`: Fetches the feed.
- `likeReel(reelId)` / `opposeReel(reelId)`: Toggles interactions.
- `uploadReel(...)`: Handles multi-step upload process.

### `ReelsViewModel`
Manages the feed state, including pagination and optimistic updates for likes/opposes.

### `VideoPlayerManager`
A utility in `:app` that manages a pool of Media3 `ExoPlayer` instances to ensure smooth scrolling and preloading in the `VerticalPager`.
