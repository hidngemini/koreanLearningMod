package com.hidngemini.speakit.client;

import java.util.concurrent.CompletableFuture;

public class TTSManager {
    // TODO: lang int could be an enum?
    public static void speak(String text, SpeakItLangauge lang) {
        switch (lang) {
            case SpeakItLangauge.KOREAN:
                speakKorean(text);
                break;
        }
    }

    public static void speakKorean(String hangulText) {
        CompletableFuture.runAsync(() -> {
            try {
                String os = System.getProperty("os.name").toLowerCase();
                ProcessBuilder pb;

                if (os.contains("mac")) { // macOS uses Yuna
                    pb = new ProcessBuilder("say", "-v", "Yuna", hangulText);
                } else if (os.contains("win")) {
                    // TODO: windows tts
                    System.err.println("[SpeakIt] TTS unsupported on Windows rn");
                    return;
                } else {
                    // TODO: linux tts
                    System.err.println("[SpeakIt] TTS unsupported on Linux rn");
                    return;
                }
                
                // run the command to synthesize voice
                Process process = pb.start();
                process.waitFor();
            } catch (Exception e) {
                System.err.println("[SpeakIt] Failed to synthesize speech:");
                e.printStackTrace();
            }
        });
    }
}
