package ru.rapidcoder.gemini.bot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.rapidcoder.gemini.bot.handler.MessageHandler;
import ru.rapidcoder.gemini.database.DatabaseManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Bot extends TelegramLongPollingBot {

    private static final Logger logger = LoggerFactory.getLogger(Bot.class);
    private final String botName;
    private final DatabaseManager databaseManager;
    private final SettingsBot settings;
    private final MessageHandler messageHandler;

    public Bot(SettingsBot settings) {
        super(settings.getTokenId());
        this.botName = settings.getBotName();
        this.settings = settings;

        this.databaseManager = DatabaseManager.getInstance(settings.getDataBaseFile());
        this.messageHandler = new MessageHandler(this);
    }

    @Override
    public String getBotUsername() {
        return botName;
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            if (update.hasMessage()) {
                Message message = update.getMessage();
                Long userId = message.getFrom().getId();
                long chatId = message.getChatId();
                logger.info("Обработка сообщения chatId={}, userId={}", chatId, userId);
                if (message.hasText()) {
                    handleCommand(update);
                } else if (message.hasPhoto()) {
                    handleCommand(update);
                }
            } else if (update.hasCallbackQuery()) {
                handleCallback(update);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void handleCommand(Update update) {
        messageHandler.handleCommand(update);
    }

    public void handleCallback(Update update) {
        messageHandler.handleCallback(update);
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public SettingsBot getSettings() {
        return settings;
    }

    public boolean hasAccess(Long userId) {
        return false;
    }

    public Message sendMessage(Long chatId, String text, InlineKeyboardMarkup keyboard) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        message.setParseMode(ParseMode.HTML);
        message.setReplyMarkup(keyboard);
        try {
            return execute(message);
        } catch (TelegramApiException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    public void updateMessage(Long chatId, Integer messageId, String text, InlineKeyboardMarkup keyboard) {
        EditMessageText message = new EditMessageText();
        message.setChatId(chatId);
        message.setText(text);
        message.setMessageId(messageId);
        message.setParseMode(ParseMode.HTML);
        message.setReplyMarkup(keyboard);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public SendDocument sendDocument(Long chatId, String fileName) {
        File file = new File(fileName);
        SendDocument document = new SendDocument();
        document.setChatId(chatId);
        document.setDocument(new InputFile(file, file.getName()));
        return document;
    }

    public String getStoragePicturesPath(Long chatId) throws IOException {
        return Files.createDirectories(Paths.get(getSettings().getStoragePath() + "/pictures/" + chatId)).toString();
    }

    public String getStorageVideosPath(Long chatId) throws IOException {
        return Files.createDirectories(Paths.get(getSettings().getStoragePath() + "/videos/" + chatId)).toString();
    }

    public String getStorageUploadPath(Long chatId) throws IOException {
        return Files.createDirectories(
                Paths.get(getSettings().getStoragePath() + "/videos/" + chatId + "/upload")
        ).toString();
    }
}
