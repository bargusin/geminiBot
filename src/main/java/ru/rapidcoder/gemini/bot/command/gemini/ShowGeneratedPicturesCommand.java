package ru.rapidcoder.gemini.bot.command.gemini;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.rapidcoder.gemini.bot.Bot;
import ru.rapidcoder.gemini.bot.command.AbstractCommand;
import ru.rapidcoder.gemini.bot.component.InterfaceFactory;
import ru.rapidcoder.gemini.bot.component.KeyboardButton;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class ShowGeneratedPicturesCommand extends AbstractCommand {

    public ShowGeneratedPicturesCommand(Bot bot, String identifier, String description) {
        super(bot, identifier, description);
    }

    @Override
    public void execute(Update update) {
        String text = InterfaceFactory.format("\uD83D\uDDBC\uFE0F <b>Список сгенерированных изображений</b>");
        List<String> files;
        try (Stream<Path> paths = Files.list(Paths.get(bot.getStoragePicturesPath(getChatId(update))))) {
            files = paths
                    .filter(Files::isRegularFile)
                    .sorted((p1, p2) -> {
                        try {
                            return Files.getLastModifiedTime(p2).compareTo(Files.getLastModifiedTime(p1));
                        } catch (IOException e) {
                            return 0;
                        }
                    })
                    .limit(20)
                    .map(path -> path.getFileName().toString())
                    .toList();
        } catch (IOException e) {
            logger.error("Ошибка при чтении списка файлов: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }

        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        for (String fileName : files) {
            rows.add(List.of(new KeyboardButton(fileName, "picture#" + fileName)));
        }
        rows.add(List.of(new KeyboardButton("\uD83D\uDCE6 Скачать все изображения", "download_all_pictures")));
        rows.add(List.of(new KeyboardButton("\uD83C\uDFE0 Главное меню", "back_to_main")));
        keyboard.setKeyboard(rows);

        processMessage(update, text, keyboard);
    }
}
