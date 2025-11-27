package ru.rapidcoder.gemini.bot.handler;

import com.google.genai.Client;
import com.google.genai.types.Blob;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Objects;

public class GeminiHandler {

    private static final Logger logger = LoggerFactory.getLogger(GeminiHandler.class);
    private static final String GEMINI_MODEL = "gemini-2.5-flash-image";
    private final Client client;

    public GeminiHandler(String geminiApiKey) {
        client = Client.builder()
                .apiKey(geminiApiKey)
                .build();
    }

    public byte[] generatePicture(String prompt) {
        GenerateContentConfig config = GenerateContentConfig.builder()
                .responseModalities(Collections.singletonList("IMAGE"))
                .build();

        GenerateContentResponse response = client.models.generateContent(GEMINI_MODEL, prompt, config);

        for (Part part : Objects.requireNonNull(response.parts())) {
            if (part.text()
                    .isPresent()) {
                System.out.println(part.text()
                        .get());
            } else if (part.inlineData()
                    .isPresent()) {
                Blob blob = part.inlineData()
                        .get();
                if (blob.data()
                        .isPresent()) {
                    return blob.data()
                            .get();
                }
            }
        }
        return null;
    }
}
