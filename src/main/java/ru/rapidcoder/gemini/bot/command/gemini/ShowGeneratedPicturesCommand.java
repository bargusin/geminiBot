package ru.rapidcoder.gemini.bot.command.gemini;

import ru.rapidcoder.gemini.bot.Bot;
import ru.rapidcoder.gemini.bot.command.CommandId;
import ru.rapidcoder.gemini.bot.command.MediaListCommand;

import java.io.IOException;

public class ShowGeneratedPicturesCommand extends MediaListCommand {

    public ShowGeneratedPicturesCommand(Bot bot, String identifier, String description) {
        super(bot, identifier, description);
    }

    @Override
    protected String getHeaderText() {
        return "🖼️ <b>Список сгенерированных изображений</b>";
    }

    @Override
    protected String getStoragePath(Long chatId) throws IOException {
        return bot.getStoragePicturesPath(chatId);
    }

    @Override
    protected String getShowCommandId() {
        return CommandId.SHOW_PICTURE;
    }

    @Override
    protected String getDownloadAllCommandId() {
        return CommandId.DOWNLOAD_ALL_PICTURES;
    }

    @Override
    protected String getDownloadButtonLabel() {
        return "📦 Скачать все изображения";
    }
}
