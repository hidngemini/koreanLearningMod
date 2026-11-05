package com.hidngemini.langlocked.mixin;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.hidngemini.langlocked.event.PlayerItemPickupCallback;

@Mixin(Inventory.class)
public class PlayerInventoryMixin {

    @Shadow @Final public Player player;

    @Unique
    private int speakit$originalCount;
    @Unique
    private ItemStack speakit$snapshot;

    @Inject(method = "add(Lnet/minecraft/world/item/ItemStack;)Z", at = @At("HEAD"))
    private void onAddHead(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        this.speakit$originalCount = stack.getCount();
        this.speakit$snapshot = stack.copy();
    }

    @Inject(method = "add(Lnet/minecraft/world/item/ItemStack;)Z", at = @At("RETURN"))
    private void onAddReturn(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue() && !player.level().isClientSide() && speakit$snapshot != null) {
            int addedCount = speakit$originalCount - stack.getCount();
            if (addedCount > 0) {
                speakit$snapshot.setCount(addedCount);
                
                // send player pickup event
                PlayerItemPickupCallback.EVENT.invoker().onItemAdded(player, speakit$snapshot, addedCount);
            }
        }
    }
}