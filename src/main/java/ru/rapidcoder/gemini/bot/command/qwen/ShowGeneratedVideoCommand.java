package ru.rapidcoder.gemini.bot.command.qwen;

import org.telegram.telegrambots.meta.api.objects.Update;
import ru.rapidcoder.gemini.bot.Bot;
import ru.rapidcoder.gemini.bot.command.AbstractCommand;

import java.nio.file.Paths;

public class ShowGeneratedVideoCommand extends AbstractCommand {

    public ShowGeneratedVideoCommand(Bot bot, String identifier, String description) {
        super(bot, identifier, description);
    }

    @Override
    public void execute(Update update) {
        String fileName = getSuffix(update.getCallbackQuery().getData());
        try {
            bot.execute(bot.sendDocument(getChatId(update), Paths.get(bot.getStorageVideosPath(getChatId(update)), fileName).toString()));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}
