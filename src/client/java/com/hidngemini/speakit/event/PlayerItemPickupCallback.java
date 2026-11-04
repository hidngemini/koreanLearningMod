package com.hidngemini.speakit.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface PlayerItemPickupCallback {
    Event<PlayerItemPickupCallback> EVENT = EventFactory.createArrayBacked(PlayerItemPickupCallback.class,
        listeners -> (player, stack, addedCount) -> {
            for (PlayerItemPickupCallback listener : listeners) {
                listener.onItemAdded(player, stack, addedCount);
            }
        });

    void onItemAdded(Player player, ItemStack stack, int addedCount);
}