/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.tutorial;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.MultiPlayerLevel;
import net.minecraft.client.player.Input;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;

@Environment(value=EnvType.CLIENT)
public interface TutorialStepInstance {
    default public void clear() {
    }

    default public void tick() {
    }

    default public void onInput(Input input) {
    }

    default public void onMouse(double d, double e) {
    }

    default public void onLookAt(MultiPlayerLevel multiPlayerLevel, HitResult hitResult) {
    }

    default public void onDestroyBlock(MultiPlayerLevel multiPlayerLevel, BlockPos blockPos, BlockState blockState, float f) {
    }

    default public void onOpenInventory() {
    }

    default public void onGetItem(ItemStack itemStack) {
    }
}

