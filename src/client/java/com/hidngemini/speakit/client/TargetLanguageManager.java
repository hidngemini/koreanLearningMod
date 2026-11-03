package com.hidngemini.speakit.client;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class TargetLanguageManager {
    private static final Gson GSON = new Gson();
    public static Map<String, String> targetLanguageMap = new HashMap<>();
    private static boolean isLoaded = false;
    private static String localeCode;

    public static void loadTargetLanguage(String localeCode) {
        if (isLoaded) return;
        
        try {
            Minecraft client = Minecraft.getInstance();
            if (client == null || client.getResourceManager() == null) {
                return; // Guard against early calls before client is ready
            }

            Identifier fileId = Identifier.fromNamespaceAndPath("minecraft", "lang/" + localeCode.toLowerCase() + ".json");
            
            try (InputStream stream = client.getResourceManager().open(fileId);
                 InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                
                targetLanguageMap = GSON.fromJson(reader, new TypeToken<>(){}.getType());
                isLoaded = true;
                System.out.println("[SpeakIt] SUCCESS! Loaded " + targetLanguageMap.size() + " translation keys for " + localeCode);
            }
        } catch (Exception e) {
            System.err.println("[SpeakIt] FAILED to load language file:");
            e.printStackTrace();
        }
    }

    public static String getTranslation(String localeCode, String translationKey) {
        if (!isLoaded || !localeCode.equals(TargetLanguageManager.localeCode)) {
            TargetLanguageManager.localeCode = localeCode;
            loadTargetLanguage(localeCode);
        }
        return targetLanguageMap.getOrDefault(translationKey, translationKey);
    }
}