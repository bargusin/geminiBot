package ru.rapidcoder.gemini.bot.command.gemini;

import ru.rapidcoder.gemini.bot.Bot;
import ru.rapidcoder.gemini.bot.command.CommandId;
import ru.rapidcoder.gemini.bot.command.MediaDownloadCommand;

import java.io.IOException;

public class DownloadAllPicturesCommand extends MediaDownloadCommand {

    public DownloadAllPicturesCommand(Bot bot, String identifier, String description) {
        super(bot, identifier, description);
    }

    @Override
    protected String getStoragePath(Long chatId) throws IOException {
        return bot.getStoragePicturesPath(chatId);
    }

    @Override
    protected String getArchiveFileName() {
        return "pictures_archive.zip";
    }

    @Override
    protected String getListCommandId() {
        return CommandId.MENU_GENERATED_PICTURES;
    }

    @Override
    protected String getListButtonLabel() {
        return "🖼️ Сгенерированные изображения";
    }

    @Override
    protected String getEmptyMessage() {
        return "📭 Нет сохранённых изображений для выгрузки.";
    }

    @Override
    protected String getSuccessMessage() {
        return "✅ Архив с изображениями отправлен.";
    }

    @Override
    protected String getReadErrorMessage() {
        return "🚫 Не удалось прочитать список изображений.";
    }
}
