package ru.rapidcoder.gemini.database.repository;

import org.hibernate.Session;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rapidcoder.gemini.database.DatabaseManager;
import ru.rapidcoder.gemini.database.entity.User;

import java.util.Optional;

public class UserRepository {

    private static final Logger logger = LoggerFactory.getLogger(UserRepository.class);

    private final DatabaseManager databaseManager;

    public UserRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    private Optional<User> getUser(Session session, Long chatId) {
        String hql = "SELECT u FROM User u WHERE u.chatId = :chatId";
        Query<User> query = session.createQuery(hql, User.class);
        query.setParameter("chatId", chatId);
        User user = query.uniqueResult();
        return Optional.ofNullable(user);
    }

    public User save(User user) {
        return databaseManager.executeInTransaction(session -> {
            User savedUser = session.merge(user);
            session.flush();
            return savedUser;
        });
    }

    public Optional<User> findByChatId(Long chatId) {
        try {
            return databaseManager.executeInTransaction(session -> getUser(session, chatId));
        } catch (Exception e) {
            logger.error("Ошибка при поиске пользователя chatId={}", chatId, e);
            return Optional.empty();
        }
    }

    public void updateGeminiApiKeyId(Long chatId, String value) {
        databaseManager.executeInTransaction(session -> {
            Optional<User> user = getUser(session, chatId);
            if (user.isPresent()) {
                user.get().setGeminiApiKeyId(value);
                logger.info("Задано новое значение gemini_api_key_id {} для пользователя {}", value, chatId);
                session.merge(user.get());
                session.flush();
            } else {
                logger.warn("Попытка обновить gemini_api_key_id для несуществующего пользователя: {}", chatId);
                throw new RuntimeException("Попытка обновить gemini_api_key_id для несуществующего пользователя: " + chatId);
            }
            return null;
        });
    }

    public void updateQwenApiKeyId(Long chatId, String value) {
        databaseManager.executeInTransaction(session -> {
            Optional<User> user = getUser(session, chatId);
            if (user.isPresent()) {
                user.get().setQwenApiKeyId(value);
                logger.info("Задано новое значение qwen_api_key_id {} для пользователя {}", value, chatId);
                session.merge(user.get());
                session.flush();
            } else {
                logger.warn("Попытка обновить qwen_api_key_id для несуществующего пользователя: {}", chatId);
                throw new RuntimeException("Попытка обновить qwen_api_key_id для несуществующего пользователя: " + chatId);
            }
            return null;
        });
    }

    public boolean existsByChatId(Long chatId) {
        try {
            return databaseManager.executeInTransaction(session -> getUser(session, chatId).isPresent());
        } catch (Exception e) {
            logger.error("Ошибка при проверке существования пользователя chatId={}", chatId, e);
            return false;
        }
    }
}
