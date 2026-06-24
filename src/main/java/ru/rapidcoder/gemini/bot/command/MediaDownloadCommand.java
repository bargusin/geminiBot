package ru.rapidcoder.gemini.bot.command;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import ru.rapidcoder.gemini.bot.Bot;
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

public abstract class MediaDownloadCommand extends AbstractCommand {

    public MediaDownloadCommand(Bot bot, String identifier, String description) {
        super(bot, identifier, description);
    }

    protected abstract String getStoragePath(Long chatId) throws IOException;

    protected abstract String getArchiveFileName();

    protected abstract String getListCommandId();

    protected abstract String getListButtonLabel();

    protected abstract String getEmptyMessage();

    protected abstract String getSuccessMessage();

    protected abstract String getReadErrorMessage();

    @Override
    public void execute(Update update) {
        Long chatId = getChatId(update);

        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        keyboard.setKeyboard(List.of(
                List.of(new KeyboardButton(getListButtonLabel(), getListCommandId())),
                List.of(InterfaceFactory.createButton("🏠 Главное меню", CommandId.BACK_TO_MAIN))
        ));

        List<Path> files;
        try (Stream<Path> paths = Files.list(Paths.get(getStoragePath(chatId)))) {
            files = paths.filter(Files::isRegularFile).toList();
        } catch (IOException e) {
            logger.error("Ошибка при чтении списка файлов: {}", e.getMessage(), e);
            processMessage(update, getReadErrorMessage(), keyboard);
            return;
        }

        if (files.isEmpty()) {
            processMessage(update, getEmptyMessage(), keyboard);
            return;
        }

        String archiveFileName = getArchiveFileName();
        try {
            Path archivePath = Paths.get(getStoragePath(chatId), archiveFileName);
            try (OutputStream fos = Files.newOutputStream(archivePath);
                 ZipOutputStream zos = new ZipOutputStream(fos)) {
                for (Path file : files) {
                    if (file.getFileName().toString().equals(archiveFileName)) continue;
                    zos.putNextEntry(new ZipEntry(file.getFileName().toString()));
                    Files.copy(file, zos);
                    zos.closeEntry();
                }
            }
            bot.execute(bot.sendDocument(chatId, archivePath.toString()));
            Files.deleteIfExists(archivePath);
            processMessage(update, getSuccessMessage(), keyboard);
        } catch (Exception e) {
            logger.error("Ошибка при создании архива: {}", e.getMessage(), e);
            processMessage(update, "🚫 Не удалось создать архив.", keyboard);
        }
    }
}
