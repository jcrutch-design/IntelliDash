# Manual Collection Management & AI Integration Plan

Implement a way for users to create manual categories, add images to them, and have the AI prefer these categories during automatic indexing.

## User Review Required

> [!IMPORTANT]
> The AI integration relies on injecting user-defined collection names into the system prompt. If there are many collections (e.g., >50), this might slightly increase token usage or affect inference speed on device, though still well within Gemini Nano's capabilities.

## Proposed Changes

### Data Layer
Grouped files for database updates.

---

#### [NEW] [UserCollection.kt](file:///C:/Users/jason/AndroidStudioProjects/IntelliDash/app/src/main/java/com/example/intellidash/data/UserCollection.kt)
Define a new entity to store user-defined categories.
```kotlin
@Entity(tableName = "user_collections")
data class UserCollection(
    @PrimaryKey val name: String,
    val createdAt: Long = System.currentTimeMillis()
)
```

#### [NEW] [UserCollectionDao.kt](file:///C:/Users/jason/AndroidStudioProjects/IntelliDash/app/src/main/java/com/example/intellidash/data/UserCollectionDao.kt)
DAO for managing manual collections.
```kotlin
@Dao
interface UserCollectionDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(collection: UserCollection)

    @Query("SELECT * FROM user_collections ORDER BY name ASC")
    fun getAll(): Flow<List<UserCollection>>

    @Delete
    suspend fun delete(collection: UserCollection)
}
```

#### [MODIFY] [MediaDatabase.kt](file:///C:/Users/jason/AndroidStudioProjects/IntelliDash/app/src/main/java/com/example/intellidash/data/MediaDatabase.kt)
Register `UserCollection` and its DAO.

#### [MODIFY] [MediaDao.kt](file:///C:/Users/jason/AndroidStudioProjects/IntelliDash/app/src/main/java/com/example/intellidash/data/MediaDao.kt)
Add a method to update the collection of a specific media item.

---

### AI Integration
Update the local Gemini Nano prompt to be aware of user collections.

---

#### [MODIFY] [MultimodalIndexerClient.kt](file:///C:/Users/jason/AndroidStudioProjects/IntelliDash/app/src/main/java/com/example/intellidash/MultimodalIndexerClient.kt)
Modify `indexImage` to accept a list of `userCollections`. The prompt will be updated to say:
"If the image fits into any of these existing collections: [List], use that as `collectionName`. Otherwise, suggest a new one."

#### [MODIFY] [MediaIndexerWorker.kt](file:///C:/Users/jason/AndroidStudioProjects/IntelliDash/app/src/main/java/com/example/intellidash/worker/MediaIndexerWorker.kt)
Before starting the indexing loop, fetch all `UserCollection` names and pass them to the indexer client.

---

### UI Layer
Implement the screens and logic for collection management.

---

#### [MODIFY] [MediaIndexerViewModel.kt](file:///C:/Users/jason/AndroidStudioProjects/IntelliDash/app/src/main/java/com/example/intellidash/ui/media/MediaIndexerViewModel.kt)
Add logic for:
*   Exposing `userCollections` to the UI.
*   `createUserCollection(name: String)`
*   `assignToCollection(item: MediaItem, collectionName: String)`
*   `importImageToCollection(uri: Uri, collectionName: String)`: Copying external images to a managed directory and indexing them.

#### [MODIFY] [MediaIndexerScreen.kt](file:///C:/Users/jason/AndroidStudioProjects/IntelliDash/app/src/main/java/com/example/intellidash/ui/media/MediaIndexerScreen.kt)
*   **Collections Tab**: Add a "Create Collection" FAB.
*   **Media Detail**: Add an "Add to Collection" button that opens a selection dialog.
*   **Import**: Add a button to pick an image from the gallery.

---

## Verification Plan

### Automated Tests
- Unit tests for `UserCollectionDao`.
- Mock tests for `MultimodalIndexerClient` prompt generation.

### Manual Verification
1. Create a collection named "Work Trip".
2. Take a photo of a receipt (or use one in gallery).
3. Verify that the AI automatically suggests "Work Trip" as the collection name if it's relevant.
4. Manually assign a different photo to "Work Trip" and verify it appears in the collection view.
