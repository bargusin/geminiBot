package ru.rapidcoder.gemini.bot.handler;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.bytedeco.ffmpeg.global.avcodec.AV_CODEC_ID_H264;

public class VideoMerger {

    public static void main(String[] args) throws Exception {
        List<String> files = new ArrayList<>();
        try (Stream<Path> paths = Files.list(Paths.get("./video"))) {
            paths.forEach(path -> {
                System.out.println("Файл: " + path);
                if (Files.isRegularFile(path)) {
                    files.add(path.getFileName().toString());
                }
            });
        }

        String outputFile = "./merged_output.mp4";

        try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFile, 1920, 1080)) {
            recorder.setVideoCodec(AV_CODEC_ID_H264);
            recorder.setFrameRate(30);
            recorder.start();

            for (String inputFile : files) {
                try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(inputFile)) {
                    grabber.start();
                    Frame frame;
                    while ((frame = grabber.grabFrame()) != null) {
                        recorder.record(frame);
                    }
                    grabber.stop();
                }
            }
            recorder.stop();
        }
        System.out.println("Видео успешно объединены в: " + outputFile);
    }
}
