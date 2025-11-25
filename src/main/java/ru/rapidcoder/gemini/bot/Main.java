package ru.rapidcoder.gemini.bot;

import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;

public class Main {

    public static void main(String[] args) throws IOException {
        generatePicture();
    }

    private static void generateText() {
        Client client = Client.builder()
                .apiKey("AIzaSyCM5i9SOifRLoshVqL0VR1kYvYm3OWzAt8")
                .build();

        GenerateContentConfig config = GenerateContentConfig.builder()
                .systemInstruction(Content.fromParts(Part.fromText("You are a cat. Your name is Neko.")))
                .build();

        GenerateContentResponse response = client.models.generateContent("gemini-2.5-flash", "Explain how AI works in a few words", config);

        System.out.println(response.text());
    }

    private static void generatePicture() throws IOException {
        try (Client client = Client.builder()
                .apiKey("AIzaSyCM5i9SOifRLoshVqL0VR1kYvYm3OWzAt8")
                .build();) {
            GenerateContentConfig config = GenerateContentConfig.builder()
                    .responseModalities(Collections.singletonList("IMAGE"))
                    .build();

            GenerateContentResponse response = client.models.generateContent("gemini-2.5-flash-image", "Сгенерируй картинку с лучшими друзьями на рыбалке", config);

            for (Part part : response.parts()) {
                if (part.text()
                        .isPresent()) {
                    System.out.println(part.text()
                            .get());
                } else if (part.inlineData()
                        .isPresent()) {
                    var blob = part.inlineData()
                            .get();
                    if (blob.data()
                            .isPresent()) {
                        Files.write(Paths.get("_01_generated_image.png"), blob.data()
                                .get());
                    }
                }
            }
        }
    }

}
