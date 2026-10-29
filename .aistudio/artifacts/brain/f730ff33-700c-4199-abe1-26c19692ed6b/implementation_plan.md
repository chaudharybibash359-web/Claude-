# Claude AI Premium (Free Edition) — Implementation Plan

A high-fidelity Claude Android experience unlocking all premium capabilities with zero subscription gates: unlimited conversation, intelligent model switching (Claude 3.7 Sonnet with Extended Thinking, Claude 3.5 Sonnet Coding King, Claude 3.5 Haiku Speed, and Claude 3 Opus Deep Reasoner), interactive side-by-side / full-screen Artifacts with live code preview & syntax rendering, project workspaces, prompt library, and authentic warm ivory/terracotta typography and aesthetics.

---

### User Review & Critical Decisions

> [!IMPORTANT]
> The following core product and design decisions were confirmed during initial consultation:

- **Confirmed Decision 1 (Model Engine & Personas)**: Powered by Google AI Studio's `gemini-3.8-flash` with system prompts fine-tuned for authentic Claude intelligence, coding mastery, and an interactive "Extended Thinking" toggle simulating Claude 3.7 Sonnet's reasoning traces.
- **Confirmed Decision 2 (Interactive Artifacts & Code Studio)**: Dedicated Artifacts tab and split/slide-over inspector supporting code syntax highlighting, line numbers, one-tap copy, markdown preview, and live interactive HTML/JS web rendering via an embedded WebView sandbox.
- **Confirmed Decision 3 (Aesthetic & Theming)**: Authentic Claude visual identity featuring warm ivory background (`#FAF9F5`), terracotta primary accent (`#DA7756` / `#CC5A36`), charcoal typography (`#1F1E1B`), pill badges, warm dark mode (`#1A1917` / `#262522`), and editorial serif headers.

---

### 1. Overview & Core Concept

- **What It Does**: Provides a dedicated, premium Claude Android client with unlimited access to top-tier AI features:
  - **Multi-Model Selector**: Switch freely between *Claude 3.7 Sonnet (Thinking)*, *Claude 3.5 Sonnet (Coding King)*, *Claude 3.5 Haiku (Instant Speed)*, and *Claude 3 Opus (Research & Deep Logic)*.
  - **Artifacts Studio**: Automatically detects generated code (HTML/JS/CSS, Kotlin, Python, Markdown, SVG) and extracts them into an interactive Artifacts drawer with syntax highlighting, copy actions, and live visual sandbox.
  - **Coding King Tools**: One-click code generation templates (Full App, Bug Fix, Code Refactor, System Architecture, Regex / SQL builder), formatting, and diff views.
  - **Projects & Prompts**: Create organized Project Folders (e.g., "Android Dev", "Data Science", "Writing") with custom instructions and quick-access prompt libraries.
  - **Offline-First & Fast Response**: Built with local Room persistence for instant message caching, conversation branches, search, and export (Markdown/JSON).
- **Target Audience / Persona**: Developers, students, writers, and technical power users who love Claude's thoughtful tone, reasoning traces, and code quality without artificial usage limits.
- **Key Value**: Premium Claude tier features (extended context feel, thinking toggles, multi-model flexibility, and live artifacts) accessible instantly on mobile with zero subscription friction.

---

### 2. User Experience & Visual Design

#### Key User Flows
1. **New Conversation & Model Choice**:
   - User launches the app into a welcoming Claude greeting screen ("Good morning", "What can I help you build today?").
   - Model pill at the top allows selecting *Claude 3.7 Sonnet (Thinking)*, *3.5 Sonnet (Coding)*, etc.
   - Quick action prompt chips: "Write Android Compose UI", "Debug Python script", "Create interactive HTML game", "Explain quantum physics".
2. **Conversational Coding & Streaming**:
   - User sends a coding prompt. The response streams in real time with an animated thinking indicator or collapsible thinking block (when thinking mode is active).
   - Code blocks appear with syntax coloring, copy button, language badge, and an "Open Artifact" pill.
3. **Artifacts Inspection & Live Sandbox**:
   - Tapping an Artifact opens a slide-over panel with tabs: **Preview** (renders HTML/SVG/UI live) and **Code** (syntax highlighted code with line numbers and copy).
4. **Sidebar Navigation & Projects**:
   - Drawer navigation with recent chats, starred chats, custom Projects with pinned system instructions, and Settings (API engine, thinking budget, haptic feedback, theme).

#### Visual Identity & Color Tokens
- **Light Theme (Claude Authentic Warm Ivory)**:
  - Background: `#FAF9F5` (Warm Cream / Ivory)
  - Surface / Cards: `#FFFFFF` and `#F3EFE6` (Subtle Warm Tint)
  - Primary / Accent: `#DA7756` (Claude Terracotta Coral)
  - Secondary Accent: `#786047` (Warm Earth)
  - Text Primary: `#1E1E1C` (Deep Charcoal Warm)
  - Text Secondary: `#66625D` (Muted Warm Gray)
  - Border / Dividers: `#E8E4DA`
