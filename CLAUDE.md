# CLAUDE.md

Этот файл содержит инструкции для Claude Code (claude.ai/code) при работе с кодом в этом репозитории.

## Сборка и запуск

```bash
./gradlew build          # Сборка и запуск тестов
./gradlew test           # Только тесты
./gradlew test --info    # Подробный вывод тестов
./gradlew run            # Запуск приложения
./gradlew jar            # Создать fat JAR в build/libs/
```

Запуск одного тестового класса:
```bash
./gradlew test --tests "ru.rapidcoder.gemini.database.test.DatabaseManagerTest"
```

Отчёт о покрытии генерируется в `build/reports/jacoco/html/index.html`. JaCoCo требует минимум 10% покрытия — сборка упадёт, если покрытие ниже этого порога.

## Архитектура

Telegram-бот для генерации изображений (Google Gemini) и видео (Alibaba Qwen) по запросу пользователя. Написан на Java 17 с Gradle.

### Поток сообщений

```
Telegram Update
  → Bot.onUpdateReceived()
  → MessageHandler.handleCommand() / handleCallback()
      ├─ Если у пользователя есть активный InputHandler (многошаговый флоу): перенаправить туда
      ├─ /cancel очищает активный InputHandler
      └─ Иначе: CommandRegistry → Command.execute()
```

**Многошаговый ввод**: `UserStateService` (синглтон) хранит `InputHandler` для каждого chat ID. Команды, требующие дополнительного ввода (например, промпты для генерации), устанавливают `InputHandler` в сервис, который перехватывает следующее сообщение.

### Ключевые пакеты

| Пакет | Роль |
|-------|------|
| `bot/` | `Bot` (Telegram long-polling), `MessageHandler` (маршрутизация), `SettingsBot` (конфигурация) |
| `bot/command/` | По одному классу на команду; `CommandRegistry` сопоставляет текст/колбэки с экземплярами `Command` |
| `bot/handler/` | `GeminiHandler` (генерация изображений), `QwenVideoGenerator` (асинхронная генерация видео с поллингом) |
| `bot/service/` | `UserStateService` — хранит состояние пользователя в памяти для многошаговых флоу |
| `database/` | `DatabaseManager` синглтон (HikariCP + Flyway + Hibernate), `UserRepository` |
| `service/` | `EncryptionService` — AES-256-GCM для хранения API-ключей пользователей |

### База данных

SQLite с Hibernate ORM. Схема управляется через Flyway (`src/main/resources/migration/`). Размер пула HikariCP равен 1 (ограничение SQLite). Весь доступ к БД идёт через `DatabaseManager.executeInTransaction()`.

API-ключи пользователей (Gemini, Qwen) хранятся в зашифрованном виде в таблице `users`. Ключ шифрования берётся из переменной окружения `encryptedKey`.

Сгенерированные файлы сохраняются в `{storagePath}/pictures/{chatId}/` и `{storagePath}/videos/{chatId}/`.

### Конфигурация

Бот читает переменные окружения с префиксом, заданным в `botEnv` (например, `dev` или `prod`):

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

### Внешние API

- **Google Gemini** (модель `gemini-2.5-flash-image`) — синхронная генерация изображений
- **Alibaba Qwen** (модель `wan2.6-t2v`) — асинхронная генерация видео с поллингом
- **Telegram Bot API** — long polling, inline-клавиатуры, callback-запросы

## 📝 Стандарты Git и Коммитов
* **Префикс задач:** В проекте используется сквозной префикс `PROJ-` для всех задач.
* **Формат коммитов:** Строго используй формат `[PROJ-цифры] тип(контекст): описание`.
* **Автоматизация:** Для объединения коммитов всегда используй локальный скилл `git-squash` (находится в `.claude/skills/git-squash/SKILL.md`). Не пытайся выполнять squash альтернативными опасными командами вроде `git rebase -i`.