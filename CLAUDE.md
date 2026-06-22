# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run

```bash
./gradlew build          # Build and run tests
./gradlew test           # Run tests only
./gradlew test --info    # Verbose test output
./gradlew run            # Run the application
./gradlew jar            # Create fat JAR in build/libs/
```

Run a single test class:
```bash
./gradlew test --tests "ru.rapidcoder.gemini.database.test.DatabaseManagerTest"
```

Coverage report is generated at `build/reports/jacoco/html/index.html`. JaCoCo enforces a 10% minimum coverage threshold — the build fails if coverage drops below this.

## Architecture Overview

This is a Telegram bot that generates images (Google Gemini) and videos (Alibaba Qwen) on user request. Written in Java 17 with Gradle.

### Message Flow

```
Telegram Update
  → Bot.onUpdateReceived()
  → MessageHandler.handleCommand() / handleCallback()
      ├─ If user has an active InputHandler (multi-step flow): route there
      ├─ /cancel clears the active InputHandler
      └─ Otherwise: CommandRegistry → Command.execute()
```

**Multi-step input**: `UserStateService` (singleton) stores an `InputHandler` per chat ID. Commands that need follow-up input (e.g., prompts for generation) set an `InputHandler` on the service, which intercepts the next message.

### Key Packages

| Package | Role |
|---------|------|
| `bot/` | `Bot` (Telegram long-polling), `MessageHandler` (routing), `SettingsBot` (config) |
| `bot/command/` | One class per command; `CommandRegistry` maps text/callbacks to `Command` instances |
| `bot/handler/` | `GeminiHandler` (image gen), `QwenVideoGenerator` (async video gen with polling) |
| `bot/service/` | `UserStateService` — in-memory per-user state for multi-step flows |
| `database/` | `DatabaseManager` singleton (HikariCP + Flyway + Hibernate), `UserRepository` |
| `service/` | `EncryptionService` — AES-256-GCM for storing user API keys |

### Database

SQLite with Hibernate ORM. Schema managed by Flyway (`src/main/resources/migration/`). HikariCP pool size is 1 (SQLite limitation). All DB access goes through `DatabaseManager.executeInTransaction()`.

User API keys (Gemini, Qwen) are stored encrypted in the `users` table. Encryption key comes from the `encryptedKey` environment variable.

Generated files are saved to `{storagePath}/pictures/{chatId}/` and `{storagePath}/videos/{chatId}/`.

### Configuration

The bot reads environment-prefixed variables based on `botEnv` (e.g., `dev` or `prod`):

```
botEnv=dev
devBotName=<telegram_handle>
devTokenId=<telegram_token>
devEncryptedKey=<base64_aes256_key>
devGeminiApiKey=<gemini_api_key>
devQwenApiKeyId=<qwen_key>
devQwenApiKeySecret=<qwen_secret>
devStoragePath=/path/to/storage
devDataBaseFile=/path/to/db.sqlite
```

### External APIs

- **Google Gemini** (`gemini-2.5-flash-image` model) — synchronous image generation
- **Alibaba Qwen** (`wan2.6-t2v` model) — asynchronous video generation with polling loop
- **Telegram Bot API** — long polling, inline keyboards, callback queries
