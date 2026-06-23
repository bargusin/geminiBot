package ru.rapidcoder.gemini.bot.command.qwen;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.rapidcoder.gemini.bot.Bot;
import ru.rapidcoder.gemini.bot.command.AbstractCommand;
import ru.rapidcoder.gemini.bot.command.CommandId;
import ru.rapidcoder.gemini.bot.component.InterfaceFactory;
import ru.rapidcoder.gemini.bot.component.KeyboardButton;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class ShowGeneratedVideosCommand extends AbstractCommand {

    public ShowGeneratedVideosCommand(Bot bot, String identifier, String description) {
        super(bot, identifier, description);
    }

    @Override
    public void execute(Update update) {
        String text = InterfaceFactory.format("🎬 <b>Список сгенерированных видео</b>");

        List<String> files;
        try (Stream<Path> paths = Files.list(Paths.get(bot.getStorageVideosPath(getChatId(update))))) {
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
            rows.add(List.of(new KeyboardButton(fileName, CommandId.SHOW_VIDEO + "#" + fileName)));
        }
        rows.add(List.of(new KeyboardButton("📦 Скачать все видео", CommandId.DOWNLOAD_ALL_VIDEOS)));
        rows.add(List.of(new KeyboardButton("🏠 Главное меню", CommandId.BACK_TO_MAIN)));
        keyboard.setKeyboard(rows);

        processMessage(update, text, keyboard);
    }
}
