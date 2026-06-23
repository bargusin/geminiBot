package ru.rapidcoder.gemini.bot.command.settings;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.rapidcoder.gemini.bot.Bot;
import ru.rapidcoder.gemini.bot.command.AbstractCommand;
import ru.rapidcoder.gemini.bot.command.CommandId;
import ru.rapidcoder.gemini.bot.component.InterfaceFactory;
import ru.rapidcoder.gemini.bot.handler.InputHandler;
import ru.rapidcoder.gemini.database.repository.UserRepository;
import ru.rapidcoder.gemini.service.EncryptionService;

import java.util.ArrayList;
import java.util.List;

public abstract class ChangeValueCommand extends AbstractCommand {

    protected final UserRepository userRepository;

    protected final EncryptionService encryptionService;

    public ChangeValueCommand(Bot bot, String identifier, String description) {
        super(bot, identifier, description);

        this.userRepository = new UserRepository(bot.getDatabaseManager());
        this.encryptionService = new EncryptionService(bot.getSettings().getEncryptedKey());
    }

    protected void changeValue(String valueName, Update update) {
        Long chatId = getChatId(update);

        InlineKeyboardMarkup cancelKeyboard = new InlineKeyboardMarkup();
        cancelKeyboard.setKeyboard(List.of(
                List.of(InterfaceFactory.createButton("❌ Отмена", CommandId.SETTINGS))
        ));
        processMessage(update, String.format("Введите новое значение для %s:", valueName), cancelKeyboard);

        userStateService.setInputHandler(chatId, new InputHandler() {
            @Override
            public boolean handleInput(Update update) {
                InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
                List<List<InlineKeyboardButton>> rows = new ArrayList<>();

                rows.add(List.of(InterfaceFactory.createButton("⚙️ Настройки", CommandId.SETTINGS)));
                keyboard.setKeyboard(rows);
                try {
                    updateValue(update);
                    processMessage(update, String.format("✅ Новое значение для %s успешно установлено", valueName), keyboard);
                } catch (RuntimeException e) {
                    processMessage(update, String.format("🚫 Не удалось установить значение для %s", valueName), keyboard);
                }
                return true;
            }
        });
    }

    abstract void updateValue(Update update);
}
