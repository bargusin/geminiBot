package ru.rapidcoder.gemini.database.repository.test;

import org.junit.jupiter.api.*;
import ru.rapidcoder.gemini.database.DatabaseManager;
import ru.rapidcoder.gemini.database.entity.User;
import ru.rapidcoder.gemini.database.repository.UserRepository;
import ru.rapidcoder.gemini.service.EncryptionService;

import javax.crypto.KeyGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UserRepositoryTest {

    private DatabaseManager databaseManager;

    private UserRepository userRepository;

    private EncryptionService encryptionService;

    @BeforeAll
    void setUpAll() throws NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256);
        byte[] key = keyGen.generateKey().getEncoded();
        String validKeyBase64 = Base64.getEncoder().encodeToString(key);
        encryptionService = new EncryptionService(validKeyBase64);
    }

    @BeforeEach
    void setUp() {
        databaseManager = DatabaseManager.getInstance(":memory:");
        userRepository = new UserRepository(databaseManager);
    }

    @AfterEach
    void cleanup() {
        if (databaseManager != null) {
            databaseManager.close();
        }
    }

    @Test
    @DisplayName("Интеграционный тест: Полный цикл жизни пользователя (Save -> Find)")
    void testCreateUser() {
        User newUser = new User();
        newUser.setChatId(100L);
        newUser.setUserName("User");
        newUser.setGeminiApiKeyId("gemini_api_key_id");
        newUser.setQwenApiKeyId("qwen_api_key_id");
        newUser.setQwenApiKeySecret("qwen_api_key_secret");

        User savedUser = userRepository.save(newUser);
        assertNotNull(savedUser);
        assertNotNull(savedUser.getChatId());

        Optional<User> foundOpt = userRepository.findByChatId(100L);

        assertTrue(foundOpt.isPresent(), "Пользователь должен быть найден");
        User foundUser = foundOpt.get();

        assertEquals(savedUser.getChatId(), foundUser.getChatId());
        assertEquals(savedUser.getUserName(), foundUser.getUserName());
        assertEquals(savedUser.getCreatedAt(), foundUser.getCreatedAt());
        assertEquals(savedUser.getGeminiApiKeyId(), foundUser.getGeminiApiKeyId());
        assertEquals(savedUser.getQwenApiKeyId(), foundUser.getQwenApiKeyId());
        assertEquals(savedUser.getQwenApiKeySecret(), foundUser.getQwenApiKeySecret());
    }

    @Test
    @DisplayName("Интеграционный тест: Обновление параметра gemini_api_key_id")
    void testUpdateGeminiApiKeyId() {
        User newUser = new User();
        newUser.setChatId(100L);
        newUser.setUserName("User");

        User savedUser = userRepository.save(newUser);
        assertNotNull(savedUser);
        assertNotNull(savedUser.getChatId());

        userRepository.updateGeminiApiKeyId(100L, encryptionService.encrypt("new_gemini_api_key_id"));
        Optional<User> refreshedOpt = userRepository.findByChatId(100L);
        assertTrue(refreshedOpt.isPresent());
        User refreshedUser = refreshedOpt.get();
        assertEquals("new_gemini_api_key_id", encryptionService.decrypt(refreshedUser.getGeminiApiKeyId()));
    }

    @Test
    @DisplayName("Интеграционный тест: Обновление параметра qwen_api_key_id")
    void testUpdateQwenApiKeyId() {
        User newUser = new User();
        newUser.setChatId(100L);
        newUser.setUserName("User");

        User savedUser = userRepository.save(newUser);
        assertNotNull(savedUser);
        assertNotNull(savedUser.getChatId());

        userRepository.updateQwenApiKeyId(100L, encryptionService.encrypt("new_qwen_api_key_id"));
        Optional<User> refreshedOpt = userRepository.findByChatId(100L);
        assertTrue(refreshedOpt.isPresent());
        User refreshedUser = refreshedOpt.get();
        assertEquals("new_qwen_api_key_id", encryptionService.decrypt(refreshedUser.getQwenApiKeyId()));
    }

    @Test
    @DisplayName("Интеграционный тест: Обновление параметра qwen_api_key_secret")
    void testUpdateQwenApiKeyQwenApiKeySecret() {
        User newUser = new User();
        newUser.setChatId(100L);
        newUser.setUserName("User");

        User savedUser = userRepository.save(newUser);
        assertNotNull(savedUser);
        assertNotNull(savedUser.getChatId());

        userRepository.updateQwenApiKeySecret(100L, encryptionService.encrypt("new_qwen_api_key_secret"));
        Optional<User> refreshedOpt = userRepository.findByChatId(100L);
        assertTrue(refreshedOpt.isPresent());
        User refreshedUser = refreshedOpt.get();
        assertEquals("new_qwen_api_key_secret", encryptionService.decrypt(refreshedUser.getQwenApiKeySecret()));
    }

    @Test
    @DisplayName("Интеграционный тест: Проверка существования пользователя")
    void testExistsByChatId() {
        User newUser = new User();
        newUser.setChatId(100L);
        newUser.setUserName("Trader");

        User savedUser = userRepository.save(newUser);
        assertNotNull(savedUser);
        assertNotNull(savedUser.getChatId());

        assertTrue(userRepository.existsByChatId(100L), "Пользователь должен быть найден");
    }
}
