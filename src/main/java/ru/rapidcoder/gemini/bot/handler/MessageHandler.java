package ru.rapidcoder.gemini.bot.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.rapidcoder.gemini.bot.Bot;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Stream;

public class MessageHandler {

    private static final Logger logger = LoggerFactory.getLogger(MessageHandler.class);
    private final Bot bot;
    private final GeminiHandler geminiHandler;
    private final List<Long> admins;
    private final String storagePath;

    public MessageHandler(Bot bot, String geminiApiKey, String storagePath, List<Long> admins) {
        this.bot = bot;
        this.admins = admins;
        this.storagePath = storagePath;

        geminiHandler = new GeminiHandler(geminiApiKey);
    }

    public void handleCommand(Update update) {
        Long chatId = update.getMessage()
                .getChatId();
        Long userId = update.getMessage()
                .getFrom()
                .getId();
        String messageText = update.getMessage()
                .getText();
        if (!hasAccess(userId)) {
            //TODO
        } else {
            if ("/start".equals(messageText)) {
                bot.showMainMenu(chatId, null, hasAccess(userId));
            } else if ("/help".equals(messageText)) {
                bot.showHelpMenu(chatId, null);
            } else {
                Message message = update.getMessage();
                logger.debug("Catch prompt for generate picture with id={}", message.getMessageId());
                bot.getPrompts()
                        .computeIfAbsent(chatId, k -> message.getText());
                bot.showGeneratePictureMenu(chatId, null);
            }
        }
    }

    public void handleCallback(Update update) {
        String callbackData = update.getCallbackQuery()
                .getData();
        String callbackId = update.getCallbackQuery()
                .getId();
        Long chatId = update.getCallbackQuery()
                .getMessage()
                .getChatId();
        Long userId = update.getCallbackQuery()
                .getFrom()
                .getId();
        Integer messageId = update.getCallbackQuery()
                .getMessage()
                .getMessageId();

        if (!hasAccess(userId)) {
            logger.debug("User dosn't access to bot by userId={}", userId);
            bot.showNotification(callbackId, "Доступ к боту запрещен");
        } else {
            if (callbackData.startsWith("picture_")) {
                String fileName = callbackData.substring("picture_".length());
                try {
                    bot.execute(bot.sendPicture(chatId, Paths.get(getStoragePath(chatId), fileName)
                            .toString()));
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            } else {
                switch (callbackData) {
                    case "menu_help" -> {
                        bot.showHelpMenu(chatId, messageId);
                    }
                    case "back_to_main" -> {
                        bot.showMainMenu(chatId, messageId, true);
                    }
                    case "menu_generator_picture" -> {
                        bot.showGeneratePictureMenu(chatId, messageId);
                    }
                    case "menu_generate_picture" -> {
                        CompletableFuture.runAsync(() -> {
                            try {
                                byte[] bytes = geminiHandler.generatePicture(bot.getPrompts()
                                        .get(chatId));
                                Path path = Files.write(Paths.get(getStoragePath(chatId), "generated_image_" + new Date().getTime() + ".png"), bytes);
                                bot.execute(bot.sendPicture(chatId, path.toString()));
                            } catch (
                                    Exception e) {
                                logger.error(e.getMessage(), e);
                            }
                        });
                        bot.showNotification(callbackId, "✅ Генерация картинки запущена...");
                        bot.getPrompts()
                                .remove(chatId);
                        bot.showMainMenu(chatId, messageId, hasAccess(userId));
                    }
                    case "menu_generate_picture_clear" -> {
                        bot.getPrompts()
                                .remove(chatId);
                        bot.showMainMenu(chatId, messageId, hasAccess(userId));
                    }
                    case "menu_generated_pictures" -> {
                        List<String> files = new ArrayList<>();
                        try (Stream<Path> paths = Files.list(Paths.get(getStoragePath(chatId)))) {
                            paths.forEach(path -> {
                                if (Files.isRegularFile(path)) {
                                    files.add(path.getFileName()
                                            .toString());
                                }
                            });
                        } catch (IOException e) {
                            logger.error(e.getMessage(), e);
                        }
                        bot.showGeneratedPicturesMenu(chatId, messageId, files);
                    }
                }
            }
        }
    }

    private boolean hasAccess(Long userId) {
        return admins.contains(userId);
    }

    public String getStoragePath(Long chatId) throws IOException {
        return Files.createDirectories(Paths.get(storagePath + "/" + chatId)
                        .getParent())
                .toString();
    }
}
