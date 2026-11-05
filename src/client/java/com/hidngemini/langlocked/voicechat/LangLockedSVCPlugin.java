package com.hidngemini.langlocked.voicechat;

import java.io.File;

import org.vosk.Model;
import org.vosk.Recognizer;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hidngemini.langlocked.utils.BundledModelExtractor;

import de.maxhenkel.voicechat.api.VoicechatApi;
import de.maxhenkel.voicechat.api.VoicechatClientApi;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.events.ClientSoundEvent;
import de.maxhenkel.voicechat.api.events.ClientVoicechatInitializationEvent;
import de.maxhenkel.voicechat.api.events.EventRegistration;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class LangLockedSVCPlugin implements VoicechatPlugin {
    private static final int VC_SAMPLE_RATE = 48000;   // svc mic rate
    private static final int VOSK_SAMPLE_RATE = 16000; // vosk model rate
    private static final int RATIO = VC_SAMPLE_RATE / VOSK_SAMPLE_RATE;

    private static VoicechatClientApi clientApi;
    private static Model model;
    private static Recognizer recognizer;

    static {
        // set up proper encoding
        System.setProperty("jna.encoding", "UTF-8");
    }

    @Override
    public String getPluginId() {
        return "langlocked";
    }

    @Override
    public void initialize(VoicechatApi api) {
        try {
            File targetModelDir = new File("config/langlocked/vosk-model-small-ko-0.22");
            File modelDir = BundledModelExtractor.extractModelIfNeeded(
                "/assets/langlocked/vosk-model-small-ko-0.22",
                targetModelDir
            );

            if (modelDir.exists()) {
                model = new Model(modelDir.getAbsolutePath());
                recognizer = new Recognizer(model, VOSK_SAMPLE_RATE);
                System.out.println("[LangLocked] Vosk model loaded!");
            } else {
                System.err.println("[LangLocked] Vosk model couldn't load!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void registerEvents(EventRegistration registration) {
        registration.registerEvent(ClientVoicechatInitializationEvent.class, this::onVoiceChatInit, 100);
        registration.registerEvent(ClientSoundEvent.class, this::onClientSound);
    }

    private void onVoiceChatInit(ClientVoicechatInitializationEvent event) {
        clientApi = event.getVoicechat();
    }

    private void onClientSound(ClientSoundEvent event) {
        if (recognizer == null) return;

        short[] raw = event.getRawAudio();
        if (raw == null || raw.length == 0) return;

        short[] audio = downsample(raw);

        if (recognizer.acceptWaveForm(audio, audio.length)) {
            String text = extractText(recognizer.getResult());
            if (!text.isEmpty()) {
                System.out.println(text);
                sendToChat(text);
            }
        } else {
            // TODO: show partial on UI?
        }
    }

    // average each group of 3 to downsample
    private static short[] downsample(short[] in) {
        int outLen = in.length / RATIO;
        short[] out = new short[outLen];
        for (int i = 0; i < outLen; i++) {
            int base = i * RATIO;
            int sum = 0;
            for (int j = 0; j < RATIO; j++) sum += in[base + j];
            out[i] = (short) (sum / RATIO);
        }
        return out;
    }

    // json helpers
    private static String extractText(String json) {
        return extractField(json, "text");
    }

    private static String extractField(String json, String field) {
        if (json == null || json.isEmpty()) return "";
        try {
            JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
            return obj.has(field) ? obj.get(field).getAsString().trim() : "";
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private static void sendToChat(String hangulText) {
        Minecraft client = Minecraft.getInstance();
        if (client == null) return;
        client.execute(() -> {
            if (client.player != null) {
                client.player.sendSystemMessage(Component.literal("Recognized Korean: " + hangulText));
            }
        });
    }
}