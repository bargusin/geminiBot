package ru.rapidcoder.gemini.bot.command;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import ru.rapidcoder.gemini.bot.Bot;
import ru.rapidcoder.gemini.bot.component.InterfaceFactory;

import java.util.List;

public class HelpCommand extends AbstractCommand {

    public HelpCommand(Bot bot, String identifier, String description) {
        super(bot, identifier, description);
    }

    @Override
    public void execute(Update update) {
        String text = """
                💬 <b>Помощь по боту</b>

                <b>Основные команды:</b>
                /start - Главное меню
                /help - Помощь

                """;
        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup(
                List.of(List.of(InterfaceFactory.createButton("🏠 Главное меню", CommandId.BACK_TO_MAIN))));

        processMessage(update, text, keyboard);
    }
}
