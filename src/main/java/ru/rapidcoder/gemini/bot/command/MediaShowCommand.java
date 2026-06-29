package ru.rapidcoder.gemini.bot.command;

import org.telegram.telegrambots.meta.api.objects.Update;
import ru.rapidcoder.gemini.bot.Bot;

import java.io.IOException;
import java.nio.file.Paths;

public abstract class MediaShowCommand extends AbstractCommand {

    public MediaShowCommand(Bot bot, String identifier, String description) {
        super(bot, identifier, description);
    }

    protected abstract String getStoragePath(Long chatId) throws IOException;

    @Override
    public void execute(Update update) {
        String fileName = getSuffix(update.getCallbackQuery().getData());
        try {
            bot.execute(bot.sendDocument(
                    getChatId(update),
                    Paths.get(getStoragePath(getChatId(update)), fileName).toString()));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}
