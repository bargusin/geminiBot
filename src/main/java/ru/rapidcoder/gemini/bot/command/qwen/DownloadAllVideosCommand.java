package ru.rapidcoder.gemini.bot.command.qwen;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import ru.rapidcoder.gemini.bot.Bot;
import ru.rapidcoder.gemini.bot.command.AbstractCommand;
import ru.rapidcoder.gemini.bot.component.InterfaceFactory;
import ru.rapidcoder.gemini.bot.component.KeyboardButton;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class DownloadAllVideosCommand extends AbstractCommand {

    public DownloadAllVideosCommand(Bot bot, String identifier, String description) {
        super(bot, identifier, description);
    }

    @Override
    public void execute(Update update) {
        Long chatId = getChatId(update);

        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        keyboard.setKeyboard(List.of(
                List.of(new KeyboardButton("🎬 Сгенерированные видео", "menu_generated_videos")),
                List.of(InterfaceFactory.createButton("🏠 Главное меню", "back_to_main"))
        ));

        List<Path> files;
        try (Stream<Path> paths = Files.list(Paths.get(bot.getStorageVideosPath(chatId)))) {
            files = paths.filter(Files::isRegularFile).toList();
        } catch (IOException e) {
            logger.error("Ошибка при чтении списка файлов: {}", e.getMessage(), e);
            processMessage(update, "🚫 Не удалось прочитать список видео.", keyboard);
            return;
        }

        if (files.isEmpty()) {
            processMessage(update, "📭 Нет сохранённых видео для выгрузки.", keyboard);
            return;
        }

        try {
            Path archivePath = Paths.get(bot.getStorageVideosPath(chatId), "videos_archive.zip");
            try (OutputStream fos = Files.newOutputStream(archivePath);
                 ZipOutputStream zos = new ZipOutputStream(fos)) {
                for (Path file : files) {
                    if (file.getFileName().toString().equals("videos_archive.zip")) continue;
                    zos.putNextEntry(new ZipEntry(file.getFileName().toString()));
                    Files.copy(file, zos);
                    zos.closeEntry();
                }
            }
            bot.execute(bot.sendDocument(chatId, archivePath.toString()));
            Files.deleteIfExists(archivePath);
            processMessage(update, "✅ Архив с видео отправлен.", keyboard);
        } catch (Exception e) {
            logger.error("Ошибка при создании архива: {}", e.getMessage(), e);
            processMessage(update, "🚫 Не удалось создать архив.", keyboard);
        }
    }
}
