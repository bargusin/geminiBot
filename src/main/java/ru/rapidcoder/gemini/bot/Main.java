package ru.rapidcoder.gemini.bot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import ru.rapidcoder.gemini.SettingsBot;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        try {
            String environment = System.getenv("botEnv") != null ? System.getenv("botEnv") : "dev";
            String botName = System.getenv(environment + "BotName");
            String tokenId = System.getenv(environment + "TokenId");
            String encryptedKey = System.getenv(environment + "EncryptedKey");
            String storagePicturesPath = System.getenv(environment + "StoragePicturesPath");
            String dataBaseFile = System.getenv(environment + "DataBaseFile");

            SettingsBot settings = new SettingsBot();
            settings.setBotName(botName);
            settings.setTokenId(tokenId);
            settings.setEncryptedKey(encryptedKey);
            settings.setStoragePicturesPath(storagePicturesPath);
            settings.setDataBaseFile(dataBaseFile);

            TelegramBotsApi telegramBotsApi = createTelegramBotsApi();
            Bot bot = createBot(settings);
            telegramBotsApi.registerBot(bot);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public static TelegramBotsApi createTelegramBotsApi() throws TelegramApiException {
        return new TelegramBotsApi(DefaultBotSession.class);
    }

    public static Bot createBot(SettingsBot settings) {
        return new Bot(settings);
    }

    private static List<Long> getAdmins(String environment) {
        return Optional.ofNullable(System.getenv(environment + "Admins"))
                .map(value -> {
                    if (value.trim()
                            .isEmpty()) {
                        throw new IllegalArgumentException("Admins list cannot be empty");
                    }
                    return Arrays.stream(value.split(","))
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .map(Long::parseLong)
                            .collect(Collectors.toList());
                })
                .orElseThrow(() -> new IllegalArgumentException("Role's model not defined in environment"));
    }
}
