package com.hidngemini.langlocked.client;

import java.util.HashSet;
import java.util.random.RandomGenerator;

import com.hidngemini.langlocked.event.PlayerItemPickupCallback;
import com.hidngemini.langlocked.utils.HangulPhoneticConverter;
import com.mojang.blaze3d.platform.InputConstants;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class LangLockedClient implements ClientModInitializer {

	public static final Identifier MY_HUD_ELEMENT = Identifier.fromNamespaceAndPath("speakit", "redwindow");
	private static final String localeCode = "ko_kr";
	private static final String languageName = "Korean";
	private static final long CHALLENGE_TIME_MS = 5_000;

	public static final HashSet<Item> obtainedItems = new HashSet<Item>();

	private static boolean isChallengeActive = false;
	private static long challengeStartTime = 0;

	public static ItemStack item;

	private int tickCounter = 0;

	// add a keybind!
	public static final KeyMapping TRANSLATE_KEY = new KeyMapping(
        "key.langlocked.speak", 
        InputConstants.Type.KEYBOARD, 
        InputConstants.KEYCODE_X,
        KeyMapping.Category.MISC
    );


	@Override
	public void onInitializeClient() {
		TargetLanguageManager.loadTargetLanguage(localeCode);

		// add HUD element
		HudElementRegistry.attachElementBefore(
            VanillaHudElements.CHAT, 
            MY_HUD_ELEMENT, 
            (graphics, deltaTracker) -> {
                renderPopup(graphics, deltaTracker);
            }
        );

		obtainedItems.add(Items.DIRT);

        // initialize keybind
        KeyMappingHelper.registerKeyMapping(TRANSLATE_KEY);

		PlayerItemPickupCallback.EVENT.register((player, stack, addedCount) -> {
            Minecraft client = Minecraft.getInstance();
            if (client.player != null && player.getUUID().equals(client.player.getUUID())) {
                // rescan inventory
				updateFromPlayerInventory(client);
            }
        });

		ClientTickEvents.START_CLIENT_TICK.register((Minecraft client) -> {
			// make sure in game and player exists
            if (client.player == null || client.level == null) {
                return;
            }

            tickCounter++;
            
            if (tickCounter >= 20) {
                tickCounter = 0;
                updateFromPlayerInventory(client);
            }
        });

		// add forcechal command
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(
                ClientCommands.literal("forcechal")
					.executes(context -> {
						triggerChallenge();
						return 1;
					})
            );
        });

		// add foundItems command
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(
                ClientCommands.literal("foundItems")
					.executes(context -> {
						FabricClientCommandSource source = context.getSource();

						StringBuilder sb = new StringBuilder();
						
						// iterate through items and add them to the sb
						for (Item item : obtainedItems) {
							ItemStack i = new ItemStack(item);
							sb.append(i.getItemName().getString());
							sb.append(" ");
						}

						source.sendFeedback(Component.literal(sb.toString()));
						return 1;
					})
            );
        });
	}

	private void updateFromPlayerInventory(Minecraft client) {
        // get players inventory
        var inventory = client.player.getInventory();

        // loop thru inventory
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack item = inventory.getItem(i);
            
            if (!item.isEmpty()) {
				obtainedItems.add(item.getItem());
            }
        }
    }

	public void triggerChallenge() {
		// setup an item
        Item randomElement = obtainedItems.stream()
            .skip(RandomGenerator.getDefault().nextInt(obtainedItems.size()))
            .findFirst()
            .orElse(null);

		item = new ItemStack(randomElement);

		// setup challange variables
		isChallengeActive = true;
		challengeStartTime = System.currentTimeMillis();

		// get item name in other language
		String translationKey = item.getItem().getDescriptionId();
		String otherLangName = TargetLanguageManager.getTranslation(localeCode, translationKey);

		// say it for the play
		TTSManager.speak(otherLangName, LangLockedLangauge.KOREAN);
	}

	private void renderPopup(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
		if (!isChallengeActive) return;
		
		long currentTime = System.currentTimeMillis();
		if (currentTime - challengeStartTime > CHALLENGE_TIME_MS + 2000) {
			isChallengeActive = false;
			return;
		}
		
		// time left
		float durAmt = (float) ((double) (currentTime - challengeStartTime)) / CHALLENGE_TIME_MS;

		// honestly just a bunch of constants that i should move later
		int rectWidth = 280;
		int rectHeight = 50;
		int barWidth = 3;
		int yPos = 20;
		int uiColour = 0x80101010;
		int progressColour = 0x8000ff00;
		int itemResolution = 16;
		float itemScaleFactor = 2.0f;
		float answerScaleFactor = 4.0f;
		
		// get size of screen
		int screenWidth = net.minecraft.client.Minecraft.getInstance().getWindow().getGuiScaledWidth();

		// find middle of the screen adjusted for the rectangle size
		int xPos = (screenWidth - rectWidth) / 2;

		// draw the UI box
        graphics.fill(xPos, yPos, xPos + rectWidth + barWidth, yPos + rectHeight, uiColour);

		// draw time left bar (if still going)
		if (durAmt < 1) {
			graphics.fill(xPos, yPos + rectHeight, xPos + Math.round(rectWidth * (1.0f - durAmt)), yPos + rectHeight+ barWidth, progressColour);
		} else {
			//TODO: make a correct or incorrect bool; different behaviour depending.

			// add a new scaling matrix to the stack
			graphics.pose().pushMatrix();
			graphics.pose().scale(4.0f, 4.0f);

			int barrierX = Math.round((xPos + ((rectWidth - itemResolution * answerScaleFactor) / 2)) / answerScaleFactor); // center barrier X horizontally
			int barrierY = Math.round((yPos + ((rectHeight - itemResolution * answerScaleFactor) / 2)) / answerScaleFactor); // center barrier X vertically

			// draw barrier
			graphics.item(new ItemStack(Items.BARRIER), barrierX, barrierY);

			// pop matrix off
			graphics.pose().popMatrix();
		}

		// add a new matrix to stack
		graphics.pose().pushMatrix();
		graphics.pose().scale(itemScaleFactor, itemScaleFactor);

		// compute where to draw the item
		int itemX = Math.round((xPos + 4) / itemScaleFactor); // Left padding inside the box
    	int itemY = Math.round((yPos + (rectHeight - itemResolution * itemScaleFactor) / 2) / itemScaleFactor); // center item vertically

		// draw the item
		graphics.item(LangLockedClient.item, itemX, itemY);

		// remove the scale by 2 matrix to uhh not mess up the entire pause GUI
		graphics.pose().popMatrix();

		// text
		{
			// compute text position;
			int textX = 30 + xPos;
			int textY = yPos + 5;

			// build text
			StringBuilder displayText = new StringBuilder();
			displayText.append(languageName);
			displayText.append(" Word: ");

			// find other language name for the item
			String translationKey = item.getItem().getDescriptionId();
			String otherLangName = TargetLanguageManager.getTranslation(localeCode, translationKey);
			displayText.append(otherLangName);

			System.out.println(displayText.toString());

			int textColour = 0xFFFFFFFF;
			int pronounciationColour = 0xFFFFFF00;

			graphics.text(
				Minecraft.getInstance().font,
				displayText.toString(), 
				textX, 
				textY, 
				textColour, 
				true
			);

			displayText = new StringBuilder();

			// TODO: make this not just for korean lol
			String pronounciation = HangulPhoneticConverter.toPhonetic(otherLangName);

			displayText.append(pronounciation);

			graphics.text(
				Minecraft.getInstance().font,
				displayText.toString(), 
				textX, 
				textY+20, 
				pronounciationColour, 
				true
			);
		}
    }
}
