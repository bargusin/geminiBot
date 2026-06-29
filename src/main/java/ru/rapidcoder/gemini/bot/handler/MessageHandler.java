package ru.rapidcoder.gemini.bot.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.rapidcoder.gemini.bot.Bot;
import ru.rapidcoder.gemini.bot.command.CommandId;
import ru.rapidcoder.gemini.bot.command.CommandRegistry;
import ru.rapidcoder.gemini.bot.command.HelpCommand;
import ru.rapidcoder.gemini.bot.command.StartCommand;
import ru.rapidcoder.gemini.bot.command.gemini.DownloadAllPicturesCommand;
import ru.rapidcoder.gemini.bot.command.gemini.GeneratorPictureCommand;
import ru.rapidcoder.gemini.bot.command.gemini.ShowGeneratedPictureCommand;
import ru.rapidcoder.gemini.bot.command.gemini.ShowGeneratedPicturesCommand;
import ru.rapidcoder.gemini.bot.command.qwen.DownloadAllVideosCommand;
import ru.rapidcoder.gemini.bot.command.qwen.GeneratorVideoCommand;
import ru.rapidcoder.gemini.bot.command.qwen.ShowGeneratedVideoCommand;
import ru.rapidcoder.gemini.bot.command.qwen.ShowGeneratedVideosCommand;
import ru.rapidcoder.gemini.bot.command.settings.ChangeGeminiApiKeyIdCommand;
import ru.rapidcoder.gemini.bot.command.settings.ChangeQwenApiKeyIdCommand;
import ru.rapidcoder.gemini.bot.command.settings.SettingsCommand;
import ru.rapidcoder.gemini.bot.service.UserStateService;

public class MessageHandler {

    private static final Logger logger = LoggerFactory.getLogger(MessageHandler.class);

    private final Bot bot;

    private final CommandRegistry commandRegistry;

    private final UserStateService userStateService = UserStateService.getInstance();

    public MessageHandler(Bot bot) {
        this.bot = bot;
        this.commandRegistry = new CommandRegistry(bot);

        commandRegistry.registry(new StartCommand(bot, CommandId.START, "Старт"));
        commandRegistry.registry(new StartCommand(bot, CommandId.BACK_TO_MAIN, "Возврат в основное меню"));
        commandRegistry.registry(new HelpCommand(bot, CommandId.HELP, "Помощь"));

        commandRegistry.registry(new SettingsCommand(bot, CommandId.SETTINGS, "Настройки"));
        commandRegistry.registry(new ChangeGeminiApiKeyIdCommand(
                bot, CommandId.CHANGE_GEMINI_API_KEY_ID,
                "Установка нового значения для параметра gemini_api_key_id"));
        commandRegistry.registry(new ChangeQwenApiKeyIdCommand(
                bot, CommandId.CHANGE_QWEN_API_KEY_ID,
                "Установка нового значения для параметра qwen_api_key_id"));

        commandRegistry.registry(new ShowGeneratedPicturesCommand(
                bot, CommandId.MENU_GENERATED_PICTURES,
                "Отображение списка ранее сгенерированных изображений"));
        commandRegistry.registry(new GeneratorPictureCommand(
                bot, CommandId.MENU_GENERATOR_PICTURE, "Генерация изображения"));
        commandRegistry.registry(new ShowGeneratedPictureCommand(
                bot, CommandId.SHOW_PICTURE, "Загрузить ранее сгенерированное изображение"));
        commandRegistry.registry(new DownloadAllPicturesCommand(
                bot, CommandId.DOWNLOAD_ALL_PICTURES,
                "Выгрузка всех сохранённых изображений в архив"));

        commandRegistry.registry(new ShowGeneratedVideosCommand(
                bot, CommandId.MENU_GENERATED_VIDEOS,
                "Отображение списка ранее сгенерированных видео"));
        commandRegistry.registry(new GeneratorVideoCommand(
                bot, CommandId.MENU_GENERATOR_VIDEO, "Генерация видео"));
        commandRegistry.registry(new ShowGeneratedVideoCommand(
                bot, CommandId.SHOW_VIDEO, "Загрузить ранее сгенерированное видео"));
        commandRegistry.registry(new DownloadAllVideosCommand(
                bot, CommandId.DOWNLOAD_ALL_VIDEOS,
                "Выгрузка всех сохранённых видео в архив"));
    }

    public void handleCommand(Update update) {
        String messageText = update.getMessage().getText();
        Long userId = update.getMessage().getFrom().getId();

        if (bot.hasAccess(userId)) {
            bot.sendMessage(userId, "Доступ к боту запрещен", null);
        } else {
            if (CommandId.CANCEL.equals(messageText)) {
                userStateService.clearState(userId);
                return;
            }

            InputHandler activeHandler = userStateService.getInputHandler(userId);
            if (activeHandler != null) {
                boolean finished = activeHandler.handleInput(update);
                if (finished) {
                    userStateService.clearState(userId);
                }
                return;
            }

            try {
                int index = messageText.indexOf(' ');
                if (index > 0) {
                    messageText = messageText.substring(0, index);
                }
                commandRegistry.retrieveCommand(messageText).execute(update);
            } catch (IllegalStateException e) {
                logger.error("Ошибка обработки команды", e);
            }
        }
    }

    public void handleCallback(Update update) {
        String callbackData = update.getCallbackQuery().getData();
        Long userId = update.getCallbackQuery().getFrom().getId();

        if (bot.hasAccess(userId)) {
            bot.sendMessage(userId, "Доступ к боту запрещен", null);
        } else {
            int index = callbackData.indexOf('#');
            if (index > 0) {
                callbackData = callbackData.substring(0, index);
            }

            try {
                userStateService.clearState(userId);
                commandRegistry.retrieveCommand(callbackData).execute(update);
            } catch (IllegalStateException e) {
                logger.error("Ошибка обработки команды", e);
            }
        }
    }
}
