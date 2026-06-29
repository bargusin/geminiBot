package ru.rapidcoder.gemini.bot.command.qwen;

import ru.rapidcoder.gemini.bot.Bot;
import ru.rapidcoder.gemini.bot.command.MediaShowCommand;

import java.io.IOException;

public class ShowGeneratedVideoCommand extends MediaShowCommand {

    public ShowGeneratedVideoCommand(Bot bot, String identifier, String description) {
        super(bot, identifier, description);
    }

    @Override
    protected String getStoragePath(Long chatId) throws IOException {
        return bot.getStorageVideosPath(chatId);
    }
}
