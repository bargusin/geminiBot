package ru.rapidcoder.gemini.bot.command.qwen;

import com.alibaba.dashscope.aigc.videosynthesis.VideoSynthesis;
import com.alibaba.dashscope.aigc.videosynthesis.VideoSynthesisOutput;
import com.alibaba.dashscope.aigc.videosynthesis.VideoSynthesisParam;
import com.alibaba.dashscope.aigc.videosynthesis.VideoSynthesisResult;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.utils.Constants;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.rapidcoder.gemini.bot.Bot;
import ru.rapidcoder.gemini.bot.command.AbstractCommand;
import ru.rapidcoder.gemini.bot.component.InterfaceFactory;
import ru.rapidcoder.gemini.bot.handler.InputHandler;
import ru.rapidcoder.gemini.database.entity.User;
import ru.rapidcoder.gemini.database.repository.UserRepository;
import ru.rapidcoder.gemini.service.EncryptionService;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GeneratorVideoCommand extends AbstractCommand {

    private static final String IMAGE_URL_REGEX = "(https?://\\S+?\\.(?i)(?:png|jpe?g|gif|webp|bmp|tiff)(?:\\?\\S+)?)(?=[.,;!\\s]|$)";
    private static final int MAX_NETWORK_RETRIES = 5;
    protected final UserRepository userRepository;
    protected final EncryptionService encryptionService;
    private final ExecutorService videoExecutor = Executors.newFixedThreadPool(10);

    public GeneratorVideoCommand(Bot bot, String identifier, String description) {
        super(bot, identifier, description);

        this.userRepository = new UserRepository(bot.getDatabaseManager());
        this.encryptionService = new EncryptionService(bot.getSettings().getEncryptedKey());
    }

    @Override
    public void execute(Update update) {
        Long chatId = getChatId(update);

        InlineKeyboardMarkup cancelKeyboard = new InlineKeyboardMarkup();
        cancelKeyboard.setKeyboard(List.of(
                List.of(InterfaceFactory.createButton("❌ Отмена", "back_to_main"))
        ));
        processMessage(update, "Введите описание для генерации видео:", cancelKeyboard);

        userStateService.setInputHandler(chatId, new InputHandler() {
            @Override
            public boolean handleInput(Update update) {
                Message statusMessage = bot.sendMessage(chatId, "\uD83C\uDFAC Начинаю генерацию видео...", null);

                InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
                List<List<InlineKeyboardButton>> rows = new ArrayList<>();

                rows.add(List.of(InterfaceFactory.createButton("\uD83C\uDFE0 Главное меню", "back_to_main")));
                keyboard.setKeyboard(rows);
                Constants.apiKey = encryptionService.decrypt(checkQwenApiKeyIdQwenApiKeyId(update));

                Message message = update.getMessage();
                String prompt;
                String imageUrl;
                if (message.hasPhoto()) {
                    prompt = message.getCaption() != null ? message.getCaption() : "Оживи это изображение";
                    imageUrl = getTelegramFileUrl(chatId, message.getPhoto());
                } else if (message.hasText()) {
                    ParsedData data = parseText(message.getText());
                    prompt = data.prompt;
                    imageUrl = data.imageUrl;
                } else {
                    prompt = "";
                    imageUrl = null;
                }
                logger.info("Prompt: {}", prompt);
                logger.info("ImageUrl: {}", imageUrl);

                Instant startTime = Instant.now();

                CompletableFuture<VideoSynthesisOutput> videoFuture = getVideoSynthesisOutputCompletableFuture(keyboard, startTime, chatId);

                CompletableFuture.runAsync(() -> {
                    int networkErrorsCount = 0;
                    try {
                        Constants.baseHttpApiUrl = "https://dashscope-intl.aliyuncs.com/api/v1";

                        VideoSynthesis vs = new VideoSynthesis();

                        Map<String, Object> extraParams = new HashMap<>();
                        extraParams.put("resolution", "720P");
                        extraParams.put("duration", 15);
                        extraParams.put("prompt_extend", true);

                        VideoSynthesisParam param = VideoSynthesisParam.builder()
                                .model(!StringUtils.isEmpty(imageUrl) ? "wan2.6-i2v-flash" : "wan2.6-t2v") // Модель для Image-to-Video
                                .prompt(prompt)
                                //.parameters(extraParams)
                                .build();

                        if (!StringUtils.isEmpty(imageUrl)) {
                            param.setImgUrl(imageUrl);
                        }

                        VideoSynthesisResult result = vs.asyncCall(param);
                        String taskId = result.getOutput().getTaskId();
                        logger.info("Задача на генерацию видео создана: {}", taskId);

                        while (!videoFuture.isDone()) {
                            Thread.sleep(5000); // Пауза 5 сек перед опросом
                            try {
                                result = vs.fetch(taskId, null);
                                String status = result.getOutput().getTaskStatus();
                                networkErrorsCount = 0;
                                if ("SUCCEEDED".equals(status)) {
                                    logger.info("Ссылка на сгенерированное видео: {}", result.getOutput().getVideoUrl());
                                    videoFuture.complete(result.getOutput());
                                    break;
                                } else if ("FAILED".equals(status)) {
                                    logger.error(result.getMessage());
                                    videoFuture.completeExceptionally(new RuntimeException(result.getMessage() != null ? result.getMessage() : result.getCode()));
                                    break;
                                } else {
                                    String timeStr = formatDuration(Duration.between(startTime, Instant.now()));
                                    bot.updateMessage(chatId, statusMessage.getMessageId(), String.format("⏳ Генерирую видео...%nПрошло времени: %s%nСтатус: %s", timeStr, status), null);
                                }
                            } catch (Exception e) {
                                networkErrorsCount++;
                                logger.warn("Сбой сети ({}/{}) при опросе статуса: {}", networkErrorsCount, MAX_NETWORK_RETRIES, e.getMessage());
                                if (networkErrorsCount >= MAX_NETWORK_RETRIES) {
                                    throw new RuntimeException("Сбой сети при опросе");
                                }
                            }
                        }
                        bot.sendMessage(chatId, "✅ Генерация видео успешно завершена", keyboard);
                    } catch (ApiException e) {
                        logger.error(String.format("[%s] %s", e.getStatus().getCode(), e.getStatus().getMessage()), e);
                        videoFuture.completeExceptionally(new RuntimeException(String.format("[%s] %s", e.getStatus().getCode(), e.getStatus().getMessage())));
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                        videoFuture.completeExceptionally(e);
                    }
                }, videoExecutor);

                return true;
            }
        });
    }

    @NotNull
    private CompletableFuture<VideoSynthesisOutput> getVideoSynthesisOutputCompletableFuture(InlineKeyboardMarkup keyboard, Instant startTime, Long chatId) {
        CompletableFuture<VideoSynthesisOutput> videoFuture = new CompletableFuture<>();
        videoFuture.whenComplete((output, throwable) -> {
            if (throwable != null) {
                logger.error("Ошибка в цепочке генерации для chatId: {}", chatId, throwable);
                bot.sendMessage(chatId, "\uD83D\uDEAB Не удалось сгенерировать видео. " + throwable.getMessage(), keyboard);
            } else {
                try {
                    String totalTime = formatDuration(Duration.between(startTime, Instant.now()));
                    finishTask(chatId, output, totalTime);
                } catch (Exception e) {
                    logger.error("Ошибка при финальной обработке видео (finishTask):", e);
                    bot.sendMessage(chatId, "\uD83D\uDEAB Видео сгенерировано, но не смогло быть отправлено.", null);
                }
            }
        });
        return videoFuture;
    }

    private void finishTask(long chatId, VideoSynthesisOutput output, String totalTime) throws Exception {
        bot.sendMessage(chatId, "✅ Видео готово", null);
        Path outputPath = Paths.get(bot.getStorageVideosPath(chatId), output.getTaskId() + ".mp4");
        try (InputStream in = new URL(output.getVideoUrl()).openStream()) {
            Files.copy(in, outputPath, StandardCopyOption.REPLACE_EXISTING);
            logger.info("Видео сохранено локально: {}", outputPath);
        }
        try {
            java.io.File fileToSend = outputPath.toFile();
            if (!fileToSend.exists()) {
                throw new FileNotFoundException("Файл не найден по пути: " + outputPath);
            }
            SendVideo video = SendVideo.builder()
                    .chatId(chatId)
                    .video(new InputFile(fileToSend, outputPath.getFileName().toString()))
                    .caption(String.format("✅ Результат генерации: ⏱ Время: %s", totalTime))
                    .build();
            bot.execute(video);
            logger.info("Видео из файла успешно отправлено в чат {}", chatId);
        } catch (Exception e) {
            logger.error("Критическая ошибка при отправке файла в Telegram", e);
            throw new IllegalStateException(e);
        }
    }

    private String checkQwenApiKeyIdQwenApiKeyId(Update update) throws IllegalArgumentException {
        Optional<User> user = userRepository.findByChatId(getChatId(update));
        if (user.isPresent()) {
            if (StringUtils.isEmpty(user.get().getQwenApiKeyId())) {
                throw new IllegalArgumentException("Ключ qwen_api_key_id не задан.");
            } else {
                return user.get().getQwenApiKeyId();
            }
        } else {
            throw new IllegalArgumentException("Не удалось найти пользователя в базе данных.");
        }
    }

    private String getTelegramFileUrl(Long chatId, List<PhotoSize> photos) {
        PhotoSize largest = photos.stream().max(Comparator.comparing(PhotoSize::getFileSize)).orElse(null);
        if (largest == null)
            return null;
        try {
            org.telegram.telegrambots.meta.api.objects.File file = bot.execute(new GetFile(largest.getFileId()));
            String fileUrl = "https://api.telegram.org/file/bot" + bot.getBotToken() + "/" + file.getFilePath();
            Path outputPath = Paths.get(bot.getStorageUploadPath(chatId), file.getFileUniqueId());
            try (InputStream in = new URL(fileUrl).openStream()) {
                Files.copy(in, outputPath, StandardCopyOption.REPLACE_EXISTING);
                logger.info("Файл для генерации видео сохранен: {}", outputPath);
            }
            return fileUrl;
        } catch (Exception e) {
            return null;
        }
    }

    private ParsedData parseText(String text) {
        Pattern pattern = Pattern.compile(IMAGE_URL_REGEX);
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            String url = matcher.group(1);
            return new ParsedData(url, text.replace(url, "").trim());
        }
        return new ParsedData(null, text);
    }

    private String formatDuration(Duration d) {
        long s = d.getSeconds();
        return (s < 60) ? s + " сек." : (s / 60) + " мин. " + (s % 60) + " сек.";
    }

    record ParsedData(String imageUrl,
                      String prompt) {
    }
}
