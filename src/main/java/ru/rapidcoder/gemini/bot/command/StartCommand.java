package ru.rapidcoder.gemini.bot.command;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.rapidcoder.gemini.bot.Bot;
import ru.rapidcoder.gemini.bot.component.InterfaceFactory;
import ru.rapidcoder.gemini.bot.component.KeyboardButton;
import ru.rapidcoder.gemini.database.entity.User;
import ru.rapidcoder.gemini.database.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

public class StartCommand extends AbstractCommand {

    private final UserRepository userRepository;

    public StartCommand(Bot bot, String identifier, String description) {
        super(bot, identifier, description);

        userRepository = new UserRepository(bot.getDatabaseManager());
    }

    @Override
    public void execute(Update update) {
        if (!userRepository.existsByChatId(getChatId(update))) {
            logger.info("Создается новый пользователь для первого запуска бота");
            User user = new User();
            user.setChatId(getChatId(update));
            user.setUserName(getUserName(update));
            userRepository.save(user);
        }

        String text = InterfaceFactory.format("🏠 <b>Главное меню</b>");

        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        rows.add(List.of(new KeyboardButton("🎨 Генерация изображения", CommandId.MENU_GENERATOR_PICTURE)));
        rows.add(List.of(new KeyboardButton("🎥 Генерация видео", CommandId.MENU_GENERATOR_VIDEO)));
        rows.add(List.of(new KeyboardButton("🖼️ Сгенерированные изображения", CommandId.MENU_GENERATED_PICTURES)));
        rows.add(List.of(new KeyboardButton("🎬 Сгенерированные видео", CommandId.MENU_GENERATED_VIDEOS)));
        rows.add(List.of(InterfaceFactory.createButton("⚙️ Настройки", CommandId.SETTINGS), InterfaceFactory.createButton("💬 Помощь", CommandId.HELP)));
        keyboard.setKeyboard(rows);

        processMessage(update, text, keyboard);
    }
}
