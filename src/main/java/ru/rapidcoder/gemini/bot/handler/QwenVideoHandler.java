package ru.rapidcoder.gemini.bot.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class QwenVideoHandler {

    private static final String[] API_ENDPOINTS = {
            "https://dashscope.aliyuncs.com/api/v1/services/aigc/video-generation/generation",
            "https://dashscope.aliyuncs.com/compatible-mode/v1/video/generations",
            "https://dashscope-intl.aliyuncs.com/api/v1/services/aigc/video-generation/generation"
    };

    private final String apiKey;
    private final OkHttpClient client;
    private final ObjectMapper objectMapper;

    public QwenVideoHandler(String apiKey) {
        this.apiKey = apiKey;

        // Создаем клиент с таймаутами
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();

        this.objectMapper = new ObjectMapper();
    }

    public static void main(String[] args) {
        try {
            // Получаем API ключ из переменных окружения или конфига
            String apiKey = "";
            if (apiKey == null || apiKey.trim().isEmpty()) {
                // Альтернативные способы получения ключа
                apiKey = System.getProperty("qwen.api.key");

                if (apiKey == null) {
                    // Чтение из файла
                    try (BufferedReader reader = new BufferedReader(new FileReader("api_key.txt"))) {
                        apiKey = reader.readLine();
                    } catch (Exception e) {
                        System.err.println("API ключ не найден. Установите одним из способов:");
                        System.err.println("1. Переменная окружения: QWEN_API_KEY");
                        System.err.println("2. Параметр JVM: -Dqwen.api.key=ВАШ_КЛЮЧ");
                        System.err.println("3. Файл api_key.txt в корне проекта");
                        System.exit(1);
                    }
                }
            }

            System.out.println("=== Тестирование Qwen Video API ===");
            QwenVideoHandler handler = new QwenVideoHandler(apiKey);

            // 1. Тестируем API ключ
            boolean keyValid = handler.testApiKey();
            if (!keyValid) {
                System.err.println("\nПроверьте следующее:");
                System.err.println("1. Ключ должен начинаться с 'sk-'");
                System.err.println("2. Ключ должен быть активен в консоли DashScope");
                System.err.println("3. Должны быть включены сервисы генерации видео");
                System.err.println("4. Должны быть доступные квоты");
                System.exit(1);
            }

            // 2. Генерируем видео
            System.out.println("\n=== Генерация видео ===");
            Scanner scanner = new Scanner(System.in);
            System.out.println("Введите промпт для видео:");
            String prompt = scanner.nextLine();

            String taskId = handler.generateVideo(prompt);
            System.out.println("Задача создана. Task ID: " + taskId);

            // 3. Проверяем статус
            System.out.println("\n=== Проверка статуса задачи ===");
            int maxAttempts = 60; // 60 попыток (5 минут при интервале 5 секунд)
            int attempt = 0;

            while (attempt < maxAttempts) {
                attempt++;
                System.out.println("Попытка " + attempt + "/" + maxAttempts);

                Map<String, Object> status = handler.checkTaskStatus(taskId);
                String currentStatus = (String) status.get("status");
                System.out.println("Статус: " + currentStatus);

                if ("SUCCEEDED".equals(currentStatus)) {
                    System.out.println("Видео успешно сгенерировано!");
                    String videoUrl = (String) status.get("video_url");
                    System.out.println("URL видео: " + videoUrl);

                    // Скачивание видео
                    // handler.downloadVideo(videoUrl, "video_" + System.currentTimeMillis() + ".mp4");
                    break;
                } else if ("FAILED".equals(currentStatus)) {
                    System.out.println("Ошибка генерации видео. Полный ответ:");
                    System.out.println(status.get("raw_response"));
                    break;
                } else if ("RUNNING".equals(currentStatus)) {
                    System.out.println("Задача выполняется...");
                    Thread.sleep(5000); // Ждем 5 секунд
                } else {
                    System.out.println("Неизвестный статус. Ждем...");
                    Thread.sleep(5000);
                }
            }

            if (attempt >= maxAttempts) {
                System.out.println("Превышено время ожидания завершения задачи");
            }

            scanner.close();

        } catch (Exception e) {
            System.err.println("Критическая ошибка:");
            e.printStackTrace();
        }
    }

    /**
     * Генерация видео с детальной отладкой
     */
    public String generateVideo(String prompt) throws IOException {
        for (String apiUrl : API_ENDPOINTS) {
            try {
                System.out.println("Пробую endpoint: " + apiUrl);
                return generateVideoWithEndpoint(apiUrl, prompt);
            } catch (IOException e) {
                System.err.println("Ошибка с endpoint " + apiUrl + ": " + e.getMessage());
                // Продолжаем пробовать следующий endpoint
            }
        }
        throw new IOException("Все endpoints вернули ошибку");
    }

    private String generateVideoWithEndpoint(String apiUrl, String prompt) throws IOException {
        // 1. Подготовка JSON запроса
        Map<String, Object> requestBody = new HashMap<>();
        Map<String, Object> input = new HashMap<>();
        Map<String, Object> parameters = new HashMap<>();

        input.put("prompt", prompt);

        // Параметры генерации (можно настроить)
        parameters.put("duration", 5);
        parameters.put("resolution", "1024x576");
        parameters.put("fps", 30);
        parameters.put("seed", new Random().nextInt(10000)); // Для воспроизводимости

        requestBody.put("model", "stable-video-diffusion-img2vid-xt-1-1");
        requestBody.put("input", input);
        requestBody.put("parameters", parameters);

        String jsonBody = objectMapper.writeValueAsString(requestBody);

        System.out.println("Отправляемый JSON:");
        System.out.println(jsonBody);
        System.out.println("Длина API ключа: " + (apiKey != null ? apiKey.length() : 0));
        System.out.println("Первые 10 символов ключа: " +
                (apiKey != null && apiKey.length() >= 10 ?
                        apiKey.substring(0, 10) + "..." : "N/A"));

        // 2. Создание HTTP запроса
        RequestBody body = RequestBody.create(
                jsonBody,
                MediaType.parse("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url(apiUrl)
                .post(body)
                .addHeader("Authorization", "Bearer " + apiKey.trim()) // Убедитесь в пробелах!
                .addHeader("Content-Type", "application/json")
                .addHeader("X-DashScope-Async", "enable") // Важно для асинхронных задач
                .build();

        // 3. Логируем заголовки запроса (без полного ключа)
        System.out.println("\nЗаголовки запроса:");
        Headers headers = request.headers();
        for (String name : headers.names()) {
            String value = headers.get(name);
            if ("Authorization".equals(name)) {
                value = value.substring(0, Math.min(value.length(), 30)) + "...";
            }
            System.out.println(name + ": " + value);
        }

        // 4. Выполнение запроса
        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body() != null ? response.body().string() : "";

            System.out.println("\nОтвет API:");
            System.out.println("Код: " + response.code());
            System.out.println("Сообщение: " + response.message());
            System.out.println("Заголовки ответа: " + response.headers());
            System.out.println("Тело ответа: " + responseBody);

            if (!response.isSuccessful()) {
                // Пытаемся извлечь детальную информацию об ошибке
                try {
                    JsonNode errorNode = objectMapper.readTree(responseBody);
                    if (errorNode.has("code")) {
                        throw new IOException("Ошибка API: код=" + errorNode.get("code").asText() +
                                ", сообщение=" + errorNode.get("message").asText());
                    }
                } catch (Exception e) {
                    // Если не удалось распарсить JSON с ошибкой
                }

                throw new IOException("Ошибка API: " + response.code() + " - " +
                        response.message() + "\nПолный ответ: " + responseBody);
            }

            // 5. Парсинг успешного ответа
            JsonNode rootNode = objectMapper.readTree(responseBody);
            JsonNode outputNode = rootNode.path("output");

            if (!outputNode.isMissingNode() && outputNode.has("task_id")) {
                String taskId = outputNode.get("task_id").asText();
                System.out.println("Получен Task ID: " + taskId);
                return taskId;
            } else {
                throw new IOException("Не удалось извлечь task_id из ответа: " + responseBody);
            }
        }
    }

    /**
     * Проверка статуса задачи с улучшенной обработкой
     */
    public Map<String, Object> checkTaskStatus(String taskId) throws IOException {
        // Формируем URL для проверки статуса
        String statusUrl = "https://dashscope.aliyuncs.com/api/v1/tasks/" + taskId;

        Request request = new Request.Builder()
                .url(statusUrl)
                .get()
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("X-DashScope-Async", "enable")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Ошибка проверки статуса: " + response.code() +
                        " - " + response.message());
            }

            String responseBody = response.body().string();
            JsonNode rootNode = objectMapper.readTree(responseBody);

            Map<String, Object> result = new HashMap<>();
            result.put("status", rootNode.path("status").asText());
            result.put("raw_response", responseBody);

            if (rootNode.has("output")) {
                JsonNode output = rootNode.path("output");
                if (output.has("video_url")) {
                    result.put("video_url", output.get("video_url").asText());
                }
                if (output.has("task_status")) {
                    result.put("task_status", output.get("task_status").asText());
                }
            }

            return result;
        }
    }

    /**
     * Утилита для проверки API ключа
     */
    public boolean testApiKey() throws IOException {
        // Используем простой запрос для проверки ключа
        String testUrl = "https://dashscope.aliyuncs.com/api/v1/tasks";

        Request request = new Request.Builder()
                .url(testUrl)
                .get()
                .addHeader("Authorization", "Bearer " + apiKey)
                .build();

        try (Response response = client.newCall(request).execute()) {
            System.out.println("Тест API ключа:");
            System.out.println("Код ответа: " + response.code());
            System.out.println("Сообщение: " + response.message());

            if (response.code() == 200) {
                System.out.println("API ключ валиден!");
                return true;
            } else {
                System.out.println("API ключ невалиден или нет доступа");
                return false;
            }
        }
    }
}
