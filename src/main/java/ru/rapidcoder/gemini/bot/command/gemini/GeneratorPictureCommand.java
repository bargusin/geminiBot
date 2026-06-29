package ru.rapidcoder.gemini.bot.command.gemini;

import org.apache.commons.lang3.StringUtils;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.rapidcoder.gemini.bot.Bot;
import ru.rapidcoder.gemini.bot.command.AbstractCommand;
import ru.rapidcoder.gemini.bot.command.CommandId;
import ru.rapidcoder.gemini.bot.component.InterfaceFactory;
import ru.rapidcoder.gemini.bot.handler.GeminiHandler;
import ru.rapidcoder.gemini.bot.handler.InputHandler;
import ru.rapidcoder.gemini.database.entity.User;
import ru.rapidcoder.gemini.database.repository.UserRepository;
import ru.rapidcoder.gemini.service.EncryptionService;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class GeneratorPictureCommand extends AbstractCommand {

    protected final UserRepository userRepository;

    protected final EncryptionService encryptionService;

    public GeneratorPictureCommand(Bot bot, String identifier, String description) {
        super(bot, identifier, description);

        this.userRepository = new UserRepository(bot.getDatabaseManager());
        this.encryptionService = new EncryptionService(bot.getSettings().getEncryptedKey());
    }

    @Override
    public void execute(Update update) {
        Long chatId = getChatId(update);

        InlineKeyboardMarkup cancelKeyboard = new InlineKeyboardMarkup();
        cancelKeyboard.setKeyboard(List.of(
                List.of(InterfaceFactory.createButton("❌ Отмена", CommandId.BACK_TO_MAIN))
        ));
        processMessage(update, "Введите описание для генерации изображения:", cancelKeyboard);

        userStateService.setInputHandler(chatId, new InputHandler() {
            @Override
            public boolean handleInput(Update update) {
                InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
                List<List<InlineKeyboardButton>> rows = new ArrayList<>();

                rows.add(List.of(InterfaceFactory.createButton("🏠 Главное меню", CommandId.BACK_TO_MAIN)));
                keyboard.setKeyboard(rows);
                try {
                    Optional<User> user = userRepository.findByChatId(getChatId(update));
                    if (user.isPresent()) {
                        if (StringUtils.isEmpty(user.get().getGeminiApiKeyId())) {
                            throw new IllegalArgumentException("Ключ gemini_api_key_id не задан.");
                        }
                        GeminiHandler geminiHandler = new GeminiHandler(
                                encryptionService.decrypt(user.get().getGeminiApiKeyId()));
                        byte[] bytes = geminiHandler.generatePicture(update.getMessage().getText());
                        String fileName = "generated_image_" + new Random().nextDouble() + ".png";
                        Path path = Files.write(
                                Paths.get(bot.getStoragePicturesPath(getChatId(update)), fileName),
                                bytes);
                        bot.execute(bot.sendDocument(getChatId(update), path.toString()));

                        processMessage(update, "Изображение успешно сгенерировано", keyboard);
                    } else {
                        throw new IllegalArgumentException("Не удалось найти пользователя в базе данных.");
                    }
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                    processMessage(update, "🚫 Не удалось сгенерировать изображение. " + e.getMessage(), keyboard);
                }
                return true;
            }
        });
    }
}
