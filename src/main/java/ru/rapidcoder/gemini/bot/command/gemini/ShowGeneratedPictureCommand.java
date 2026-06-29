package ru.rapidcoder.gemini.bot.command.gemini;

import ru.rapidcoder.gemini.bot.Bot;
import ru.rapidcoder.gemini.bot.command.MediaShowCommand;

import java.io.IOException;

public class ShowGeneratedPictureCommand extends MediaShowCommand {

    public ShowGeneratedPictureCommand(Bot bot, String identifier, String description) {
        super(bot, identifier, description);
    }

    @Override
    protected String getStoragePath(Long chatId) throws IOException {
        return bot.getStoragePicturesPath(chatId);
    }
}
