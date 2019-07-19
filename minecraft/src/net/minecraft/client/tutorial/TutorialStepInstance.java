package net.minecraft.client.tutorial;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.MultiPlayerLevel;
import net.minecraft.client.player.Input;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;

@Environment(EnvType.CLIENT)
public interface TutorialStepInstance {
	default void clear() {
	}

	default void tick() {
	}

	default void onInput(Input input) {
	}

	default void onMouse(double d, double e) {
	}

	default void onLookAt(MultiPlayerLevel multiPlayerLevel, HitResult hitResult) {
	}

	default void onDestroyBlock(MultiPlayerLevel multiPlayerLevel, BlockPos blockPos, BlockState blockState, float f) {
	}

	default void onOpenInventory() {
	}

	default void onGetItem(ItemStack itemStack) {
	}
}
