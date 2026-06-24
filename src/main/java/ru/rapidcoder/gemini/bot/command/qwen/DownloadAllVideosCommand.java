package ru.rapidcoder.gemini.bot.command.qwen;

import ru.rapidcoder.gemini.bot.Bot;
import ru.rapidcoder.gemini.bot.command.CommandId;
import ru.rapidcoder.gemini.bot.command.MediaDownloadCommand;

import java.io.IOException;

public class DownloadAllVideosCommand extends MediaDownloadCommand {

    public DownloadAllVideosCommand(Bot bot, String identifier, String description) {
        super(bot, identifier, description);
    }

    @Override
    protected String getStoragePath(Long chatId) throws IOException {
        return bot.getStorageVideosPath(chatId);
    }

    @Override
    protected String getArchiveFileName() {
        return "videos_archive.zip";
    }

    @Override
    protected String getListCommandId() {
        return CommandId.MENU_GENERATED_VIDEOS;
    }

    @Override
    protected String getListButtonLabel() {
        return "🎬 Сгенерированные видео";
    }

    @Override
    protected String getEmptyMessage() {
        return "📭 Нет сохранённых видео для выгрузки.";
    }

    @Override
    protected String getSuccessMessage() {
        return "✅ Архив с видео отправлен.";
    }

    @Override
    protected String getReadErrorMessage() {
        return "🚫 Не удалось прочитать список видео.";
    }
}