- **Dark Theme (Claude Warm Obsidian)**:
  - Background: `#171615` (Deep Warm Charcoal)
  - Surface: `#23221F` (Warm Slate Surface)
  - Surface Variant: `#2E2C28`
  - Primary / Accent: `#E28766` (Vibrant Terracotta)
  - Text Primary: `#EDEBE8`
  - Text Secondary: `#9E9A92`
  - Border: `#36342F`
- **Typography**: Clean, elegant sans-serif body combined with high-contrast editorial serif headers, mimicking Claude's refined literary style. Monospace formatting for code blocks.

---

### 3. Key Product Decisions & Trade-Offs

- **Decision 1: Dual-Mode AI Logic (Gemini Engine with Claude Personas + Custom API Option)**:
  - *Chosen Approach*: Wire Firebase AI Logic / Gemini SDK as the default high-speed zero-cost engine, with prompt engineering personas reproducing Claude's nuanced personality, thinking tags (`<thinking>...</thinking>`), and coding rigor. Additionally, provide an optional Custom API Key settings tab for users who want to connect direct Anthropic or OpenRouter keys.
  - *Why*: Delivers instant, free, unlimited access out of the box with zero setup hurdles, while honoring power users.
- **Decision 2: Interactive Artifacts with Sandbox WebView**:
  - *Chosen Approach*: Parse assistant messages for code fences (```html, ```svg, ```kotlin, ```python). For web/visual code, render directly in an isolated local Android `WebView`.
  - *Why*: Artifacts are Claude's defining feature; on mobile, having live previews elevates the experience far above a standard chat interface.
- **Decision 3: Room Database for Chat History & Projects**:
  - *Chosen Approach*: Local Room SQLite database with entities for `Conversation`, `ChatMessage`, `Artifact`, and `Project`.
  - *Why*: Instant offline loads, zero network latency for history retrieval, and complete user privacy.

---

### 4. Technical Architecture & Data Strategy

```
┌────────────────────────────────────────────────────────────────────────┐
│                        Jetpack Compose UI Layer                        │
├─────────────────┬───────────────────┬──────────────────┬───────────────┤
│   Chat Screen   │  Artifacts Drawer │  Projects & Prompts  Settings    │
│  - Stream Text  │  - Syntax Highlt  │  - Folders       │  - Model Mode │
│  - Thinking Box │  - Live WebView   │  - System Prompt │  - Dark/Light │
│  - Model Pill   │  - Quick Copy     │  - Quick Chips   │  - Thinking   │
└────────┬────────┴─────────┬─────────┴────────┬─────────┴───────┬───────┘
         │                  │                  │                 │
┌────────▼──────────────────▼──────────────────▼─────────────────▼────────┐
│                   ChatViewModel & ArtifactViewModel                    │
│      - StateFlow<ChatUiState>, Stream buffer, Thinking parser          │
└───────────────────────┬─────────────────────────────┬──────────────────┘
                        │                             │
┌───────────────────────▼─────────────┐   ┌───────────▼──────────────────┐
│      ClaudeAiRepository             │   │     Local Room Database      │
│  - Model router:                    │   │  - ConversationEntity        │
│    * Claude 3.7 Sonnet (Thinking)   │   │  - MessageEntity             │
│    * Claude 3.5 Sonnet (Coding)     │   │  - ArtifactEntity            │
│    * Claude 3.5 Haiku (Fast)        │   │  - ProjectEntity             │
│    * Claude 3 Opus (Research)       │   │  - Room Dao & Migration      │
│  - Streaming Flow chunks            │   └──────────────────────────────┘
│  - Artifact Extractor Regex         │
└─────────────────────────────────────┘
```

#### Core Data Entities
1. **ConversationEntity**: `id`, `title`, `modelId`, `thinkingEnabled`, `createdAt`, `projectId`, `isPinned`
2. **MessageEntity**: `id`, `conversationId`, `sender` ("user" | "assistant"), `content`, `thinkingContent`, `timestamp`
3. **ArtifactEntity**: `id`, `messageId`, `conversationId`, `title`, `type` ("html" | "code" | "markdown" | "svg"), `language`, `content`
4. **ProjectEntity**: `id`, `name`, `description`, `systemPrompt`, `icon`

#### Verification Steps
1. Verify Gradle dependencies (`room`, `compose-material3`, `webkit`).
2. Run `compile_applet` to ensure pristine compilation.
3. Test CUJs: starting chat, switching Claude models, triggering thinking mode, rendering syntax-highlighted code, opening live web artifact preview, and creating project workspaces.
