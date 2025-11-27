package ru.rapidcoder.gemini.bot;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.rapidcoder.gemini.bot.component.KeyboardButton;
import ru.rapidcoder.gemini.bot.handler.MessageHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Bot extends TelegramLongPollingBot {

    private static final Logger logger = LoggerFactory.getLogger(Bot.class);
    private final String botName;
    private final Map<Long, String> prompts = new ConcurrentHashMap<>();
    private final MessageHandler messageHandler;

    public Bot(String botName, String tokenId, String geminiApiKey, String storagePath, List<Long> admins) {
        super(tokenId);
        this.botName = botName;

        messageHandler = new MessageHandler(this, geminiApiKey, storagePath, admins);
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
                Long userId = message.getFrom()
                        .getId();
                long chatId = message.getChatId();
                logger.debug("Обработка сообщения chatId={}, userId={}", chatId, userId);
                if (message.hasText()) {
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

    public void showMainMenu(Long chatId, Integer messageId, boolean isAdmin) {
        String text = "\uD83C\uDFE0 <b>Главное меню</b>";

        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        if (getPrompts().get(chatId) != null && !StringUtils.isEmpty(getPrompts().get(chatId))) {
            rows.add(List.of(new KeyboardButton("\uD83C\uDFA8 Генерация картинки", "menu_generator_picture")));
        }

        rows.add(List.of(new KeyboardButton("\uD83D\uDDBC\uFE0F Список картинок", "menu_generated_pictures"), new KeyboardButton("\uD83D\uDCAC Помощь", "menu_help")));
        keyboard.setKeyboard(rows);

        if (messageId != null) {
            updateMessage(chatId, messageId, text, keyboard);
        } else {
            sendMessage(chatId, text, keyboard);
        }
    }

    public void showHelpMenu(Long chatId, Integer messageId) {
        String text = """
                \uD83D\uDCAC <b>Помощь по боту</b>
                
                <b>Основные команды:</b>
                /start - Главное меню
                /help - Помощь
                
                """;

        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup(List.of(List.of(new KeyboardButton("\uD83C\uDFE0 Главное меню", "back_to_main"))));
        if (messageId != null) {
            updateMessage(chatId, messageId, text, keyboard);
        } else {
            sendMessage(chatId, text, keyboard);
        }
    }

    public void showGeneratePictureMenu(Long chatId, Integer messageId) {
        StringBuilder sb = new StringBuilder();
        sb.append("\uD83C\uDFA8 <b>Генерация картинки</b>\n\n");

        sb.append(String.format("Prompt: %s%n", getPrompts().get(chatId)));

        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        rows.add(List.of(new KeyboardButton("✏\uFE0F Сгенерировать!", "menu_generate_picture"), new KeyboardButton("\uD83D\uDDD1\uFE0F Очистить", "menu_generate_picture_clear")));
        rows.add(List.of(new KeyboardButton("\uD83C\uDFE0 Главное меню", "back_to_main")));
        keyboard.setKeyboard(rows);

        if (messageId != null) {
            updateMessage(chatId, messageId, sb.toString(), keyboard);
        } else {
            sendMessage(chatId, sb.toString(), keyboard);
        }
    }

    public void sendMessage(Long chatId, String text, InlineKeyboardMarkup keyboard) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        message.setParseMode(ParseMode.HTML);
        message.setReplyMarkup(keyboard);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            logger.error(e.getMessage(), e);
        }
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

    public void showNotification(String callbackQueryId, String text) {
        AnswerCallbackQuery answer = new AnswerCallbackQuery();
        answer.setCallbackQueryId(callbackQueryId);
        answer.setText(text);
        answer.setShowAlert(false);
        try {
            execute(answer);
        } catch (TelegramApiException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public Map<Long, String> getPrompts() {
        return prompts;
    }
}
