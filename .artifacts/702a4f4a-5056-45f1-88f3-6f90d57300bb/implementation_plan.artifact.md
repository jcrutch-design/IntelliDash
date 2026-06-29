# Unify AI Clients to use AICore (Gemini Nano)

The goal is to migrate the `Media Indexer` from MediaPipe (local `.bin` model) to AICore (ML Kit GenAI Prompt API). This ensures both the `Context Auditor` and `Media Indexer` leverage the system-managed Gemini Nano model installed via the AICore Developer Preview.

## User Review Required

> [!IMPORTANT]
> This change removes the dependency on the local model file at `/data/local/tmp/models/gemma-3n.bin`. The app will now require Gemini Nano to be enabled and updated via Google Play Services on the device.

## Proposed Changes

### AI Clients

#### [MODIFY] [MultimodalIndexerClient.kt](file:///C:/Users/jason/AndroidStudioProjects/IntelliDash/app/src/main/java/com/example/intellidash/MultimodalIndexerClient.kt)
- Replace MediaPipe `LlmInference` with ML Kit `GenerativeModel`.
- Update initialization to use `ModelReleaseStage.PREVIEW`.
- Update `indexImage` to use `content { text(...); image(...) }` for multimodal input.
- Remove model file path checks.

#### [MODIFY] [AICoreClient.kt](file:///C:/Users/jason/AndroidStudioProjects/IntelliDash/app/src/main/java/com/example/intellidash/AICoreClient.kt)
- Ensure it uses `ModelPreference.FULL` for NPU acceleration as a primary option.

### Workers

#### [MODIFY] [MediaIndexerWorker.kt](file:///C:/Users/jason/AndroidStudioProjects/IntelliDash/app/src/main/java/com/example/intellidash/worker/MediaIndexerWorker.kt)
- Clean up exception handling related to the missing `.bin` file.

### UI

#### [MODIFY] [MediaIndexerScreen.kt](file:///C:/Users/jason/AndroidStudioProjects/IntelliDash/app/src/main/java/com/example/intellidash/ui/media/MediaIndexerScreen.kt)
- Add a "Powered by Gemini Nano (AICore)" label in the Dashboard.

## Verification Plan

### Automated Tests
- Run `gradlew :app:assembleDebug` to ensure compilation.

### Manual Verification
- Verify that the Media Indexer starts without checking for the local file.
- Observe logs for "AICore" and "GenerativeModel" activity during indexing.
