package ru.rapidcoder.gemini.bot.command.qwen;

import ru.rapidcoder.gemini.bot.Bot;
import ru.rapidcoder.gemini.bot.command.CommandId;
import ru.rapidcoder.gemini.bot.command.MediaListCommand;

import java.io.IOException;

public class ShowGeneratedVideosCommand extends MediaListCommand {

    public ShowGeneratedVideosCommand(Bot bot, String identifier, String description) {
        super(bot, identifier, description);
    }

    @Override
    protected String getHeaderText() {
        return "🎬 <b>Список сгенерированных видео</b>";
    }

    @Override
    protected String getStoragePath(Long chatId) throws IOException {
        return bot.getStorageVideosPath(chatId);
    }

    @Override
    protected String getShowCommandId() {
        return CommandId.SHOW_VIDEO;
    }

    @Override
    protected String getDownloadAllCommandId() {
        return CommandId.DOWNLOAD_ALL_VIDEOS;
    }

    @Override
    protected String getDownloadButtonLabel() {
        return "📦 Скачать все видео";
    }
}
