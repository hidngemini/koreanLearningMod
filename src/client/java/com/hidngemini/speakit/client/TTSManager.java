package com.hidngemini.speakit.client;

import java.util.concurrent.CompletableFuture;

public class TTSManager {
    
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
                    String safeText = hangulText.replace("'", "''"); // prevent weird crashes

                    // powershell command for tts
                    String psCommand = "Add-Type -AssemblyName System.Speech; " +
                                       "$s = New-Object System.Speech.Synthesis.SpeechSynthesizer; " +
                                       "$s.SelectVoiceByHints([System.Speech.Synthesis.VoiceGender]::Female, [System.Speech.Synthesis.VoiceAge]::Adult, 0, [System.Globalization.CultureInfo]::GetCultureInfo('ko-KR')); " +
                                       "$s.Speak('" + safeText + "');";

                    pb = new ProcessBuilder("powershell.exe", "-NoProfile", "-NonInteractive", "-Command", psCommand);
                } else {
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