CREATE TABLE IF NOT EXISTS users (
    chat_id BIGINT PRIMARY KEY,
    user_name TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS user_settings (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    chat_id BIGINT NOT NULL,
    gemini_api_key_id TEXT,
    qwen_api_key_id TEXT

    FOREIGN KEY (chat_id) REFERENCES users(chat_id) ON DELETE CASCADE,

    UNIQUE(chat_id)
);