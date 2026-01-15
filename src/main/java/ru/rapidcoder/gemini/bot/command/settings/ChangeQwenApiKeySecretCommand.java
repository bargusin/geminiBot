package ru.rapidcoder.gemini.bot.command.settings;

import org.telegram.telegrambots.meta.api.objects.Update;
import ru.rapidcoder.gemini.bot.Bot;

public class ChangeQwenApiKeySecretCommand extends ChangeValueCommand {

    public ChangeQwenApiKeySecretCommand(Bot bot, String identifier, String description) {
        super(bot, identifier, description);
    }

    @Override
    public void execute(Update update) {
        changeValue("qwen_api_key_secret", update);
    }

    @Override
    void updateValue(Update update) throws RuntimeException {
        userRepository.updateQwenApiKeySecret(getChatId(update), encryptionService.encrypt(update.getMessage().getText()));
    }
}
