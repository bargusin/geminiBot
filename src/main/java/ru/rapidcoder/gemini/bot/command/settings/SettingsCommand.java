package ru.rapidcoder.gemini.bot.command.settings;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.rapidcoder.gemini.bot.Bot;
import ru.rapidcoder.gemini.bot.command.AbstractCommand;
import ru.rapidcoder.gemini.bot.component.InterfaceFactory;
import ru.rapidcoder.gemini.bot.component.KeyboardButton;

import java.util.ArrayList;
import java.util.List;

public class SettingsCommand extends AbstractCommand {

    public SettingsCommand(Bot bot, String identifier, String description) {
        super(bot, identifier, description);
    }

    @Override
    public void execute(Update update) {
        String text = InterfaceFactory.format("⚙\uFE0F <b>Настройки</b>");

        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        rows.add(List.of(createChangeValueButton("gemini_api_key_id")));
        rows.add(List.of(createChangeValueButton("qwen_api_key_id")));
        rows.add(List.of(InterfaceFactory.createButton("\uD83C\uDFE0 Главное меню", "back_to_main")));
        keyboard.setKeyboard(rows);

        processMessage(update, text, keyboard);
    }

    private KeyboardButton createChangeValueButton(String valueName) {
        return InterfaceFactory.createButton("✏\uFE0F " + valueName, "change_setting_" + valueName);
    }
}

