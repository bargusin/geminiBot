package ru.rapidcoder.gemini.bot.command.settings;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.rapidcoder.gemini.bot.Bot;
import ru.rapidcoder.gemini.bot.command.AbstractCommand;
import ru.rapidcoder.gemini.bot.command.CommandId;
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
        String text = InterfaceFactory.format("⚙️ <b>Настройки</b>");

        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        rows.add(List.of(new KeyboardButton("✏️ gemini_api_key_id", CommandId.CHANGE_GEMINI_API_KEY_ID)));
        rows.add(List.of(new KeyboardButton("✏️ qwen_api_key_id", CommandId.CHANGE_QWEN_API_KEY_ID)));
        rows.add(List.of(InterfaceFactory.createButton("🏠 Главное меню", CommandId.BACK_TO_MAIN)));
        keyboard.setKeyboard(rows);

        processMessage(update, text, keyboard);
    }
}
