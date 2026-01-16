package ru.rapidcoder.gemini.bot.handler;

import com.alibaba.dashscope.aigc.videosynthesis.VideoSynthesis;
import com.alibaba.dashscope.aigc.videosynthesis.VideoSynthesisParam;
import com.alibaba.dashscope.aigc.videosynthesis.VideoSynthesisResult;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.utils.Constants;

import java.util.HashMap;
import java.util.Map;

public class QwenVideoGenerator {

    public static void main(String[] args) {
        // Установите ваш API ключ
        Constants.apiKey = "";
        Constants.baseHttpApiUrl = "https://dashscope-intl.aliyuncs.com/api/v1";

        VideoSynthesis vs = new VideoSynthesis();

        Map<String, Object> extraParams = new HashMap<>();
        extraParams.put("resolution", "720P");
        extraParams.put("duration", 10);
        extraParams.put("prompt_extend", true);

        // Параметры генерации
        VideoSynthesisParam param = VideoSynthesisParam.builder()
                //.model("wan2.2-i2v-flash")
                .model("wan2.6-t2v")
                .prompt("Лесная поляна зимой")
                //.imgUrl("https://cdn.translate.alibaba.com/r/wanx-demo-1.png")
                .parameters(extraParams)
                .build();

        try {
            // 1. Отправка задачи на генерацию
            VideoSynthesisResult result = vs.asyncCall(param);
            String taskId = result.getOutput().getTaskId();
            System.out.println("Задача создана. ID: " + taskId);

            // 2. Опрос статуса задачи (Polling)
            while (true) {
                result = vs.fetch(taskId, null);
                String status = result.getOutput().getTaskStatus();

                if ("SUCCEEDED".equals(status)) {
                    System.out.println("Видео успешно создано!");
                    System.out.println("Ссылка на видео: " + result.getOutput().getVideoUrl());
                    break;
                } else if ("FAILED".equals(status)) {
                    System.err.println("Ошибка генерации: " + result.toString());
                    break;
                } else {
                    System.out.println("Статус задачи: " + status + "...");
                    Thread.sleep(5000); // Ждем 5 секунд перед следующей проверкой
                }
            }

        } catch (ApiException |
                 NoApiKeyException |
                 InterruptedException e) {
            e.printStackTrace();
        } catch (InputRequiredException e) {
            throw new RuntimeException(e);
        }
    }
}
