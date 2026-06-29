package ru.rapidcoder.gemini.bot.command.settings;

import org.telegram.telegrambots.meta.api.objects.Update;
import ru.rapidcoder.gemini.bot.Bot;

public class ChangeGeminiApiKeyIdCommand extends ChangeValueCommand {

    public ChangeGeminiApiKeyIdCommand(Bot bot, String identifier, String description) {
        super(bot, identifier, description);
    }

    @Override
    public void execute(Update update) {
        changeValue("gemini_api_key_id", update);
    }

    @Override
    void updateValue(Update update) throws RuntimeException {
        userRepository.updateGeminiApiKeyId(
                getChatId(update), encryptionService.encrypt(update.getMessage().getText()));
    }
}
