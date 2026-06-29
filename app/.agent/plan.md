# Project Plan

IntelliDash: Multimodal AI Productivity Hub with Context Auditor and Media Indexer. Currently has a searchable gallery and AI-generated metadata.

## Project Brief

# IntelliDash: Project Brief

## Features
- **Multimodal AI Indexer**: Automatically generates metadata and extracts context from images and media using multimodal AI models.
- **Semantic Search Gallery**: A high-performance searchable interface that allows users to find media using natural language queries based on AI-indexed content.
- **Smart AI Collections**: Intelligent categorization that automatically groups media into folders (e.g., Receipts, Travel, Work, Food) based on AI-suggested labels, mirroring a "Pixel Screenshots" style experience.
- **Adaptive Productivity Hub**: A responsive dashboard providing a high-level overview of indexed context and quick access to smart collections, optimized for all form factors.

## High-Level Technical Stack
- **Kotlin**: The core programming language for modern Android development.
- **Jetpack Compose**: Declarative UI framework for building the app's components.
- **Jetpack Navigation 3**: State-driven navigation architecture to manage app flows and deep linking.
- **Compose Material Adaptive**: Core library for implementing adaptive layouts (e.g., List-Detail, Supporting Pane) that scale across phones, foldables, and tablets.
- **Kotlin Coroutines**: For handling asynchronous AI processing and background indexing tasks.

## Implementation Steps
**Total Duration:** 3h 45m 47s

### Task_1_SetupConfig: Configure the project for Android 16 (compileSdk 36) and add dependencies for AppFunctions (1.0.0-alpha09), AICore, and Compose Material Adaptive libraries.
- **Status:** COMPLETED
- **Updates:** Configured build.gradle.kts and libs.versions.toml for Android 16 (SDK 36). Added AppFunctions 1.0.0-alpha09 dependencies and KSP. Verified Compose Adaptive and Navigation dependencies are present. Project builds successfully.
- **Acceptance Criteria:**
  - compileSdk is set to 36
  - AppFunctions 1.0.0-alpha09 dependency is added
  - Compose Adaptive and Navigation 3 dependencies are added
  - Project builds successfully
- **Duration:** 20m 58s

### Task_2_AICoreClient: Implement the AICore client for local Gemini Nano inference, supporting the Preview track and structured JSON output for AI code audits.
- **Status:** COMPLETED
- **Updates:** Fixed AICore Error 200 by adding structured output support.
- **Acceptance Criteria:**
  - AICore client initialized on supported device
  - JSON output schema defined and supported
  - 100% local inference verified
- **Duration:** 1h 45m 18s

### Task_3_AppFunctionAudit: Implement the 'auditCodeSnippet' AppFunction with KDoc schema generation and integrate it with injection routing and registration.
- **Status:** COMPLETED
- **Updates:** Implemented AuditFunctions.kt with @AppFunction annotated auditCodeSnippet.
- **Acceptance Criteria:**
  - 'auditCodeSnippet' function implemented
  - KDoc schema generated successfully
  - AppFunction registered and discoverable by system
- **Duration:** 27m 13s

### Task_4_ComposeUI: Implement the Dynamic Code Workbench, AI Health Dashboard, and Adaptive Multi-Pane Layout using Jetpack Compose and Navigation 3.
- **Status:** COMPLETED
- **Updates:** Fixed Phone UI accessibility using NavigationSuiteScaffold and improved state handling.
- **Acceptance Criteria:**
  - Code Workbench allows code input and shows audit results
  - AI Health Dashboard displays model status
  - UI adapts to different screen sizes using Material Adaptive
  - App navigation handled by Navigation 3
- **Duration:** 10m 50s

### Task_5_RunAndVerify: Run the application and perform final verification. Instruct critic_agent to verify application stability (no crashes), confirm alignment with user requirements, and report critical UI issues.
- **Status:** COMPLETED
- **Updates:** Final verification complete.
- **Acceptance Criteria:**
  - App does not crash
  - Local inference and AppFunctions work correctly
  - Build pass
  - All existing tests pass
- **Duration:** 6m 23s

### Task_6_MediaIndexerCore: Implement Media Indexer backend: Root-based file scanning for DCIM/Screenshots, Room DB with FTS5 search, and MediaPipe multimodal tagging (Gemma-3n) with WorkManager background processing.
- **Status:** COMPLETED
- **Updates:** Implemented Media Indexer backend including Room FTS5 and MediaPipe pipeline.
- **Acceptance Criteria:**
  - Root scanning of restricted storage successful
  - Room DB with FTS5 indexing implemented
  - MediaPipe Gemma-3n inference working with OOM protection (batch size 1)
  - WorkManager handles background indexing during charging/idle
- **Duration:** 41m 7s

### Task_7_MediaGalleryUI: Implement the Home Hub for navigation and the Searchable Media Gallery UI. Perform final system-wide verification for stability and performance.
- **Status:** COMPLETED
- **Updates:** Final system-wide implementation and verification complete for gallery.
- **Acceptance Criteria:**
  - Home Hub allows navigation to Context Auditor and Media Indexer
  - Searchable gallery displays media with FTS-based search
  - App does not crash
  - Build pass
  - All existing tests pass
- **Duration:** 13m 58s

### Task_8_SmartCollectionsImpl: Implement Smart Collections: Update Room DB and FTS for 'collectionName', update the AI prompt to extract categorization metadata, and build the adaptive Collection Browser UI tab.
- **Status:** IN_PROGRESS
- **Acceptance Criteria:**
  - Room DB schema supports collectionName
  - AI prompt returns collectionName in JSON output
  - Collection Browser UI allows viewing media grouped by AI categories
  - UI is adaptive across phone and tablet form factors
- **StartTime:** 2026-06-28 20:49:19 EDT

### Task_9_RunAndVerifyCollections: Run the application and perform final verification of the Smart Collections feature. Instruct critic_agent to verify application stability (no crashes), confirm alignment with user requirements, and report critical UI issues.
- **Status:** PENDING
- **Acceptance Criteria:**
  - Smart Collections correctly group media
  - App does not crash during AI categorization or browsing
  - Build pass
  - All existing tests pass

