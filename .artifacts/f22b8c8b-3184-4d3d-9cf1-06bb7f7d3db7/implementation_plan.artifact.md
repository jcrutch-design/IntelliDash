# Media Indexer Discovery Improvement Plan

The goal is to ensure users see their media immediately upon opening the Media Indexer screen, even before AI indexing is complete. We will expand the scan paths, decouple discovery from indexing constraints, and update the UI to show "Pending" items.

## Proposed Changes

### 1. Root Storage Helper [MODIFY]
#### [RootStorageHelper.kt](file:///C:/Users/jason/AndroidStudioProjects/IntelliDash/app/src/main/java/com/example/intellidash/utils/RootStorageHelper.kt)
- Expand `scanMediaFiles` to include `/sdcard/Pictures`, `/sdcard/Download`, and common social media media paths (WhatsApp, Telegram).
- Add support for `.webp` and `.jpeg` extensions (case-insensitive).

### 2. Media Database & DAO [MODIFY]
#### [MediaDao.kt](file:///C:/Users/jason/AndroidStudioProjects/IntelliDash/app/src/main/java/com/example/intellidash/data/MediaDao.kt)
- Change `getAll()` ordering to `ORDER BY id DESC` to show recently discovered items first, regardless of indexing status.

### 3. Background Workers [NEW / MODIFY]
#### [DiscoveryWorker.kt](file:///C:/Users/jason/AndroidStudioProjects/IntelliDash/app/src/main/java/com/example/intellidash/worker/DiscoveryWorker.kt) [NEW]
- Create a lightweight `DiscoveryWorker` that only performs the root file scan and inserts new paths into the database.
- This worker will have **NO constraints** (runs immediately).

#### [MediaIndexerWorker.kt](file:///C:/Users/jason/AndroidStudioProjects/IntelliDash/app/src/main/java/com/example/intellidash/worker/MediaIndexerWorker.kt) [MODIFY]
- Remove the discovery scan logic from `MediaIndexerWorker` to avoid redundancy, or keep it as a fallback.
- Ensure `MediaIndexerWorker` only focuses on AI indexing of items already in the database.

### 4. UI Layer [MODIFY]
#### [MediaIndexerViewModel.kt](file:///C:/Users/jason/AndroidStudioProjects/IntelliDash/app/src/main/java/com/example/intellidash/ui/media/MediaIndexerViewModel.kt)
- Add `runImmediateDiscovery()` function to enqueue `DiscoveryWorker`.
- Call `runImmediateDiscovery()` in `init`.

#### [MediaIndexerScreen.kt](file:///C:/Users/jason/AndroidStudioProjects/IntelliDash/app/src/main/java/com/example/intellidash/ui/media/MediaIndexerScreen.kt)
- Add a "REFRESH DISCOVERY" button to the Dashboard.
- Update `MediaThumbnail` to show a "PENDING" overlay or a blur effect for items with `summary == null`.

## Verification Plan

### Automated Tests
- N/A (Root commands are difficult to unit test without a rooted environment).

### Manual Verification
- Launch the app.
- Navigate to Media Indexer.
- Verify that "Total images" count increases immediately.
- Verify that unindexed images appear in the gallery with a "PENDING" status.
- Verify that images from `Pictures` and `Download` folders are now included.
