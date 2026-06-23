package ru.rapidcoder.gemini.bot.command.settings;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import ru.rapidcoder.gemini.bot.Bot;
import ru.rapidcoder.gemini.bot.SettingsBot;
import ru.rapidcoder.gemini.bot.handler.InputHandler;
import ru.rapidcoder.gemini.bot.service.UserStateService;
import ru.rapidcoder.gemini.database.DatabaseManager;
import ru.rapidcoder.gemini.database.entity.User;
import ru.rapidcoder.gemini.database.repository.UserRepository;
import ru.rapidcoder.gemini.service.EncryptionService;

import javax.crypto.KeyGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ChangeGeminiApiKeyIdCommandTest {

    @Mock
    private Bot bot;

    private DatabaseManager databaseManager;
    private UserRepository userRepository;
    private EncryptionService encryptionService;
    private ChangeGeminiApiKeyIdCommand command;
    private String validKeyBase64;

    private static final Long CHAT_ID = 42L;
    private static final Integer MESSAGE_ID = 100;

    @BeforeAll
    void setUpAll() throws NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256);
        byte[] key = keyGen.generateKey().getEncoded();
        validKeyBase64 = Base64.getEncoder().encodeToString(key);
    }

    @BeforeEach
    void setUp() {
        databaseManager = DatabaseManager.getInstance(":memory:");
        userRepository = new UserRepository(databaseManager);
        encryptionService = new EncryptionService(validKeyBase64);

        SettingsBot settings = new SettingsBot();
        settings.setEncryptedKey(validKeyBase64);

        when(bot.getDatabaseManager()).thenReturn(databaseManager);
        when(bot.getSettings()).thenReturn(settings);

        command = new ChangeGeminiApiKeyIdCommand(bot, "/change_gemini_api_key", "Изменить Gemini API ключ");

        UserStateService.getInstance().clearState(CHAT_ID);
    }

    @AfterEach
    void tearDown() {
        UserStateService.getInstance().clearState(CHAT_ID);
        if (databaseManager != null) {
            databaseManager.close();
        }
    }

    private Update buildCallbackUpdate(Long chatId, Integer messageId) {
        Message message = mock(Message.class);
        when(message.getChatId()).thenReturn(chatId);
        when(message.getMessageId()).thenReturn(messageId);

        CallbackQuery callbackQuery = mock(CallbackQuery.class);
        when(callbackQuery.getMessage()).thenReturn(message);

        Update update = mock(Update.class);
        when(update.hasCallbackQuery()).thenReturn(true);
        when(update.getCallbackQuery()).thenReturn(callbackQuery);

        return update;
    }

    private Update buildMessageUpdate(Long chatId, String text) {
        Message message = mock(Message.class);
        when(message.getChatId()).thenReturn(chatId);
        when(message.getText()).thenReturn(text);

        Update update = mock(Update.class);
        when(update.hasCallbackQuery()).thenReturn(false);
        when(update.hasMessage()).thenReturn(true);
        when(update.getMessage()).thenReturn(message);

        return update;
    }

    private User createTestUser(Long chatId) {
        User user = new User();
        user.setChatId(chatId);
        user.setUserName("testUser");
        return userRepository.save(user);
    }

    @Test
    @DisplayName("getIdentifier: должен вернуть переданный идентификатор")
    void testGetIdentifier() {
        assertEquals("/change_gemini_api_key", command.getIdentifier());
    }

    @Test
    @DisplayName("getDescription: должен вернуть переданное описание")
    void testGetDescription() {
        assertEquals("Изменить Gemini API ключ", command.getDescription());
    }

    @Test
    @DisplayName("execute: должен запросить новое значение у пользователя")
    void testExecuteSendsPromptMessage() {
        Update update = buildCallbackUpdate(CHAT_ID, MESSAGE_ID);

        command.execute(update);

        verify(bot).updateMessage(
                eq(CHAT_ID),
                eq(MESSAGE_ID),
                eq("Введите новое значение для gemini_api_key_id:"),
                any(InlineKeyboardMarkup.class)
        );
    }

    @Test
    @DisplayName("execute: должен зарегистрировать InputHandler в UserStateService")
    void testExecuteRegistersInputHandler() {
        Update update = buildCallbackUpdate(CHAT_ID, MESSAGE_ID);

        command.execute(update);

        InputHandler handler = UserStateService.getInstance().getInputHandler(CHAT_ID);
        assertNotNull(handler, "InputHandler должен быть зарегистрирован после вызова execute");
    }

    @Test
    @DisplayName("InputHandler: при успешном обновлении должен отправить сообщение об успехе")
    void testInputHandlerSendsSuccessMessage() {
        createTestUser(CHAT_ID);
        Update executeUpdate = buildCallbackUpdate(CHAT_ID, MESSAGE_ID);
        command.execute(executeUpdate);

        InputHandler handler = UserStateService.getInstance().getInputHandler(CHAT_ID);
        assertNotNull(handler);

        Update inputUpdate = buildMessageUpdate(CHAT_ID, "my-new-gemini-api-key");
        handler.handleInput(inputUpdate);

        verify(bot).sendMessage(
                eq(CHAT_ID),
                eq("✅ Новое значение для gemini_api_key_id успешно установлено"),
                any(InlineKeyboardMarkup.class)
        );
    }

    @Test
    @DisplayName("InputHandler: при ошибке обновления должен отправить сообщение об ошибке")
    void testInputHandlerSendsErrorMessageWhenUpdateFails() {
        // Пользователь не создан → updateGeminiApiKeyId выбросит RuntimeException
        Update executeUpdate = buildCallbackUpdate(CHAT_ID, MESSAGE_ID);
        command.execute(executeUpdate);

        InputHandler handler = UserStateService.getInstance().getInputHandler(CHAT_ID);
        assertNotNull(handler);

        Update inputUpdate = buildMessageUpdate(CHAT_ID, "some-key");
        handler.handleInput(inputUpdate);

        verify(bot).sendMessage(
                eq(CHAT_ID),
                eq("🚫 Не удалось установить значение для gemini_api_key_id"),
                any(InlineKeyboardMarkup.class)
        );
    }

    @Test
    @DisplayName("InputHandler: должен всегда возвращать true")
    void testInputHandlerAlwaysReturnsTrue() {
        createTestUser(CHAT_ID);
        Update executeUpdate = buildCallbackUpdate(CHAT_ID, MESSAGE_ID);
        command.execute(executeUpdate);

        InputHandler handler = UserStateService.getInstance().getInputHandler(CHAT_ID);
        assertNotNull(handler);

        Update inputUpdate = buildMessageUpdate(CHAT_ID, "any-key");
        assertTrue(handler.handleInput(inputUpdate));
    }

    @Test
    @DisplayName("InputHandler: новый ключ должен быть зашифрован и сохранён в базе данных")
    void testInputHandlerSavesEncryptedKeyToDatabase() {
        createTestUser(CHAT_ID);
        Update executeUpdate = buildCallbackUpdate(CHAT_ID, MESSAGE_ID);
        command.execute(executeUpdate);

        InputHandler handler = UserStateService.getInstance().getInputHandler(CHAT_ID);
        assertNotNull(handler);

        String newApiKey = "super-secret-gemini-key";
        Update inputUpdate = buildMessageUpdate(CHAT_ID, newApiKey);
        handler.handleInput(inputUpdate);

        Optional<User> savedUser = userRepository.findByChatId(CHAT_ID);
        assertTrue(savedUser.isPresent());
        String decryptedKey = encryptionService.decrypt(savedUser.get().getGeminiApiKeyId());
        assertEquals(newApiKey, decryptedKey);
    }
}
