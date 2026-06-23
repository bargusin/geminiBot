package ru.rapidcoder.gemini.bot.command;

import org.telegram.telegrambots.meta.api.objects.Update;
import ru.rapidcoder.gemini.bot.Bot;

public class UnknownCommand extends AbstractCommand {

    public UnknownCommand(Bot bot) {
        this(bot, CommandId.UNKNOWN, "Unknown command");
    }

    private UnknownCommand(Bot bot, String identifier, String description) {
        super(bot, identifier, description);
    }

    @Override
    public void execute(Update update) {
        processMessage(update, getIdentifier(), null);
    }
}
