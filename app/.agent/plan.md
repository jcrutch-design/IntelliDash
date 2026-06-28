# Project Plan

Local-first coding assistant app using on-device Gemini Nano (AICore) and AppFunctions to expose capabilities to the system. Features include Android 16 SDK 36 support, AppFunctions alpha09, and structured JSON output for code audits.

## Project Brief

I have created a project brief for IntelliDash, focusing on a local-first MVP that leverages on-device AI and system-level integrations.

### Features
1. **On-Device Code Audit Engine**: Uses Gemini Nano (AICore) to analyze code snippets for complexity and vulnerabilities locally, ensuring privacy and speed.
2. **Dynamic Code Workbench**: A modern interface for inputting code and viewing structured audit results (complexity, vulnerabilities, refactored code) side-by-side.
3. **System-Aware AppFunctions**: Exposes auditing capabilities to the Android system, enabling voice commands and shortcuts via AppFunctions (alpha09).
4. **AI Health Dashboard**: A real-time status indicator showing the availability and performance of the on-device AI model.
5. **Adaptive Multi-Pane Layout**: A responsive UI built with Compose Material Adaptive that optimizes the experience across phones, foldables, and tablets.

### High-Level Technical Stack
*   **Language**: Kotlin
*   **UI Framework**: Jetpack Compose (Material 3)
*   **Navigation**: Jetpack Navigation 3 (State-driven)
*   **Adaptive Strategy**: Compose Material Adaptive Library
*   **AI Engine**: Google AI Edge SDK (Gemini Nano / AICore)
*   **System Integration**: Android 16 (SDK 36), AppFunctions (alpha09)
*   **Concurrency**: Kotlin Coroutines & Flow

*The UI Design Image section was omitted as the generation tool is unavailable.*

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
- **Updates:** Fixed AICore Error 200 by: [details omitted]
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
- **Updates:** Implemented Media Indexer backend:
- **Acceptance Criteria:**
  - Root scanning of restricted storage successful
  - Room DB with FTS5 indexing implemented
  - MediaPipe Gemma-3n inference working with OOM protection (batch size 1)
  - WorkManager handles background indexing during charging/idle
- **Duration:** 41m 7s

### Task_7_MediaGalleryUI: Implement the Home Hub for navigation and the Searchable Media Gallery UI. Perform final system-wide verification for stability and performance.
- **Status:** COMPLETED
- **Updates:** Final system-wide implementation and verification complete:
- Home Hub: Entry point for both Context Auditor and Media Indexer.
- Media Indexer UI: Dashboard with progress/status, FTS-based searchable gallery, and detail view with AI metadata.
- Navigation: Jetpack Navigation 3 implementation with adaptive layout support.
- Stability: Verified no crashes during root scanning, AI inference, or background work.
- Design: Consistent premium Cyber theme applied app-wide.
- **Acceptance Criteria:**
  - Home Hub allows navigation to Context Auditor and Media Indexer
  - Searchable gallery displays media with FTS-based search
  - App does not crash
  - Build pass
  - All existing tests pass
- **Duration:** 13m 58s

